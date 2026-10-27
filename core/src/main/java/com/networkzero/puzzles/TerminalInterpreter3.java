package com.networkzero.puzzles;

/** Deterministic interpreter for control terminal (retained as alias for TerminalInterpreter2). */
public final class TerminalInterpreter3 {
    private TerminalInterpreter3() {}
    public static boolean validIpv4(String value) { return TerminalInterpreter2.validIpv4(value); }
    public static Result execute(String command, boolean logSolved, boolean attackerBlocked,
                                 boolean cctvSolved, boolean portsSolved, boolean lockdownReleased) {
        TerminalInterpreter2.Result r = TerminalInterpreter2.execute(command, logSolved, attackerBlocked, cctvSolved, portsSolved, lockdownReleased);
        return new Result(r.output(), r.attackerBlocked(), r.lockdownReleased());
    }
    public record Result(String output, boolean attackerBlocked, boolean lockdownReleased) {}
}
