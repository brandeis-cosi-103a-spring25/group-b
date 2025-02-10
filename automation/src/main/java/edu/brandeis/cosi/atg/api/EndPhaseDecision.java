package edu.brandeis.cosi.atg.api.decisions;

import edu.brandeis.cosi.atg.api.GameState;

public final class EndPhaseDecision implements Decision {
    private final GameState.TurnPhase phase;

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

    @Override
    public String toString() {
        return "EndPhaseDecision{" +
                "phase=" + phase +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndPhaseDecision that = (EndPhaseDecision) o;

        return phase == that.phase;
    }

    @Override
    public int hashCode() {
        return phase.hashCode();
    }
}
