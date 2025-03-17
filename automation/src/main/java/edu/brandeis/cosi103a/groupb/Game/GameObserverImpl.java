package edu.brandeis.cosi103a.groupb.Game;

import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.event.Event;

public class GameObserverImpl implements GameObserver {
    
    @Override
    public void notifyEvent(GameState state, Event event) {
        System.out.println("Game Event: " + event.getDescription());
        System.out.println("Current Game State: " + state);
    }
}
