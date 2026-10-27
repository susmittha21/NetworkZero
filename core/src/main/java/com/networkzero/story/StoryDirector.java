package com.networkzero.story;

import java.util.List;

public final class StoryDirector {
    private final List<StoryPanel> panels;
    private int index;
    public StoryDirector(List<StoryPanel> panels) { this.panels=panels; }
    public StoryPanel current() { return index>=0&&index<panels.size()?panels.get(index):null; }
    public int index() { return index; }
    public int count() { return panels.size(); }
    public boolean advance() { if(index<panels.size())index++; return index<panels.size(); }
    public void restartAt(int panelIndex) { index=Math.max(0,Math.min(panelIndex,Math.max(0,panels.size()-1))); }
}
