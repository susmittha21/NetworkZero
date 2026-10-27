package com.networkzero.story;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.google.gson.*;
import com.networkzero.engine.SaveCheck;
import com.networkzero.engine.SettingsManager;
import com.networkzero.screens.LandingScreen;
import com.networkzero.screens.RoomScreen;
import com.networkzero.session.*;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Player-paced story sequence supporting both intro and ending presentations. */
public final class StoryScreen implements Screen {
    private static final float W = 1920, H = 1080, HALF_TRANSITION = .4f;
    private final Game game;
    private final GameSession session;
    private final Runnable onComplete;
    private final String dataFilePath;
    private final boolean isEndingSequence;
    private boolean onCompleteCalled;
    private static final float SKIP_BTN_W = 240, SKIP_BTN_H = 44;
    private float skipBtnX() { return W - SKIP_BTN_W - 50; }
    private float skipBtnY() { return H - (H * (data != null ? data.letterboxRatio : 0.12f)) + 12; }
    private boolean isSkipIntroHit(float px, float py) {
        if (isEndingSequence || titleCard) return false;
        float x = skipBtnX(), y = skipBtnY();
        return px >= x && px <= x + SKIP_BTN_W && py >= y && py <= y + SKIP_BTN_H;
    }
    private final FitViewport viewport = new FitViewport(W, H);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Random grainRandom = new Random(77);
    private StoryDirector director;
    private StoryData data;
    private final Array<Texture> images = new Array<>();
    private final List<StoryEffect> effects = new ArrayList<>();
    private final TypewriterText typewriter = new TypewriterText();
    private final CameraMove camera = new CameraMove();
    private final SkipHold skipHold = new SkipHold();
    private final StoryAudio audio;
    private BitmapFont textFont, titleFont;
    private boolean failed, titleCard, debug, ending, skipTriggered;
    private float transitionLeft = .8f, transitionDuration = .8f, screenElapsed, titleElapsed, endingElapsed;
    private Transition transition = Transition.IN;
    private enum Transition { NONE, OUT, IN }

    public StoryScreen(Game game) {
        this(game, "data/story.json", null, null);
    }

    public StoryScreen(Game game, Runnable onComplete) {
        this(game, "data/story.json", null, onComplete);
    }

    public StoryScreen(Game game, GameSession session, Runnable onComplete) {
        this(game, "data/story.json", session, onComplete);
    }

    public StoryScreen(Game game, String storyDataPath, Runnable onComplete) {
        this(game, storyDataPath, null, onComplete);
    }

    public StoryScreen(Game game, String storyDataPath, GameSession session, Runnable onComplete) {
        this.game = game;
        this.session = session;
        this.dataFilePath = (storyDataPath == null || storyDataPath.isBlank()) ? "data/story.json" : storyDataPath;
        this.isEndingSequence = this.dataFilePath.toLowerCase().contains("ending");
        this.onComplete = onComplete;
        SettingsManager settings = new SettingsManager();
        settings.load();
        audio = new StoryAudio(settings.music, settings.sfx, isEndingSequence);
        loadData();
        if (!failed) {
            director = new StoryDirector(data.panels);
            createFonts();
            typewriter.setLines(director.current().text);
            configureEffects();
            checkAudioCues();
        } else {
            director = null;
        }
    }

    private String resolveImagePath(String path) {
        if (path == null) return null;
        if (Gdx.files.internal(path).exists()) return path;
        if (Gdx.files.internal("story/" + path).exists()) return "story/" + path;
        if (Gdx.files.internal("story/end_panel/" + path).exists()) return "story/end_panel/" + path;
        if (Gdx.files.internal("landing/" + path).exists()) return "landing/" + path;
        if (Gdx.files.internal("story/backgrounds/" + path).exists()) return "story/backgrounds/" + path;
        if (Gdx.files.internal("story/panels/" + path).exists()) return "story/panels/" + path;
        return path;
    }

    private void loadData() {
        try {
            if (!Gdx.files.internal(dataFilePath).exists()) {
                throw new IllegalStateException("Required story data missing: assets/" + dataFilePath);
            }
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(StoryPanel.EffectConfig.class, (JsonDeserializer<StoryPanel.EffectConfig>) (json, type, ctx) -> {
                    if (json.isJsonPrimitive()) {
                        return new StoryPanel.EffectConfig(json.getAsString());
                    } else if (json.isJsonObject()) {
                        JsonObject obj = json.getAsJsonObject();
                        StoryPanel.EffectConfig c = new StoryPanel.EffectConfig();
                        if (obj.has("name")) c.name = obj.get("name").getAsString();
                        if (obj.has("type") && c.name == null) c.name = obj.get("type").getAsString();
                        if (obj.has("region")) c.region = ctx.deserialize(obj.get("region"), float[].class);
                        if (obj.has("point")) c.point = ctx.deserialize(obj.get("point"), float[].class);
                        if (obj.has("points")) c.points = ctx.deserialize(obj.get("points"), float[][].class);
                        if (obj.has("intensity")) c.intensity = obj.get("intensity").getAsFloat();
                        return c;
                    }
                    return new StoryPanel.EffectConfig();
                })
                .create();

            try (Reader reader = Gdx.files.internal(dataFilePath).reader("UTF-8")) {
                data = gson.fromJson(reader, StoryData.class);
            }
            if (data == null || data.panels == null || data.panels.isEmpty() || data.titleCard == null) {
                throw new IllegalStateException("Invalid story data: assets/" + dataFilePath);
            }
            if (!Gdx.files.internal("fonts/Silkscreen-Regular.ttf").exists()) {
                throw new IllegalStateException("Required story font missing: assets/fonts/Silkscreen-Regular.ttf");
            }
            if (!Gdx.files.internal("fonts/Silkscreen-Bold.ttf").exists()) {
                throw new IllegalStateException("Required story font missing: assets/fonts/Silkscreen-Bold.ttf");
            }
            for (StoryPanel panel : data.panels) {
                String resolved = resolveImagePath(panel.image);
                if (resolved == null || !Gdx.files.internal(resolved).exists()) {
                    throw new IllegalStateException("Required story image missing: assets/" + (resolved != null ? resolved : panel.image));
                }
                Texture t = new Texture(Gdx.files.internal(resolved), true);
                t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
                images.add(t);
            }
        } catch (Exception e) {
            failed = true;
            Gdx.app.error("NETWORK ZERO STORY", e.getMessage(), e);
        }
    }

    private void createFonts() {
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = 23;
        p.magFilter = Texture.TextureFilter.Nearest;
        p.minFilter = Texture.TextureFilter.Nearest;
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Silkscreen-Regular.ttf"));
        textFont = gen.generateFont(p);
        gen.dispose();

        p.size = 42;
        p.borderWidth = 2;
        p.borderColor = new Color(.02f, .03f, .04f, 1);
        gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Silkscreen-Bold.ttf"));
        titleFont = gen.generateFont(p);
        gen.dispose();
    }

    private void configureEffects() {
        effects.clear();
        StoryPanel panel = director.current();
        if (panel == null || panel.effects == null) return;

        for (StoryPanel.EffectConfig eff : panel.effects) {
            if (eff == null || eff.name == null) continue;
            String name = eff.name;
            if ("rain".equals(name) || "rain_window".equals(name)) {
                float[] r = panel.rainRegion;
                float left = r != null && r.length > 3 ? r[0] : 0;
                float top = r != null && r.length > 3 ? r[1] : 0;
                float width = r != null && r.length > 3 ? r[2] : data.sourceWidth;
                float height = r != null && r.length > 3 ? r[3] : data.sourceHeight;
                effects.add(new RainEffect(left, top, width, height, panel.rainWind));
            }
        }
    }

    private void checkAudioCues() {
        StoryPanel panel = director.current();
        if (panel == null) return;
        int line = typewriter.lineIndex();

        if (panel.audioCues != null) {
            for (StoryPanel.AudioCueData cue : panel.audioCues) {
                if (cue.line == line) audio.playCue(cue.sound);
            }
        }

        if ("locked_doors".equals(panel.id) && line == 2) {
            audio.doorLock();
        } else if ("end1".equals(panel.id) && line == 0) {
            audio.doorOpen();
        } else if ("end2".equals(panel.id) && line == 1) {
            audio.powerUp();
        }
    }

    @Override
    public void show() {
        if (failed) {
            Gdx.app.postRunnable(() -> {
                if (onComplete != null) onComplete.run();
                else game.setScreen(new LandingScreen(game));
            });
            return;
        }

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int key) {
                if (onCompleteCalled && key == Input.Keys.ESCAPE) {
                    if (session != null) session.close();
                    game.setScreen(new LandingScreen(game));
                    return true;
                }
                if (key == Input.Keys.SPACE) {
                    advanceText();
                    return true;
                }
                if (key == Input.Keys.F1) {
                    debug = !debug;
                    return true;
                }
                if (key == Input.Keys.F2) {
                    reloadStory();
                    return true;
                }
                if (key == Input.Keys.F3) {
                    skipPanel();
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int sx, int sy, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    Vector2 point = viewport.unproject(new Vector2(sx, sy));
                    if (isSkipIntroHit(point.x, point.y)) {
                        goToTitleCard();
                        return true;
                    }
                }
                return true;
            }

            @Override
            public boolean touchUp(int sx, int sy, int pointer, int button) {
                if (button != Input.Buttons.LEFT) return false;
                Vector2 point = viewport.unproject(new Vector2(sx, sy));
                if (isSkipIntroHit(point.x, point.y)) {
                    goToTitleCard();
                    return true;
                }
                advanceText();
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, .05f);
        screenElapsed += dt;
        if (session != null) {
            if (session instanceof HostSession h) h.tick(dt);
            else if (session instanceof RemoteSession r) r.tick();
        }
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (failed) return;

        skipHold.update(dt, Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
        if (skipHold.completed() && !skipTriggered) {
            skipTriggered = true;
            if (isEndingSequence) {
                beginEnding();
            } else {
                goToTitleCard();
            }
        }
        if (!Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) skipTriggered = false;

        if (ending) {
            endingElapsed += dt;
            if (endingElapsed >= .8f) {
                if (onComplete != null) {
                    if (!onCompleteCalled) {
                        onCompleteCalled = true;
                        onComplete.run();
                    }
                    if (game.getScreen() == this) {
                        drawWaitingForPartner();
                        return;
                    }
                } else {
                    SaveCheck.markStoryComplete();
                    game.setScreen(new RoomScreen(game, false));
                    return;
                }
            }
        }

        updateTransition(dt);
        if (!titleCard) {
            camera.update(dt);
            typewriter.update(dt);
            for (StoryEffect effect : effects) effect.update(dt);
        } else {
            titleElapsed += dt;
            typewriter.update(dt);
        }

        audio.update(dt, director.index(), titleCard, ending, typewriter.isTyping());
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);

        ScreenUtils.clear(Color.BLACK);
        if (titleCard) drawTitleCard();
        else drawPanel();

        if (transition != Transition.NONE) drawBlackOverlay(transitionAlpha());
        if (ending) drawBlackOverlay(Math.min(1, endingElapsed / .8f));
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) drawSkipProgress(skipHold.progress());
    }

    private void updateTransition(float dt) {
        if (transition == Transition.NONE) return;
        transitionLeft -= dt;
        if (transitionLeft > 0) return;
        if (transition == Transition.OUT) {
            if (!titleCard && director.advance()) {
                camera.reset();
                titleCard = false;
                typewriter.setLines(director.current().text);
                configureEffects();
                checkAudioCues();
            } else {
                titleCard = true;
                titleElapsed = 0;
                typewriter.setLines(data.titleCard.text);
                effects.clear();
            }
            transition = Transition.IN;
            transitionLeft = HALF_TRANSITION;
            transitionDuration = HALF_TRANSITION;
        } else {
            transition = Transition.NONE;
            transitionLeft = 0;
        }
    }

    private float transitionAlpha() {
        return transition == Transition.OUT
            ? 1 - Math.max(0, transitionLeft) / transitionDuration
            : Math.max(0, transitionLeft) / transitionDuration;
    }

    private void beginTransition() {
        typewriter.setLines(List.of());
        transition = Transition.OUT;
        transitionDuration = HALF_TRANSITION;
        transitionLeft = HALF_TRANSITION;
    }

    private void advanceText() {
        if (failed || ending || transition != Transition.NONE) return;
        int oldLine = typewriter.lineIndex();
        boolean done = typewriter.advance();
        if (!done && typewriter.lineIndex() != oldLine) {
            checkAudioCues();
        }
        if (done) {
            if (titleCard) {
                if (titleElapsed >= 3) beginEnding();
            } else {
                beginTransition();
            }
        }
    }

    private void beginEnding() {
        ending = true;
        endingElapsed = 0;
    }

    private void skipPanel() {
        if (transition == Transition.NONE && !titleCard) beginTransition();
    }

    private void goToTitleCard() {
        if (titleCard) return;
        director.restartAt(director.count() - 1);
        transition = Transition.OUT;
        transitionLeft = HALF_TRANSITION;
        transitionDuration = HALF_TRANSITION;
        typewriter.setLines(List.of());
        skipTriggered = true;
    }

    private void reloadStory() {
        for (Texture t : images) t.dispose();
        images.clear();
        failed = false;
        loadData();
        if (failed) {
            Gdx.app.postRunnable(() -> {
                if (onComplete != null) onComplete.run();
                else game.setScreen(new LandingScreen(game));
            });
            return;
        }
        if (textFont != null) textFont.dispose();
        if (titleFont != null) titleFont.dispose();
        createFonts();
        director = new StoryDirector(data.panels);
        titleCard = false;
        ending = false;
        camera.reset();
        typewriter.setLines(director.current().text);
        configureEffects();
        checkAudioCues();
        transition = Transition.IN;
        transitionLeft = .45f;
        transitionDuration = .45f;
    }

    private PanelTransform panelTransform(StoryPanel panel) {
        float bar = H * data.letterboxRatio, visibleHeight = H - 2 * bar;
        float base = Math.max(W / (float)data.sourceWidth, visibleHeight / (float)data.sourceHeight), scale = base * camera.zoom(panel);
        float imageWidth = data.sourceWidth * scale, imageHeight = data.sourceHeight * scale;
        float x = (W - imageWidth) / 2 - camera.panX(panel) * scale;
        float y = (H - imageHeight) / 2 + camera.panY(panel) * scale;
        return new PanelTransform(x, y, scale, data.sourceWidth, data.sourceHeight);
    }

    private void drawPanel() {
        StoryPanel panel = director.current();
        if (panel == null) return;
        Texture image = images.get(director.index());
        PanelTransform transform = panelTransform(panel);

        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(image, transform.imageX, transform.imageY, data.sourceWidth * transform.scale, data.sourceHeight * transform.scale);
        batch.end();

        for (StoryEffect effect : effects) effect.render(shapes, transform);
        if (panel.screenText != null) drawScreenText(panel.screenText, transform);
        if (data.vignette) drawVignette();
        if (data.filmGrain) drawFilmGrain();

        LetterboxBars.draw(shapes, W, H, data.letterboxRatio);
        drawCaption();
        drawPanelChrome();
        if (!isEndingSequence) drawSkipIntroButton();
    }

    private void drawWaitingForPartner() {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);

        ScreenUtils.clear(.015f, .02f, .035f, 1);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(.035f, .045f, .065f, 1));
        shapes.rect(460, 340, 1000, 400);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.ORANGE);
        shapes.rect(460, 340, 1000, 400);
        shapes.end();

        batch.begin();
        if (titleFont != null) {
            titleFont.setColor(Color.ORANGE);
            titleFont.draw(batch, "WAITING FOR PARTNER...", 520, 650);
        }

        if (textFont != null) {
            String partner = (session != null && !session.partnerName().isBlank()) ? session.partnerName() : "partner";
            textFont.setColor(new Color(.92f, .90f, .84f, 1));
            int dots = (int)(screenElapsed * 2) % 4;
            String dotStr = ".".repeat(dots);
            textFont.draw(batch, "Waiting for " + partner + " to finish story intro" + dotStr, 520, 560);
            textFont.setColor(Color.LIGHT_GRAY);
            textFont.draw(batch, "The lab puzzle will start as soon as both players are ready.", 520, 500);

            if (session != null && !session.partnerConnected()) {
                textFont.setColor(new Color(1f, .3f, .3f, 1));
                textFont.draw(batch, "Partner disconnected. Press ESC to return to menu.", 520, 420);
            }
        }
        batch.end();
    }

    private void drawVignette() {
        float bar = H * data.letterboxRatio;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Soft vignette gradients at viewport edges
        shapes.setColor(0, 0, 0, 0.25f);
        shapes.rect(0, bar, 120, H - 2 * bar);
        shapes.rect(W - 120, bar, 120, H - 2 * bar);
        shapes.end();
    }

    private void drawFilmGrain() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float bar = H * data.letterboxRatio;
        for (int i = 0; i < 60; i++) {
            float gx = grainRandom.nextFloat() * W;
            float gy = bar + grainRandom.nextFloat() * (H - 2 * bar);
            shapes.setColor(1, 1, 1, 0.025f + grainRandom.nextFloat() * 0.025f);
            shapes.rect(gx, gy, 1.5f, 1.5f);
        }
        shapes.end();
    }

    private void drawScreenText(StoryPanel.ScreenTextData text, PanelTransform transform) {
        float x = transform.x(text.x, text.y);
        float y = transform.y(text.x, text.y) - text.height * transform.scale;
        batch.begin();
        titleFont.setColor(.16f, .015f, .02f, 1);
        titleFont.draw(batch, text.headline, x, y + text.height * transform.scale * .68f, text.width * transform.scale, Align.center, true);
        textFont.setColor(.08f, .01f, .015f, 1);
        textFont.draw(batch, text.subline, x, y + text.height * transform.scale * .3f, text.width * transform.scale, Align.center, true);
        batch.end();
    }

    private void drawCaption() {
        float bar = H * data.letterboxRatio, x = 105, y = 18, width = W - 210, height = bar - 34;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(.025f, .032f, .04f, .94f);
        shapes.rect(x, y, width, height);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(.82f, .35f, .16f, .85f);
        shapes.rect(x, y, width, height);
        shapes.end();

        batch.begin();
        textFont.setColor(.94f, .92f, .84f, 1);
        if (typewriter.hasLine()) {
            String line = typewriter.currentLine();
            int n = typewriter.visibleCharacters();
            textFont.draw(batch, line.substring(0, Math.min(n, line.length())), x + 30, y + height - 28, width - 80, Align.left, true);
        }
        batch.end();

        if (typewriter.hasLine() && !typewriter.isTyping()) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1, .49f, .17f, 1);
            shapes.triangle(x + width - 46, y + 30, x + width - 30, y + 38, x + width - 46, y + 46);
            shapes.end();
        }
    }

    private void drawPanelChrome() {
        float bar = H * data.letterboxRatio;
        batch.begin();
        textFont.setColor(.8f, .8f, .75f, .68f);

        if (isEndingSequence) {
            // Ending panel counter at top right
            textFont.draw(batch, (director.index() + 1) + "/" + director.count(), W - 140, H - bar + 46);
        } else {
            // Intro panel counter at top left
            textFont.draw(batch, (director.index() + 1) + "/" + director.count(), 60, H - bar + 46);
        }

        // Controls hint: panel 1 only, first 5s, only when typewriter is idle
        if (director.index() == 0 && screenElapsed < 5 && !typewriter.isTyping()) {
            textFont.setColor(.5f, .5f, .48f, .65f);
            String hint = isEndingSequence
                ? "SPACE / CLICK: CONTINUE     ESC (HOLD): SKIP"
                : "SPACE / CLICK: CONTINUE     ESC (HOLD): SKIP INTRO";
            textFont.draw(batch, hint, 165, 30);
        }
        batch.end();

        if (debug) drawDebug();
    }

    private void drawSkipIntroButton() {
        float x = skipBtnX(), y = skipBtnY(), width = SKIP_BTN_W, height = SKIP_BTN_H;
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean hover = mouse.x >= x && mouse.x <= x + width && mouse.y >= y && mouse.y <= y + height;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(hover ? new Color(.08f, .10f, .14f, .95f) : new Color(.025f, .032f, .04f, .88f));
        shapes.rect(x, y, width, height);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(hover ? Color.ORANGE : new Color(.82f, .35f, .16f, .9f));
        shapes.rect(x, y, width, height);
        shapes.end();

        batch.begin();
        textFont.setColor(hover ? Color.ORANGE : new Color(.94f, .92f, .84f, 1));
        GlyphLayout l = new GlyphLayout(textFont, "SKIP INTRO");
        textFont.draw(batch, "SKIP INTRO", x + (width - l.width) / 2, y + (height + l.height) / 2);
        batch.end();
    }

    private void drawTitleCard() {
        ScreenUtils.clear(.012f, .02f, .032f, 1);

        batch.begin();
        titleFont.setColor(.96f, .93f, .86f, 1);
        textFont.setColor(1, .48f, .18f, 1);

        List<String> lines = data.titleCard.text;
        for (int i = 0; i < lines.size(); i++) {
            String shown = "";
            if (i < typewriter.lineIndex()) shown = lines.get(i);
            else if (i == typewriter.lineIndex()) shown = lines.get(i).substring(0, Math.min(typewriter.visibleCharacters(), lines.get(i).length()));
            BitmapFont f = i == 0 ? titleFont : textFont;
            GlyphLayout l = new GlyphLayout(f, shown);
            f.draw(batch, shown, (W - l.width) / 2, 625 - i * 75);
        }

        if (titleElapsed >= 3 && typewriter.lineIndex() >= lines.size()) {
            float pulse = 0.6f + 0.4f * (float)Math.sin(screenElapsed * 4.0f);
            textFont.setColor(1, .48f, .18f, pulse);
            GlyphLayout promptLayout = new GlyphLayout(textFont, data.titleCard.prompt);
            textFont.draw(batch, data.titleCard.prompt, (W - promptLayout.width) / 2, 410);
        }
        batch.end();

        if (debug) drawDebug();
    }

    private void drawDebug() {
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        PanelTransform tr = titleCard ? null : panelTransform(director.current());
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.GREEN);

        if (tr != null) {
            shapes.rect(tr.x(0, 0), tr.y(0, 0), data.sourceWidth * tr.scale, -data.sourceHeight * tr.scale);
            StoryPanel p = director.current();
            if (p.screenText != null) {
                shapes.setColor(Color.ORANGE);
                shapes.rect(tr.x(p.screenText.x, p.screenText.y), tr.y(p.screenText.x, p.screenText.y), p.screenText.width * tr.scale, -p.screenText.height * tr.scale);
            }
            if (p.effects != null) {
                shapes.setColor(Color.CYAN);
                for (StoryPanel.EffectConfig eff : p.effects) {
                    if (eff.region != null && eff.region.length >= 4) {
                        float rx1 = Math.min(eff.region[0], eff.region[2]);
                        float ry1 = Math.min(eff.region[1], eff.region[3]);
                        float rx2 = Math.max(eff.region[0], eff.region[2]);
                        float ry2 = Math.max(eff.region[1], eff.region[3]);
                        float sx = tr.x(rx1 * tr.sourceWidth(), ry1 * tr.sourceHeight());
                        float sy = tr.y(rx1 * tr.sourceWidth(), ry1 * tr.sourceHeight());
                        float sw = (rx2 - rx1) * tr.sourceWidth() * tr.scale;
                        float sh = (ry2 - ry1) * tr.sourceHeight() * tr.scale;
                        shapes.rect(sx, sy, sw, -sh);
                    }
                    if (eff.point != null && eff.point.length >= 2) {
                        float cx = tr.x(eff.point[0] * tr.sourceWidth(), eff.point[1] * tr.sourceHeight());
                        float cy = tr.y(eff.point[0] * tr.sourceWidth(), eff.point[1] * tr.sourceHeight());
                        shapes.circle(cx, cy, 8);
                    }
                    if (eff.points != null) {
                        for (float[] pt : eff.points) {
                            float cx = tr.x(pt[0] * tr.sourceWidth(), pt[1] * tr.sourceHeight());
                            float cy = tr.y(pt[0] * tr.sourceWidth(), pt[1] * tr.sourceHeight());
                            shapes.circle(cx, cy, 6);
                        }
                    }
                }
            }
        }
        shapes.end();

        batch.begin();
        textFont.setColor(Color.GREEN);
        float normX = tr != null ? (mouse.x - tr.imageX) / (data.sourceWidth * tr.scale) : 0;
        float normY = tr != null ? 1f - (mouse.y - tr.imageY) / (data.sourceHeight * tr.scale) : 0;
        textFont.draw(batch, String.format("PIXEL %.0f, %.0f  NORM %.3f, %.3f", mouse.x, mouse.y, Math.max(0, Math.min(1, normX)), Math.max(0, Math.min(1, normY))), 40, H - 35);
        batch.end();
    }

    private void drawSkipProgress(float progress) {
        if (progress <= 0) return;
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(.88f, .62f, .35f, .82f);
        shapes.arc(W - 70, H - 65, 20, 90, 360 * progress);
        shapes.end();
        batch.begin();
        textFont.setColor(.75f, .72f, .67f, .85f);
        textFont.draw(batch, "SKIP", W - 118, H - 119);
        batch.end();
    }

    private void drawBlackOverlay(float alpha) {
        if (alpha <= 0) return;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, Math.min(1, alpha));
        shapes.rect(0, 0, W, H);
        shapes.end();
    }

    public static final class PanelTransform {
        public final float imageX, imageY, scale;
        private final int sourceWidth, sourceHeight;

        public PanelTransform(float imageX, float imageY, float scale, int sourceWidth, int sourceHeight) {
            this.imageX = imageX;
            this.imageY = imageY;
            this.scale = scale;
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
        }

        public int sourceWidth() { return sourceWidth; }
        public int sourceHeight() { return sourceHeight; }
        public float x(float sourceX, float sourceY) { return imageX + sourceX * scale; }
        public float y(float sourceX, float sourceY) { return imageY + (sourceHeight - sourceY) * scale; }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override
    public void dispose() {
        for (Texture texture : images) texture.dispose();
        batch.dispose();
        shapes.dispose();
        if (textFont != null) textFont.dispose();
        if (titleFont != null) titleFont.dispose();
        audio.close();
    }
}
