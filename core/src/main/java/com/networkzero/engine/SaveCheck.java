package com.networkzero.engine;

import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class SaveCheck {
    private SaveCheck() {}
    private static Path file(){return ProfilePaths.saves().resolve("progress.json");}
    public static boolean exists() { return storySeen(); }
    /** 1 resumes Room 1, 2 resumes Room 2 (the final room), and 0 means no resumable run. */
    public static int continueRoom(){try{if(!Files.isRegularFile(file()))return 0;JsonObject data=JsonParser.parseString(Files.readString(file())).getAsJsonObject();if(!data.has("room"))return 0;String room=data.get("room").getAsString();if("complete".equalsIgnoreCase(room))return 0;if("2".equals(room)||"3".equals(room))return 2;return "1".equals(room)?1:0;}catch(Exception ignored){return 0;}}
    public static boolean storySeen(){try{if(!Files.isRegularFile(file()))return false;JsonObject data=JsonParser.parseString(Files.readString(file())).getAsJsonObject();return data.has("storySeen")&&data.get("storySeen").getAsBoolean();}catch(Exception ignored){return false;}}
    public static void markStoryComplete(){try{Files.createDirectories(file().getParent());Files.writeString(file(),"{\"storySeen\": true, \"room\": 1}\n");}catch(Exception e){System.err.println("Could not write saves/progress.json: "+e.getMessage());}}
}
