package edu.brandeis.cosi.atg.api;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi.atg.api.event.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class GameEngineTest {
    private GameEngine gameEngine;
    private List<Player> players;
    private GameObserver observer;

    @Before
    public void setUp() {
        players = new ArrayList<>();
        players.add(new TestPlayer("Player 1"));
        players.add(new TestPlayer("Player 2"));
        observer = new TestGameObserver();
        gameEngine = new GameEngine(players, observer);
    }

    @Test
    public void testInitializeGameDeck() {
        GameDeck gameDeck = gameEngine.initializeGameDeck();
        assertEquals(60, gameDeck.getNumAvailable(Card.Type.BITCOIN));
        assertEquals(40, gameDeck.getNumAvailable(Card.Type.ETHEREUM));
        assertEquals(30, gameDeck.getNumAvailable(Card.Type.DOGECOIN));
        assertEquals(14, gameDeck.getNumAvailable(Card.Type.METHOD));
        assertEquals(8, gameDeck.getNumAvailable(Card.Type.MODULE));
        assertEquals(8, gameDeck.getNumAvailable(Card.Type.FRAMEWORK));
    }

    @Test
    public void testDealStartingHands() {
        gameEngine.dealStartingHands();
        for (Player player : players) {
            assertEquals(10, player.getDeck().size());
            assertEquals(7, player.getDeck().stream().filter(card -> card.getType() == Card.Type.BITCOIN).count());
            assertEquals(3, player.getDeck().stream().filter(card -> card.getType() == Card.Type.METHOD).count());
        }
    }

    @Test
    public void testPlay() throws PlayerViolationException {
        ImmutableList<Player.ScorePair> scores = gameEngine.play();
        assertNotNull(scores);
        assertEquals(2, scores.size());
    }

    // Additional tests for edge cases and specific scenarios can be added here

    private static class TestPlayer implements Player {
        private final String name;
        private final List<Card> hand = new ArrayList<>();
        private final List<Card> deck = new ArrayList<>();
        private final List<Card> discardPile = new ArrayList<>();

        public TestPlayer(String name) {
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

    private static class TestGameObserver implements GameObserver {
        @Override
        public void notifyEvent(GameState state, Event event) {
            // Simplified event handling for testing
        }
    }
}
