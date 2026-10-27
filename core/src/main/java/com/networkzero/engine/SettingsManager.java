package com.networkzero.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.*;

public final class SettingsManager {
    public float music = .65f, sfx = .8f;
    public boolean fullscreen;
    public boolean skipStory;
    public String playerName = "Player";
    private final transient Path file = ProfilePaths.root().resolve("settings.json");
    private final transient Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public void load() {
        try { if (Files.exists(file)) { SettingsManager s = gson.fromJson(Files.readString(file), SettingsManager.class); if (s != null) { music=s.music; sfx=s.sfx; fullscreen=s.fullscreen; skipStory=s.skipStory; playerName=s.playerName == null ? "Player" : s.playerName; } } }
        catch (Exception e) { System.err.println("Could not load settings: " + e.getMessage()); }
    }
    public void save() {
        try { Files.createDirectories(file.getParent()); Files.writeString(file, gson.toJson(this)); }
        catch (Exception e) { System.err.println("Could not save settings: " + e.getMessage()); }
    }
}
