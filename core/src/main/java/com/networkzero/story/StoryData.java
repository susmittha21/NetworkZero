package com.networkzero.story;

import java.util.List;

public final class StoryData {
    public int sourceWidth = 1376;
    public int sourceHeight = 768;
    public float letterboxRatio = .12f;
    public boolean filmGrain = false;
    public boolean vignette = false;
    public List<StoryPanel> panels;
    public TitleCard titleCard;

    public static final class TitleCard {
        public List<String> text;
        public String prompt = "PRESS SPACE TO BEGIN";
    }
}
