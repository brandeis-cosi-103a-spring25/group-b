package edu.brandeis.cosi103a.groupb;

import java.util.List;
import java.util.Optional;

import edu.brandeis.cosi103a.groupb.Decisions.*;


public interface Player {
    String getName();
    Decision makeDecision(GameState state, List<Decision> options);

    Optional<GameObserver> getObserver();

    class ScorePair {
        public final Player player;
        public final int score;

        public ScorePair(Player player, int score) {
            this.player = player;
            this.score = score;
        }

        public int getScore() {
            return score;
        }
    }
}
