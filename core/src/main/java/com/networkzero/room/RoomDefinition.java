package com.networkzero.room;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Set;

/** Validated room configuration used by the second room and future data-backed rooms. */
public final class RoomDefinition {
    public int imageWidth, imageHeight;
    public String background, onlineBackground;
    public boolean isFinal;
    public ClueOwners clueOwners = new ClueOwners();
    public java.util.List<RoomObject> objects;

    public static final class ClueOwners { public String bookshelf="host", incident_board="client"; }

    public static RoomDefinition loadRoom2() {
        String file="data/room2.json";
        if (!Gdx.files.internal(file).exists()) file="data/room3.json";
        try {
            if (!Gdx.files.internal(file).exists()) throw new IllegalStateException("Required room definition missing: assets/"+file);
            RoomDefinition d=new Gson().fromJson(Gdx.files.internal(file).readString("UTF-8"),RoomDefinition.class);
            if (d==null||d.imageWidth<=0||d.imageHeight<=0||d.objects==null) throw new IllegalStateException("Invalid room definition: assets/"+file);
            Set<String> ids=new HashSet<>();
            for(RoomObject o:d.objects){if(o==null||o.id==null||o.name==null||!ids.add(o.id)||!Float.isFinite(o.x)||!Float.isFinite(o.y)||!Float.isFinite(o.w)||!Float.isFinite(o.h)||o.w<=0||o.h<=0||o.x<0||o.y<0||o.x+o.w>1.00001f||o.y+o.h>1.00001f)throw new IllegalStateException("Invalid hotspot in assets/"+file);}
            RoomObject a=find(d,"filing_a"),b=find(d,"filing_b"),c=find(d,"filing_c");
            if(overlaps(a,b)||overlaps(b,c)||overlaps(a,c))throw new IllegalStateException("Room 2 filing cabinet hotspots overlap in assets/"+file);
            return d;
        }catch(RuntimeException e){throw e;}catch(Exception e){throw new IllegalStateException("Could not load assets/"+file,e);}
    }

    public static RoomDefinition loadRoom3() {
        return loadRoom2();
    }
    private static RoomObject find(RoomDefinition d,String id){for(RoomObject o:d.objects)if(id.equals(o.id))return o;throw new IllegalStateException("Missing Room 2 hotspot: "+id);}
    private static boolean overlaps(RoomObject a,RoomObject b){return a.x<b.x+b.w&&a.x+a.w>b.x&&a.y<b.y+b.h&&a.y+a.h>b.y;}
}
