package edu.brandeis.cosi103a.groupb.Game;

import edu.brandeis.cosi103a.groupb.Events.Event;

/**
 * Interface for observing game events.
 */
public interface GameObserver {
    void notifyEvent(GameState state, Event event);
}
