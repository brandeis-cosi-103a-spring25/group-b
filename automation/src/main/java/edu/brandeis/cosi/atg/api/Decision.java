package edu.brandeis.cosi.atg.api.decisions;

public sealed interface Decision permits BuyDecision, EndPhaseDecision, PlayCardDecision {
    String getDescription();
}
