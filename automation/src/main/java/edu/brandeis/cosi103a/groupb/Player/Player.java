package edu.brandeis.cosi103a.groupb.Player;

import java.util.List;
import java.util.Optional;

import edu.brandeis.cosi103a.groupb.Decisions.Decision;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Game.GameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameState;


public interface Player {
    String getName();
    Decision makeDecision(GameState state, List<Decision> options);
    DiscardDeck getDiscardDeck();
    DrawDeck getDrawDeck();

    Optional<GameObserver> getObserver();

    class ScorePair {
        public final Player player;
        public final int score; //Victory points /

        public ScorePair(Player player, int score) {
            this.player = player;
            this.score = score;
        }

        public int getScore() {
            return score;
        }
    }
}
