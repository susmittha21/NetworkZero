package com.networkzero.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public final class PlaceholderScreen implements Screen {
    private static final float W=1920,H=1080;
    private final Game game;private final FitViewport viewport=new FitViewport(W,H);private final SpriteBatch batch=new SpriteBatch();private final ShapeRenderer shapes=new ShapeRenderer();private final Texture background;private final BitmapFont font;private final BitmapFont small;
    public PlaceholderScreen(Game game){this.game=game;background=Gdx.files.internal("landing/landing_bg.png").exists()?new Texture(Gdx.files.internal("landing/landing_bg.png")):null;if(background!=null)background.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);FreeTypeFontGenerator g=new FreeTypeFontGenerator(Gdx.files.internal("fonts/Silkscreen-Bold.ttf"));FreeTypeFontGenerator.FreeTypeFontParameter p=new FreeTypeFontGenerator.FreeTypeFontParameter();p.size=38;p.magFilter=Texture.TextureFilter.Nearest;p.minFilter=Texture.TextureFilter.Nearest;font=g.generateFont(p);p.size=20;small=g.generateFont(p);g.dispose();}
    private void back(){game.setScreen(new LandingScreen(game));}
    @Override public void render(float delta){ScreenUtils.clear(.015f,.022f,.032f,1);viewport.apply();batch.setProjectionMatrix(viewport.getCamera().combined);batch.begin();if(background!=null){float s=Math.max(W/background.getWidth(),H/background.getHeight());batch.setColor(.27f,.31f,.34f,1);batch.draw(background,(W-background.getWidth()*s)/2,(H-background.getHeight()*s)/2,background.getWidth()*s,background.getHeight()*s);}batch.end();shapes.setProjectionMatrix(viewport.getCamera().combined);shapes.begin(ShapeRenderer.ShapeType.Filled);shapes.setColor(.72f,.77f,.8f,.83f);shapes.rect(0,0,W,H);shapes.end();batch.begin();font.setColor(.96f,.93f,.86f,1);font.draw(batch,"ROOM 2: SERVER ROOM",W/2-625,H/2+70);font.setColor(1,.48f,.18f,1);small.setColor(1,.48f,.18f,1);small.draw(batch,"COMING NEXT",W/2-98,H/2+10);batch.end();shapes.begin(ShapeRenderer.ShapeType.Filled);shapes.setColor(.04f,.05f,.06f,.96f);shapes.rect(W/2-205,280,410,74);shapes.end();shapes.begin(ShapeRenderer.ShapeType.Line);shapes.setColor(.9f,.38f,.15f,1);shapes.rect(W/2-205,280,410,74);shapes.end();batch.begin();small.setColor(.94f,.91f,.84f,1);small.draw(batch,"BACK TO MENU",W/2-97,326);batch.end();if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))back();}
    @Override public void resize(int width,int height){viewport.update(width,height,true);} @Override public void show(){Gdx.input.setInputProcessor(new InputAdapter(){@Override public boolean keyDown(int key){if(key==Input.Keys.ESCAPE){back();return true;}return false;}@Override public boolean touchUp(int sx,int sy,int pointer,int button){Vector2 pos=viewport.unproject(new Vector2(sx,sy));if(pos.x>=W/2-205&&pos.x<=W/2+205&&pos.y>=280&&pos.y<=354){back();return true;}return false;}});} @Override public void hide(){} @Override public void pause(){} @Override public void resume(){}
    @Override public void dispose(){batch.dispose();shapes.dispose();if(background!=null)background.dispose();font.dispose();small.dispose();}
}
