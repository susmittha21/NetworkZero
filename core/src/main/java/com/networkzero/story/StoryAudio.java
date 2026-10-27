package com.networkzero.story;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages audio for story sequences (intro and ending):
 * background music, ambient loops, alarm, typing click sounds, and one-shot SFX.
 * Typing volume is amplified by 1.6x (capped at 1.0) to be clearly audible.
 */
public final class StoryAudio implements AutoCloseable {
    private final Map<String, Sound> sounds = new HashMap<>();
    private final Music storyMusic, rainMusic, alarm, typingMusic, endingMusic;
    private final float musicVolume, sfxVolume;
    private final boolean isEnding;
    /** Boost factor applied to typing sound so it cuts through clearly. */
    private static final float TYPING_BOOST = 1.6f;
    private float fadeIn;

    public StoryAudio(float musicVolume, float sfxVolume) {
        this(musicVolume, sfxVolume, false);
    }

    public StoryAudio(float musicVolume, float sfxVolume, boolean isEnding) {
        this.musicVolume = musicVolume;
        this.sfxVolume   = sfxVolume;
        this.isEnding    = isEnding;

        typingMusic = loadMusic("audio/story_typing.ogg");

        if (isEnding) {
            storyMusic  = null;
            rainMusic   = null;
            alarm       = null;
            endingMusic = loadMusic("audio/ending_music.ogg");
            loadSound("door_open", "audio/door_open.ogg");
            loadSound("power_up", "audio/power_up.ogg");
            if (endingMusic != null) endingMusic.play();
        } else {
            endingMusic = null;
            storyMusic  = loadMusic("audio/story_music.wav");
            rainMusic   = loadMusic("audio/rain_loop.wav");
            alarm       = loadMusic("audio/alarm.mp3");
            loadSound("door_lock", "audio/door_lock.wav");
            if (storyMusic != null) storyMusic.play();
            if (rainMusic  != null) rainMusic.play();
            if (alarm      != null) alarm.play();
        }
    }

    private Music loadMusic(String path) {
        try {
            if (!Gdx.files.internal(path).exists()) {
                System.out.println("Optional story audio missing: " + path);
                return null;
            }
            Music m = Gdx.audio.newMusic(Gdx.files.internal(path));
            m.setLooping(true);
            m.setVolume(0);
            return m;
        } catch (Exception e) {
            System.out.println("Optional story audio unavailable: " + path + " (" + e.getMessage() + ")");
            return null;
        }
    }

    private void loadSound(String key, String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                sounds.put(key, Gdx.audio.newSound(Gdx.files.internal(path)));
            } else {
                System.out.println("Optional story audio missing: " + path);
            }
        } catch (Exception e) {
            System.out.println("Optional story audio unavailable: " + path + " (" + e.getMessage() + ")");
        }
    }

    public void update(float delta, int panelIndex, boolean titleCard, boolean ending, boolean typing) {
        fadeIn = Math.min(1, fadeIn + delta);

        if (isEnding) {
            float musicTarget = ending ? 0 : musicVolume * fadeIn;
            if (endingMusic != null) endingMusic.setVolume(musicTarget);
        } else {
            float duck = (panelIndex == 2 || panelIndex == 3) ? .6f : 1f;
            float musicTarget = ending ? 0 : musicVolume * duck * fadeIn;
            if (storyMusic != null) storyMusic.setVolume(musicTarget);

            // Rain ambient on panels 0 (campus), 1 (lab window), and 3 (locked doors in rain)
            boolean hasRain = (panelIndex == 0 || panelIndex == 1 || panelIndex == 3);
            float rainTarget = !ending && !titleCard && hasRain ? musicVolume * .4f * fadeIn : 0;
            if (rainMusic != null) rainMusic.setVolume(rainTarget);

            float alarmTarget = !ending && !titleCard && (panelIndex == 2 || panelIndex == 3)
                                ? musicVolume * .22f * fadeIn : 0;
            if (alarm != null) alarm.setVolume(alarmTarget);
        }

        if (typingMusic != null) {
            if (typing && !typingMusic.isPlaying()) typingMusic.play();
            else if (!typing && typingMusic.isPlaying()) typingMusic.stop();
            typingMusic.setVolume(typing ? Math.min(1f, sfxVolume * TYPING_BOOST) : 0);
        }
    }

    public void doorLock() { play("door_lock", sfxVolume); }
    public void doorOpen() { play("door_open", sfxVolume); }
    public void powerUp()  { play("power_up", sfxVolume); }

    public void playCue(String cue) {
        if (cue == null) return;
        String key = cue.replace("audio/", "").replace(".ogg", "").replace(".wav", "").replace(".mp3", "");
        if (!sounds.containsKey(key)) {
            loadSound(key, cue.startsWith("audio/") ? cue : "audio/" + cue);
        }
        play(key, sfxVolume);
    }

    private void play(String key, float volume) {
        Sound sound = sounds.get(key);
        if (sound != null && volume > 0) sound.play(volume);
    }

    @Override
    public void close() {
        if (storyMusic  != null) storyMusic.dispose();
        if (rainMusic   != null) rainMusic.dispose();
        if (alarm       != null) alarm.dispose();
        if (endingMusic != null) endingMusic.dispose();
        if (typingMusic != null) typingMusic.dispose();
        for (Sound sound : sounds.values()) sound.dispose();
    }
}