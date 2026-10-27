package com.networkzero.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** End-of-room results; fixed layout with click targets. */
public final class RoomSummaryScreen implements Screen {
    private final Game game;
    private final int seconds, hints, score;
    private final FitViewport viewport = new FitViewport(1920, 1080);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private BitmapFont font;
    private boolean missing;

    public RoomSummaryScreen(Game game, int seconds, int hints, int score) {
        this.game = game;
        this.seconds = seconds;
        this.hints = hints;
        this.score = score;
        try {
            String p = "fonts/PressStart2P.ttf";
            if (!Gdx.files.internal(p).exists()) throw new IllegalStateException("Required room font missing: assets/" + p);
            FreeTypeFontGenerator g = new FreeTypeFontGenerator(Gdx.files.internal(p));
            FreeTypeFontGenerator.FreeTypeFontParameter f = new FreeTypeFontGenerator.FreeTypeFontParameter();
            f.size = 24;
            f.minFilter = Texture.TextureFilter.Nearest;
            f.magFilter = Texture.TextureFilter.Nearest;
            font = g.generateFont(f);
            g.dispose();
        } catch (Exception e) {
            missing = true;
            System.err.println(e.getMessage());
        }
    }

    @Override public void show() {
        if (missing) { game.setScreen(new LandingScreen(game)); return; }
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchUp(int sx, int sy, int pointer, int button) {
                Vector2 p = viewport.unproject(new Vector2(sx, sy));
                if (p.y >= 290 && p.y <= 400) {
                    if (p.x >= 350 && p.x <= 750) {
                        com.networkzero.room.GameState next = new com.networkzero.room.GameState();
                        next.beginRoom3();
                        game.setScreen(new Room3Screen(game, new com.networkzero.session.LocalSession(next, "Player", null), false));
                    } else if (p.x >= 760 && p.x <= 1160) {
                        game.setScreen(new RoomScreen(game, false));
                    } else if (p.x >= 1170 && p.x <= 1570) {
                        game.setScreen(new LandingScreen(game));
                    }
                    return true;
                }
                return false;
            }
            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.ESCAPE) {
                    game.setScreen(new LandingScreen(game));
                    return true;
                }
                return false;
            }
        });
    }

    @Override public void render(float dt) {
        ScreenUtils.clear(.02f, .027f, .04f, 1);
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        // Modal panel
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(.07f, .08f, .11f, 1);
        shapes.rect(320, 230, 1280, 620);
        // Button fills
        shapes.setColor(new Color(.11f, .12f, .16f, 1));
        shapes.rect(360, 300, 380, 80);
        shapes.rect(770, 300, 380, 80);
        shapes.rect(1180, 300, 380, 80);
        shapes.end();

        // Borders
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1, .55f, .1f, 1);
        shapes.rect(320, 230, 1280, 620);
        shapes.rect(360, 300, 380, 80);
        shapes.rect(770, 300, 380, 80);
        shapes.rect(1180, 300, 380, 80);
        shapes.end();

        int stars = seconds < 1500 && hints == 0 ? 3 : seconds < 2100 || hints <= 2 ? 2 : 1;

        batch.begin();
        font.setColor(1, .55f, .1f, 1);
        font.draw(batch, "ROOM COMPLETE", 320, 790, 1280, com.badlogic.gdx.utils.Align.center, false);

        font.setColor(.94f, .93f, .86f, 1);
        font.draw(batch, String.format("TIME %02d:%02d", seconds / 60, seconds % 60), 320, 700, 1280, com.badlogic.gdx.utils.Align.center, false);
        font.draw(batch, "HINTS " + hints + "   SCORE " + score, 320, 620, 1280, com.badlogic.gdx.utils.Align.center, false);

        font.setColor(1, .65f, .2f, 1);
        font.draw(batch, "STARS " + "* ".repeat(stars), 320, 540, 1280, com.badlogic.gdx.utils.Align.center, false);

        font.setColor(.94f, .93f, .86f, 1);
        font.draw(batch, "NEXT ROOM: CONTROL OFFICE", 360, 368, 380, com.badlogic.gdx.utils.Align.center, true);
        font.draw(batch, "REPLAY ROOM 1", 770, 350, 380, com.badlogic.gdx.utils.Align.center, false);
        font.draw(batch, "MAIN MENU", 1180, 350, 380, com.badlogic.gdx.utils.Align.center, false);
        batch.end();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose();
        shapes.dispose();
        if (font != null) font.dispose();
    }
}