package com.networkzero.puzzles;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.util.Map;

/** Loads the tunable Room 2 answers and hint text from assets/data. */
public final class Room2Config {
    private static volatile String cabinet="317",door="6742",attacker="10.20.30.77";
    private static volatile int portsCount=7,camera=6;
    private static volatile Map<String,String> ports=ServiceRegisterMatcher.ANSWER;
    private static volatile Map<String,String[]> hints=Map.of();
    private Room2Config(){}
    public static synchronized void load(){
        try{
            String file="data/puzzles2.json";
            if(!Gdx.files.internal(file).exists())file="data/puzzles3.json";
            if(!Gdx.files.internal(file).exists())throw new IllegalStateException("Required puzzle data missing: assets/"+file);
            JsonObject j=new Gson().fromJson(Gdx.files.internal(file).readString("UTF-8"),JsonObject.class);
            cabinet=j.get("cabinetCode").getAsString();
            door=j.get("doorCode").getAsString();
            attacker=j.get("logSource").getAsString();
            portsCount=j.get("differentPorts").getAsInt();
            if(j.has("tamperedCamera"))camera=j.get("tamperedCamera").getAsInt();
            ports=new Gson().fromJson(j.get("servicePorts"),new TypeToken<Map<String,String>>(){}.getType());
            String hf="data/hints2.json";
            if(!Gdx.files.internal(hf).exists())hf="data/hints3.json";
            if(!Gdx.files.internal(hf).exists())throw new IllegalStateException("Required hint data missing: assets/"+hf);
            JsonObject hj=new Gson().fromJson(Gdx.files.internal(hf).readString("UTF-8"),JsonObject.class);
            java.util.LinkedHashMap<String,String[]> parsed=new java.util.LinkedHashMap<>();
            for(String id:new String[]{"P1","P2","P3","P4","P5","P6"})parsed.put(id,new Gson().fromJson(hj.getAsJsonArray(id),String[].class));
            hints=Map.copyOf(parsed);
        }catch(Exception e){
            throw new IllegalStateException("Could not load Room 2 puzzle data: "+e.getMessage(),e);
        }
    }
    public static String cabinetCode(){return cabinet;}
    public static String doorCode(){return door;}
    public static String attacker(){return attacker;}
    public static int differentPorts(){return portsCount;}
    public static int tamperedCamera(){return camera;}
    public static Map<String,String> servicePorts(){return ports;}
    public static String hint(String id,int tier){
        String[] values=hints.get(id);
        return values==null||values.length==0?"Hint data unavailable.":values[Math.max(0,Math.min(values.length-1,tier-1))];
    }
}
