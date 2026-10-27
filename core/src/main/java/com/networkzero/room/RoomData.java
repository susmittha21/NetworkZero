package com.networkzero.room;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import java.io.Reader;
import java.util.*;

public final class RoomData {
    public int imageWidth=1360,imageHeight=765;
    public String background="labs/bg_flat.png",onlineBackground="labs/bg_online.png",exitBackground="labs/exit_hallway.png";
    public List<RoomObject> objects=new ArrayList<>();
    public static RoomData load(){try(Reader r=Gdx.files.internal("data/room1.json").reader("UTF-8")){return new Gson().fromJson(r,RoomData.class);}catch(Exception e){throw new IllegalStateException("Could not load assets/data/room1.json",e);}}
}
