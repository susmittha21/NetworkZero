package com.networkzero.puzzles;

/** Pure checker for the Room 2 packet log answer. */
public final class LogChecker {
    public static final String ATTACKER = "10.20.30.77";
    public static final int DISTINCT_PORTS = 7;
    private LogChecker() {}

    public static Result check(String ip, String count) {
        return check(ip,count,ATTACKER,DISTINCT_PORTS);
    }
    public static Result check(String ip,String count,String expectedIp,int expectedCount) {
        String normalizedIp = ip == null ? "" : ip.trim();
        String normalizedCount = count == null ? "" : count.trim();
        int parsed;
        try { parsed = Integer.parseInt(normalizedCount); }
        catch (NumberFormatException ex) { return new Result(false, false); }
        return new Result(expectedIp.equals(normalizedIp), parsed == expectedCount);
    }

    public record Result(boolean correctIp, boolean correctCount) {
        public boolean correct() { return correctIp && correctCount; }
    }
}
