package edu.brandeis.cosi.atg.api;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.event.GameObserver;

import java.util.List;
import java.util.Optional;

public interface Player {
    String getName();
    Optional<GameObserver> getObserver();
    Decision makeDecision(GameState state, ImmutableList<Decision> options);
    List<Card> getHand();
    List<Card> getDeck();
    List<Card> getDiscardPile();
    void drawHand();
    void discardHand();
    void addCardToDeck(Card card);
    int getAvailableCoins();

    class ScorePair {
        private final Player player;
        private final int score;

        public ScorePair(Player player, int score) {
            this.player = player;
            this.score = score;
        }

        public Player getPlayer() {
            return player;
        }

        public int getScore() {
            return score;
        }
    }
}