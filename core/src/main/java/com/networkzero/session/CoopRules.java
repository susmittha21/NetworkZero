package com.networkzero.session;

import com.badlogic.gdx.Gdx;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;

/** Small configurable role rules shared by UI and host validation. */
public final class CoopRules {
    private static volatile boolean asymmetricClues=true;
    private static volatile Map<String,String> room2Owners=Map.of("bookshelf","host","incident_board","client");
    private CoopRules(){}
    public static void load(){try{JsonObject j=JsonParser.parseString(Gdx.files.internal("data/coop.json").readString("UTF-8")).getAsJsonObject();asymmetricClues=j.has("asymmetricClues")&&j.get("asymmetricClues").getAsBoolean();String file="data/room2.json";if(!Gdx.files.internal(file).exists())file="data/room3.json";JsonObject r=JsonParser.parseString(Gdx.files.internal(file).readString("UTF-8")).getAsJsonObject();if(r.has("clueOwners")&&r.get("clueOwners").isJsonObject())room2Owners=new Gson().fromJson(r.get("clueOwners"),Map.class);}catch(Exception e){asymmetricClues=true;room2Owners=Map.of("bookshelf","host","incident_board","client");}}
    public static boolean asymmetricClues(){return asymmetricClues;}
    public static boolean canOpen(boolean host,String objectId){return canOpen(1,host,objectId);}
    public static boolean canOpen(int room,boolean host,String objectId){if(!asymmetricClues)return true;if(room==2||room==3){String owner=room2Owners.get(objectId);return owner==null||owner.equalsIgnoreCase(host?"host":"client");}if("logbook".equals(objectId))return host;if("notice_board".equals(objectId))return !host;return true;}
}
