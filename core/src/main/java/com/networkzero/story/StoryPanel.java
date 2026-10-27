package com.networkzero.story;

import java.util.List;

public final class StoryPanel {
    public String id;
    public String image;
    public List<String> text;
    public CameraData camera;
    public List<EffectConfig> effects;
    public String musicCue;
    public List<AudioCueData> audioCues;
    public ScreenTextData screenText;
    public float[] rainRegion;
    public float rainWind = 18;

    public static final class CameraData {
        public float startZoom = 1f;
        public float endZoom = 1.02f;
        /** Small camera offsets in source-image pixels, x then y. */
        public float[] startPan = {0, 0};
        public float[] endPan = {0, 0};
        public float duration = 12f;
    }

    public static final class ScreenTextData {
        public float x = 210, y = 340, width = 360, height = 180;
        public String headline = "WORM DETECTED";
        public String subline = "NETWORK LOCKED";
    }

    public static final class EffectConfig {
        public String name;
        public float[] region;
        public float[] point;
        public float[][] points;
        public float intensity = 1f;

        public EffectConfig() {}
        public EffectConfig(String name) { this.name = name; }

        @Override
        public String toString() {
            return name != null ? name : "";
        }
    }

    public static final class AudioCueData {
        public int line;
        public String sound;
    }
}
