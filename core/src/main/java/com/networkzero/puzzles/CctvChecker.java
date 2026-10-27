package com.networkzero.puzzles;

/** Room 2 replayed camera validation. */
public final class CctvChecker {
    private CctvChecker() {}
    public static boolean isTampered(int cameraNumber) { return cameraNumber == 6; }
    public static boolean isValidCamera(int cameraNumber) { return cameraNumber >= 1 && cameraNumber <= 9; }
}
