package com.networkzero;

import com.badlogic.gdx.Game;import com.badlogic.gdx.Gdx;import com.badlogic.gdx.graphics.GL20;import com.networkzero.screens.LandingScreen;

public final class NetworkZeroGame extends Game {
    @Override public void setScreen(com.badlogic.gdx.Screen next){com.badlogic.gdx.Screen previous=getScreen();super.setScreen(next);if(previous!=null&&previous!=next)Gdx.app.postRunnable(previous::dispose);}
    @Override public void create(){Gdx.gl.glClearColor(0,0,0,1);setScreen(new LandingScreen(this));}
}
