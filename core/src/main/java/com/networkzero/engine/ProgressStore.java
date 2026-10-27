package com.networkzero.engine;

import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes the final two-room progress record without hand-built JSON. */
public final class ProgressStore {
    private ProgressStore() {}

    public static void writeComplete(Path progressFile, int room1Time, int room1Hints, int room1Score,
                                     int room2Time, int room2Hints, int room2Score) throws Exception {
        JsonObject root=new JsonObject();root.addProperty("storySeen",true);root.addProperty("room","complete");
        JsonObject first=new JsonObject();first.addProperty("time",room1Time);first.addProperty("hints",room1Hints);first.addProperty("score",room1Score);root.add("room1",first);
        JsonObject last=new JsonObject();last.addProperty("time",room2Time);last.addProperty("hints",room2Hints);last.addProperty("score",room2Score);
        root.add("room2",last);
        root.add("room3",last);
        Files.createDirectories(progressFile.getParent());Files.writeString(progressFile,new GsonBuilder().setPrettyPrinting().create().toJson(root));
    }
}
