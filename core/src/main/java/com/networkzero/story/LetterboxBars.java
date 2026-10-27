package com.networkzero.story;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class LetterboxBars {
    private LetterboxBars() {}
    public static void draw(ShapeRenderer shapes,float width,float height,float ratio) {
        float bar=height*ratio;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.BLACK);
        shapes.rect(0,0,width,bar);
        shapes.rect(0,height-bar,width,bar);
        shapes.end();
    }
}
