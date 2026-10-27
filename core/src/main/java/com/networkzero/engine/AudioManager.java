package com.networkzero.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.*;

/**
 * Manages looping background music (humming UI ambient) and short UI sounds
 * for the landing screen and menus.
 */
public final class AudioManager implements AutoCloseable {
    private final List<Music> music = new ArrayList<>();
    private final Map<String, Sound> sounds = new HashMap<>();
    /** Master volume target for background music (0 to 1). */
    private float target = .65f, fade = 0;

    public AudioManager() {
        // Primary ambient: the UI humming tone present in assets
        loadMusic("audio/humming_ui.wav");
        // Optional extras (silent if file missing)
        loadMusic("audio/music-menu.mp3");
        loadMusic("audio/rain_loop.wav");
        loadSound("hover", "audio/button_ui_normalized.wav");
        loadSound("click", "audio/click_ui.wav");
    }

    private void loadMusic(String p) {
        try {
            if (Gdx.files.internal(p).exists()) {
                Music m = Gdx.audio.newMusic(Gdx.files.internal(p));
                m.setLooping(true);
                m.play();
                m.setVolume(0);
                music.add(m);
            } else {
                System.out.println("Optional audio missing: " + p);
            }
        } catch (Exception e) {
            System.out.println("Optional audio unavailable: " + p + " (" + e.getMessage() + ")");
        }
    }

    private void loadSound(String k, String p) {
        try {
            if (Gdx.files.internal(p).exists()) {
                sounds.put(k, Gdx.audio.newSound(Gdx.files.internal(p)));
            } else {
                System.out.println("Optional audio missing: " + p);
            }
        } catch (Exception e) {
            System.out.println("Optional audio unavailable: " + p + " (" + e.getMessage() + ")");
        }
    }

    public void setVolume(float v) { target = Math.max(0, Math.min(1, v)); }
    public void fadeIn(float seconds) { fade = seconds <= 0 ? 1 : 0; }

    public void update(float dt) {
        fade = Math.min(1, fade + dt);
        for (Music m : music) m.setVolume(target * fade);
    }

    public void play(String key) {
        Sound s = sounds.get(key);
        if (s != null) s.play(1f);
    }

    @Override
    public void close() {
        music.forEach(Music::dispose);
        sounds.values().forEach(Sound::dispose);
    }
}