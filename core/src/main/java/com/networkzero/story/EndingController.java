package com.networkzero.story;

import com.badlogic.gdx.Game;

/** Starts the ending story sequence from ending.json and routes to the completion callback. */
public final class EndingController {
    private EndingController() {}

    public static void playEnding(Game game, Runnable onComplete) {
        game.setScreen(new StoryScreen(game, "data/ending.json", onComplete));
    }
}
