package edu.brandeis.cosi103a.groupb.Events;

public final class GameEvent implements Event {
    private final String description;

    public GameEvent(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}