package com.networkzero.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.networkzero.room.GameState;

/** Exit hall and keypad; callbacks return to the room or complete it. */
public final class ExitScreen implements Screen {
    private final Game game;
    private final GameState state;
    private final Runnable back, unlocked;
    private final FitViewport viewport = new FitViewport(1920, 1080);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private Texture bg;
    private BitmapFont font;
    private String code = "", error = "";
    private boolean missing;

    public ExitScreen(Game game, GameState state, Runnable back, Runnable unlocked) {
        this.game = game;
        this.state = state;
        this.back = back;
        this.unlocked = unlocked;
        try {
            String image = "labs/exit_hallway.png", fontPath = "fonts/PressStart2P.ttf";
            if (Gdx.files.internal(image).exists()) {
                bg = new Texture(Gdx.files.internal(image), true);
                bg.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
            } else {
                System.err.println("Optional room image missing: assets/" + image);
            }
            if (!Gdx.files.internal(fontPath).exists()) throw new IllegalStateException("Required room font missing: assets/" + fontPath);
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(fontPath));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = 24;
            p.minFilter = Texture.TextureFilter.Nearest;
            p.magFilter = Texture.TextureFilter.Nearest;
            font = gen.generateFont(p);
            gen.dispose();
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
                if (p.x < 450 && p.y < 300) {
                    back.run();
                    return true;
                }
                for (int i = 0; i < 12; i++) {
                    float x = 1050 + (i % 3) * 170, y = 555 - (i / 3) * 105;
                    if (p.x >= x && p.x <= x + 150 && p.y >= y && p.y <= y + 70) {
                        if (i < 9 && code.length() < 4) code += (i + 1);
                        else if (i == 9) code = "";
                        else if (i == 10 && code.length() < 4) code += "0";
                        else if (i == 11) {
                            if ("2791".equals(code)) unlocked.run();
                            else { error = "Wrong code"; code = ""; }
                        }
                        return true;
                    }
                }
                return false;
            }
            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.ESCAPE) {
                    back.run();
                    return true;
                }
                return false;
            }
        });
    }

    @Override public void render(float dt) {
        ScreenUtils.clear(.015f, .02f, .03f, 1);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        if (bg != null) batch.draw(bg, 0, 63, 1680, 945);
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(.07f, .08f, .11f, 1);
        shapes.rect(1680, 63, 240, 945);
        shapes.rect(0, 0, 1920, 63);
        shapes.rect(1050, 690, 560, 120);

        // Keypad button backgrounds
        shapes.setColor(new Color(.11f, .12f, .16f, 1));
        for (int i = 0; i < 12; i++) {
            float x = 1050 + (i % 3) * 170, y = 555 - (i / 3) * 105;
            shapes.rect(x, y, 150, 70);
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1, .55f, .1f, 1);
        shapes.rect(1680, 63, 240, 945);
        shapes.rect(1050, 690, 560, 120);
        for (int i = 0; i < 12; i++) {
            float x = 1050 + (i % 3) * 170, y = 555 - (i / 3) * 105;
            shapes.rect(x, y, 150, 70);
        }
        shapes.end();

        batch.begin();
        font.setColor(.95f, .93f, .86f, 1);
        font.draw(batch, "DOOR CODE", 1050, 780, 560, com.badlogic.gdx.utils.Align.center, false);
        font.draw(batch, code.isEmpty() ? "----" : code, 1050, 735, 560, com.badlogic.gdx.utils.Align.center, false);

        for (int i = 0; i < 12; i++) {
            String s = i < 9 ? "" + (i + 1) : i == 9 ? "CLEAR" : i == 10 ? "0" : "ENTER";
            float x = 1050 + (i % 3) * 170, y = 555 - (i / 3) * 105;
            font.draw(batch, s, x, y + 48, 150, com.badlogic.gdx.utils.Align.center, false);
        }

        font.setColor(1, .25f, .22f, 1);
        font.draw(batch, error, 1050, 235, 560, com.badlogic.gdx.utils.Align.center, false);

        font.setColor(.95f, .93f, .86f, 1);
        font.draw(batch, "BACK TO LAB", 40, 42);
        batch.end();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose();
        shapes.dispose();
        if (bg != null) bg.dispose();
        if (font != null) font.dispose();
    }
}