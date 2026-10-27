package com.networkzero.puzzles;

import java.util.Map;

/** Loads the tunable Room 2 answers and hint text (retained as alias for Room2Config). */
public final class Room3Config {
    private Room3Config(){}
    public static synchronized void load(){ Room2Config.load(); }
    public static String cabinetCode(){ return Room2Config.cabinetCode(); }
    public static String doorCode(){ return Room2Config.doorCode(); }
    public static String attacker(){ return Room2Config.attacker(); }
    public static int differentPorts(){ return Room2Config.differentPorts(); }
    public static int tamperedCamera(){ return Room2Config.tamperedCamera(); }
    public static Map<String,String> servicePorts(){ return Room2Config.servicePorts(); }
    public static String hint(String id,int tier){ return Room2Config.hint(id,tier); }
}
