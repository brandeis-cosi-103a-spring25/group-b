package edu.brandeis.cosi103a.groupb.Server.model;

import edu.brandeis.cosi.atg.api.decisions.*;

public class DecisionResponse {
    private Decision decision;

    public DecisionResponse() {}

    public DecisionResponse(Decision decision) {
        this.decision = decision;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }
}