package com.networkzero.story;

public final class SkipHold {
    private float held;
    public void update(float delta, boolean down) { held=down?Math.min(1,held+delta):0; }
    public float progress() { return held; }
    public boolean completed() { return held>=1; }
}
