package edu.brandeis.cosi103a.groupb.Server.model;

import edu.brandeis.cosi.atg.api.event.Event;

public class LogEventRequest {
    private Event event;

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
}
