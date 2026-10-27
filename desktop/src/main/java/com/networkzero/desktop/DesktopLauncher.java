package com.networkzero.desktop;

import com.badlogic.gdx.backends.lwjgl3.*;import com.networkzero.NetworkZeroGame;import com.networkzero.engine.ProfilePaths;

public final class DesktopLauncher {
    public static void main(String[] args){String profile="";for(String arg:args)if(arg.startsWith("--profile="))profile=arg.substring("--profile=".length());ProfilePaths.setProfile(profile);Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();c.setTitle("NETWORK ZERO: The Last Lab Session"+(profile.isBlank()?"":" - "+profile));c.setWindowedMode(1280,720);c.setResizable(true);c.setForegroundFPS(60);new Lwjgl3Application(new NetworkZeroGame(),c);}
}
