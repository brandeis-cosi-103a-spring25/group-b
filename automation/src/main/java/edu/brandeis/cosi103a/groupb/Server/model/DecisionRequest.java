package edu.brandeis.cosi103a.groupb.Server.model;

import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.event.Event;

import java.util.List;
import java.util.Optional;

public class DecisionRequest {
    private GameState state;
    private List<Decision> options;
    private Optional<Event> reason;

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
}