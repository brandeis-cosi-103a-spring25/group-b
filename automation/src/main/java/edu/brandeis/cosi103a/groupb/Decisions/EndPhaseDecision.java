package edu.brandeis.cosi103a.groupb.Decisions;

import edu.brandeis.cosi103a.groupb.Game.GameState;

/**
 * Represents a decision to end a phase.
 */
public final class EndPhaseDecision implements Decision {
    private final GameState.TurnPhase phase; //MONEY, BUY, CLEANUP

    public EndPhaseDecision(GameState.TurnPhase phase) {
        this.phase = phase;
    }

    public GameState.TurnPhase getPhase() {
        return phase;
    }

    @Override
    public String getDescription() {
        return "End " + phase + " phase";
    }
}
