package edu.brandeis.cosi.atg.api.event;

import edu.brandeis.cosi.atg.api.GameState;

public interface GameObserver {
    void notifyEvent(GameState state, Event event);
}