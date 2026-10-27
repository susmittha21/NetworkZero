package com.networkzero.engine;

import java.nio.file.Path;

/** Per-process data root; unset profile preserves the original ~/.networkzero layout. */
public final class ProfilePaths {
    private static volatile String profile="";
    private ProfilePaths(){}
    public static void setProfile(String p){profile=p==null?"":p.replaceAll("[^A-Za-z0-9_-]","");}
    public static String profile(){return profile;}
    public static Path root(){Path p=Path.of(System.getProperty("user.home"),".networkzero");return profile.isBlank()?p:p.resolve(profile);}
    public static Path saves(){return root().resolve("saves");}
}
