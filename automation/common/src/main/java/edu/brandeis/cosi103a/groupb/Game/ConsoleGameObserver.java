package edu.brandeis.cosi103a.groupb.Game;

import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.event.Event;

/**
 * A simple game observer that logs events to the console.
 */
public class ConsoleGameObserver implements GameObserver {
    @Override
    public void notifyEvent(GameState state, Event event) {
        System.out.println("[LOG] " + event.getDescription() + "\n");
    }
}
