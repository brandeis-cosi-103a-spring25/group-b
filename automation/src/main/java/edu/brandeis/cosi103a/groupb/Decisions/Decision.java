package edu.brandeis.cosi103a.groupb.Decisions;

public sealed interface Decision permits BuyDecision, EndPhaseDecision, PlayCardDecision {
    String getDescription();
}