package edu.brandeis.cosi103a.groupb.Server.model;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.event.*;

public class LogEventRequest {
    private Event event;
    private String player_uuid;
    private GameState state;

    public LogEventRequest() {}

    public LogEventRequest(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getPlayer_uuid() {
        return player_uuid;
    }

    public void setPlayer_uuid(String player_uuid) {
        this.player_uuid = player_uuid;
    }

    public GameState getState() {
        return this.state;
    }

    public void setState(GameState state) {
        this.state = state;
    }
}
