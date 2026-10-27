package com.networkzero.story;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface StoryEffect {
    void update(float delta);
    void render(ShapeRenderer shapes, StoryScreen.PanelTransform transform);
}
