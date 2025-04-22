package edu.brandeis.cosi103a.groupb.Game;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.event.Event;

/**
 * A simple game observer that logs events to the console.
 * This implementation displays all game events to standard output
 * for the user to track game progress.
 */
public class ConsoleGameObserver implements GameObserver {
    /**
     * Receive and display game events.
     *
     * @param state The current game state
     * @param event The event that occurred
     */
    @Override
    public void notifyEvent(GameState state, Event event) {
        System.out.println("[LOG] " + event.getDescription() + "\n");
    }
}
