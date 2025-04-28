package edu.brandeis.cosi103a.groupb.Server.model;

import java.util.*;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi.atg.api.event.*;

public class DecisionRequest {
    private GameState state;
    private List<Decision> options;
    private Optional<Event> reason;
    private String player_uuid;

    public DecisionRequest() {}

    public DecisionRequest(GameState state, List<Decision> options, Optional<Event> reason) {
        this.state = state;
        this.options = options;
        this.reason = reason;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public List<Decision> getOptions() {
        return options;
    }

    public void setOptions(List<Decision> options) {
        this.options = options;
    }

    public Optional<Event> getReason() {
        return reason;
    }

    public void setReason(Optional<Event> reason) {
        this.reason = reason;
    }

    public String getPlayer_uuid() {
        return player_uuid;
    }

    public void setPlayer_uuid(String player_uuid) {
        this.player_uuid = player_uuid;
    }
}