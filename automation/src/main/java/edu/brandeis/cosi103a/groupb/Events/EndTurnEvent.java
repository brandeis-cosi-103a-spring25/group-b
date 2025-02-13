package edu.brandeis.cosi103a.groupb.Events;

/**
 * Represents an event where a player ends their turn.
 */
public final class EndTurnEvent implements Event {
    private final String playerName;

    public EndTurnEvent(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String getDescription() {
        return playerName + " ended their turn";
    }
}
