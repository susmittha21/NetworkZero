package com.networkzero.room;

import com.google.gson.GsonBuilder;
import java.nio.file.*;

/** Source-image normalized hotspot selection and editing helpers. */
public final class HotspotEditor {
    private RoomObject selected;
    private int resizeX,resizeY;
    public RoomObject selected(){return selected;}
    public RoomObject begin(RoomData data,float nx,float ny){selected=null;float smallest=Float.MAX_VALUE;for(RoomObject o:data.objects){if(nx>=o.x&&nx<=o.x+o.w&&ny>=o.y&&ny<=o.y+o.h&&o.w*o.h<smallest){selected=o;smallest=o.w*o.h;}}if(selected!=null){resizeX=nx<selected.x+.02f?-1:nx>selected.x+selected.w-.02f?1:0;resizeY=ny<selected.y+.02f?-1:ny>selected.y+selected.h-.02f?1:0;}return selected;}
    public void drag(float dx,float dy){if(selected==null)return;if(resizeX<0){float right=selected.x+selected.w;selected.x=clamp(selected.x+dx,0,right-.01f);selected.w=right-selected.x;}else if(resizeX>0)selected.w=clamp(selected.w+dx,.01f,1-selected.x);else selected.x=clamp(selected.x+dx,0,1-selected.w);if(resizeY<0){float bottom=selected.y+selected.h;selected.y=clamp(selected.y+dy,0,bottom-.01f);selected.h=bottom-selected.y;}else if(resizeY>0)selected.h=clamp(selected.h+dy,.01f,1-selected.y);else selected.y=clamp(selected.y+dy,0,1-selected.h);}
    public void nudge(RoomData data,int px,int py){if(selected!=null){selected.x=clamp(selected.x+px/(float)data.imageWidth,0,1-selected.w);selected.y=clamp(selected.y+py/(float)data.imageHeight,0,1-selected.h);}}
    public void save(RoomData data){try{Path p=Path.of("assets","data","room1.json");Files.writeString(p,new GsonBuilder().setPrettyPrinting().create().toJson(data));}catch(Exception e){throw new IllegalStateException("Could not save assets/data/room1.json",e);}}
    private float clamp(float x,float min,float max){return Math.max(min,Math.min(max,x));}
}
