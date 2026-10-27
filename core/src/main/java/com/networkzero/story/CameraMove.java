package com.networkzero.story;

import com.badlogic.gdx.math.MathUtils;

public final class CameraMove {
    private float elapsed;
    public void reset() { elapsed = 0; }
    public void update(float delta) { elapsed += delta; }
    public float progress(StoryPanel panel) {
        float duration = panel.camera == null ? 1 : Math.max(.1f, panel.camera.duration);
        float t = MathUtils.clamp(elapsed / duration, 0, 1);
        return t * t * (3 - 2 * t);
    }
    public float zoom(StoryPanel panel) {
        if (panel.camera == null) return 1;
        return MathUtils.lerp(panel.camera.startZoom, panel.camera.endZoom, progress(panel));
    }
    public float panX(StoryPanel panel) { return pan(panel, 0); }
    public float panY(StoryPanel panel) { return pan(panel, 1); }
    private float pan(StoryPanel panel, int axis) {
        if (panel.camera == null) return 0;
        float[] start=panel.camera.startPan, end=panel.camera.endPan;
        float a=start!=null&&start.length>axis?start[axis]:0;
        float b=end!=null&&end.length>axis?end[axis]:0;
        return MathUtils.lerp(a,b,progress(panel));
    }
}
