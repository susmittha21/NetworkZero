package com.networkzero.puzzles;

import java.util.Locale;
import java.util.regex.Pattern;

/** Deterministic interpreter for Room 2's control terminal. */
public final class TerminalInterpreter2 {
    private static final Pattern IPV4 = Pattern.compile("^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$");
    private TerminalInterpreter2() {}
    public static boolean validIpv4(String value) { return value != null && IPV4.matcher(value.trim()).matches(); }
    public static Result execute(String command, boolean logSolved, boolean attackerBlocked,
                                 boolean cctvSolved, boolean portsSolved, boolean lockdownReleased) {
        String line = command == null ? "" : command.trim();
        if (line.isEmpty()) return new Result("", attackerBlocked, lockdownReleased);
        String lower = line.toLowerCase(Locale.ROOT);
        if (lower.equals("help")) return new Result("help | status | block <ip> | release | clear", attackerBlocked, lockdownReleased);
        if (lower.equals("clear")) return new Result("", attackerBlocked, lockdownReleased);
        if (lower.equals("status")) return new Result("LOCKDOWN: " + (lockdownReleased ? "RELEASED" : "ACTIVE") + "\nTHREAT: " + (attackerBlocked ? "ISOLATED (10.20.30.77)" : "UNIDENTIFIED"), attackerBlocked, lockdownReleased);
        if (lower.startsWith("block ")) {
            String ip = line.substring(6).trim();
            if (!validIpv4(ip)) return new Result("Invalid address.", attackerBlocked, lockdownReleased);
            if (!logSolved) return new Result("Evidence incomplete. Analyse the packet log first.", attackerBlocked, lockdownReleased);
            if (!Room2Config.attacker().equals(ip)) return new Result("No malicious traffic from that address. Block rejected.", attackerBlocked, lockdownReleased);
            return new Result("Attacker "+Room2Config.attacker()+" isolated.", true, lockdownReleased);
        }
        if (lower.equals("release")) {
            if (!attackerBlocked) return new Result("Cannot release: threat still active.", attackerBlocked, lockdownReleased);
            if (!cctvSolved) return new Result("Cannot release: camera integrity not verified.", attackerBlocked, lockdownReleased);
            if (!portsSolved) return new Result("Cannot release: service register not verified.", attackerBlocked, lockdownReleased);
            return new Result("Lockdown release initiated.\nRelease token: 2\nTake the token to the exit door.", attackerBlocked, true);
        }
        return new Result("Unknown command. Type help.", attackerBlocked, lockdownReleased);
    }
    public record Result(String output, boolean attackerBlocked, boolean lockdownReleased) {}
}
