package edu.brandeis.cosi.atg.api;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.event.GameObserver;
import edu.brandeis.cosi.atg.api.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        List<Player> players = new ArrayList<>();
        players.add(new SimplePlayer("Player 1"));
        players.add(new SimplePlayer("Player 2"));

        GameObserver observer = new SimpleGameObserver();
        GameEngine gameEngine = new GameEngine(players, observer);

        try {
            ImmutableList<Player.ScorePair> scores = gameEngine.play();
            System.out.println("Game Over! Final Scores:");
            for (Player.ScorePair scorePair : scores) {
                System.out.println(scorePair.getPlayer().getName() + ": " + scorePair.getScore());
            }
        } catch (PlayerViolationException e) {
            System.err.println("A player violated the game rules: " + e.getMessage());
        }
    }

    private static class SimplePlayer implements Player {
        private final String name;
        private final List<Card> hand = new ArrayList<>();
        private final List<Card> deck = new ArrayList<>();
        private final List<Card> discardPile = new ArrayList<>();

        public SimplePlayer(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Optional<GameObserver> getObserver() {
            return Optional.empty();
        }

        @Override
        public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
            return options.get(0); // Simplified decision-making for testing
        }

        @Override
        public List<Card> getHand() {
            return hand;
        }

        @Override
        public List<Card> getDeck() {
            return deck;
        }

        @Override
        public List<Card> getDiscardPile() {
            return discardPile;
        }

        @Override
        public void drawHand() {
            // Simplified draw hand logic for testing
            hand.clear();
            for (int i = 0; i < 5; i++) {
                if (!deck.isEmpty()) {
                    hand.add(deck.remove(0));
                }
            }
        }

        @Override
        public void discardHand() {
            discardPile.addAll(hand);
            hand.clear();
        }

        @Override
        public void addCardToDeck(Card card) {
            deck.add(card);
        }

        @Override
        public int getAvailableCoins() {
            return 0; // Simplified for testing
        }
    }

    private static class SimpleGameObserver implements GameObserver {
        @Override
        public void notifyEvent(GameState state, Event event) {
            System.out.println("Event: " + event.getDescription());
        }
    }
}
