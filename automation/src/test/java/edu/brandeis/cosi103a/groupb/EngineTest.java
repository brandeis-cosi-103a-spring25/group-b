package edu.brandeis.cosi103a.groupb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue; //Use java reflection to test private method
import org.junit.jupiter.api.Test;

import edu.brandeis.cosi103a.groupb.Cards.Card;
import edu.brandeis.cosi103a.groupb.Decks.GameDeck;
import edu.brandeis.cosi103a.groupb.Decks.PlayerDeck;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Game.GameObserver;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;
import edu.brandeis.cosi103a.groupb.Player.Player;

public class EngineTest {
    private Player player1 = new BigMoneyPlayer("Nancy");
    private Player player2 = new HumanPlayer("Abby");
    private GameObserver observer = new ConsoleGameObserver();
    private GameEngine gameEngine = new GameEngine(player1, player2, observer);

    @Test
    void testInitializeDeck() throws Exception {
        Method initializeDeck = gameEngine.getClass().getDeclaredMethod("initializeDeck");
        initializeDeck.setAccessible(true);
        GameDeck deck = (GameDeck) initializeDeck.invoke(gameEngine);

        assertEquals(60, deck.getNumAvailable(Card.Type.BITCOIN));
        assertEquals(40, deck.getNumAvailable(Card.Type.ETHEREUM));
        assertEquals(30, deck.getNumAvailable(Card.Type.DOGECOIN));
        assertEquals(14, deck.getNumAvailable(Card.Type.METHOD));
        assertEquals(8, deck.getNumAvailable(Card.Type.MODULE));
        assertEquals(8, deck.getNumAvailable(Card.Type.FRAMEWORK));
    }

    @Test
    void testInitializeGameState() throws Exception {
        Method initializeGameState = gameEngine.getClass().getDeclaredMethod("initializeGameState", Player.class);
        initializeGameState.setAccessible(true);
        initializeGameState.invoke(gameEngine, player1);
        initializeGameState.invoke(gameEngine, player2);

        PlayerDeck discardDeck = player1.getDiscardDeck();
        PlayerDeck drawDeck = player2.getDrawDeck();

        assertTrue(discardDeck.size() == 0);
        assertTrue(drawDeck.size() == 10);
    }

    // Only testing Big Money Player here manually with the initial simple conditions for the first 1-2 rounds to check if conditions are set up correctly
    @Test
    void testPhasesAgainAndAgain() throws Exception {
        Field mainDeck = gameEngine.getClass().getDeclaredField("deck"); 
        mainDeck.setAccessible(true);

        Method initializeGameState = gameEngine.getClass().getDeclaredMethod("initializeGameState", Player.class);
        initializeGameState.setAccessible(true);
        initializeGameState.invoke(gameEngine, player1);

        Method moneyPhase = gameEngine.getClass().getDeclaredMethod("handleMoneyPhase", Player.class);
        moneyPhase.setAccessible(true);
        moneyPhase.invoke(gameEngine, player1);

        assertTrue(player1.getDrawDeck().size() == 5);

        Method buyPhase = gameEngine.getClass().getDeclaredMethod("handleBuyPhase", Player.class);
        buyPhase.setAccessible(true);
        buyPhase.invoke(gameEngine, player1);

        assertEquals(player1.getDiscardDeck().size(), 1); //Bought card directly goes into the discard deck.

        Method cleanUpPhase = gameEngine.getClass().getDeclaredMethod("handleCleanupPhase", Player.class);
        cleanUpPhase.setAccessible(true);
        cleanUpPhase.invoke(gameEngine, player1);

        assertEquals(player1.getDiscardDeck().size(), 6);
        assertEquals(player1.getDrawDeck().size(), 5);

        // Check if all cards in hand and played cards moved to discard
        int handSize = gameEngine.getGameState().getCurrentPlayerHand().getUnplayedCards().size();
        int playedCardsSize = gameEngine.getGameState().getCurrentPlayerHand().getPlayedCards().size();
        assertEquals(0, handSize + playedCardsSize); // All played/unplayed cards should move to discard;

        // Verify draw deck has correct count (10-5)
        assertEquals(5, player1.getDrawDeck().size()); // Draw deck should have 5 cards after cleanup;

        //-----------------------------------------------------That is turn 1-----------------------------------------------------------

        moneyPhase.invoke(gameEngine, player1);
        assertTrue(player1.getDrawDeck().size() == 5-5);

        buyPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 1+1+5); // 2 bought cards, 5 cards from last round

        cleanUpPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 6+6); // 6 cards from each round
        assertEquals(player1.getDrawDeck().size(), 5-5);

        // Check if all cards in hand and played cards moved to discard
        int handSize2 = gameEngine.getGameState().getCurrentPlayerHand().getUnplayedCards().size();
        int playedCardsSize2 = gameEngine.getGameState().getCurrentPlayerHand().getPlayedCards().size();
        assertEquals(0, handSize2 + playedCardsSize2); // All played/unplayed cards should move to discard;

        // Verify draw deck has correct count (10-5)
        assertEquals(5-5, player1.getDrawDeck().size()); // Draw deck should -5 cards after this cleanup;

        //-----------------------------------------------------That is turn 2-----------------------------------------------------------
        //Testing if the reshuffling process going smoothly
        
        int newSize = player1.getDiscardDeck().size(); // 12 here
        moneyPhase.invoke(gameEngine, player1);
        assertTrue(player1.getDrawDeck().size() == newSize-5); // 7 here

        buyPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 1); // After renewing draw deck, discard deck should only have 1 newly bought card now.

        cleanUpPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 6); // 1 newly bought card + 5 cards in the hand
        assertEquals(player1.getDrawDeck().size(), newSize-5); // Draw deck should -5 cards after this cleanup; 2 here

        // Check if all cards in hand and played cards moved to discard
        int handSize3 = gameEngine.getGameState().getCurrentPlayerHand().getUnplayedCards().size();
        int playedCardsSize3 = gameEngine.getGameState().getCurrentPlayerHand().getPlayedCards().size();
        assertEquals(0, handSize3 + playedCardsSize3); // All played/unplayed cards should move to discard;

        // Verify draw deck has correct count
        assertEquals(newSize-5, player1.getDrawDeck().size()); //No action - still 7

        //-----------------------------------------------------That is turn 3-----------------------------------------------------------
        //Testing if after a reshuffling of draw deck the normal game is going ok

        moneyPhase.invoke(gameEngine, player1);
        assertTrue(player1.getDrawDeck().size() == 7-5); // 2

        buyPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 6+1); // +1 card = 7 cards now

        cleanUpPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 7+5); // +5 cards from hand = 12 cards now
        assertEquals(player1.getDrawDeck().size(), 2); // Unchanged, still 2

        // Check if all cards in hand and played cards moved to discard
        int handSize4 = gameEngine.getGameState().getCurrentPlayerHand().getUnplayedCards().size();
        int playedCardsSize4 = gameEngine.getGameState().getCurrentPlayerHand().getPlayedCards().size();
        assertEquals(0, handSize4 + playedCardsSize4); // All played/unplayed cards should move to discard;

        // Verify draw deck has correct count (10-5)
        assertEquals(7-5, player1.getDrawDeck().size()); // Draw deck should -5 cards after this cleanup;
    }

    //Test if the calculate point mechanism is correct by calculating card values at the end of one simulated 1-player game.
    @Test void calculateAPs() throws Exception {
        Field mainDeck = gameEngine.getClass().getDeclaredField("deck"); 
        mainDeck.setAccessible(true);
        GameDeck deck = (GameDeck) mainDeck.get(gameEngine);

        Method initializeGameState = gameEngine.getClass().getDeclaredMethod("initializeGameState", Player.class);
        initializeGameState.setAccessible(true);
        initializeGameState.invoke(gameEngine, player1);

        Method processTurn = gameEngine.getClass().getDeclaredMethod("processTurn", Player.class);
        processTurn.setAccessible(true);

        processTurn.invoke(gameEngine, player1);

        Method isGameOver = gameEngine.getClass().getDeclaredMethod("isGameOver");
        isGameOver.setAccessible(true);

        Method computeScores = gameEngine.getClass().getDeclaredMethod("computeScores");
        computeScores.setAccessible(true);

        while (!(Boolean) isGameOver.invoke(gameEngine)) {
            processTurn.invoke(gameEngine, player1);
        }

        @SuppressWarnings("unchecked")
        List<Player.ScorePair> scorePair = (ArrayList<Player.ScorePair>) computeScores.invoke(gameEngine);
        int score = scorePair.get(0).getScore(); // Only one player in the test so far

        int money = 0;
        for (Map.Entry<Card.Type, Integer> entry: deck.getCardCounts().entrySet()) {
            if (entry.getKey().getCategory() == Card.Type.Category.VICTORY) {
                if (entry.getKey() == Card.Type.METHOD) {
                    money += entry.getKey().getValue()*(14-deck.getNumAvailable(entry.getKey()));
                } else if (entry.getKey() == Card.Type.FRAMEWORK | entry.getKey() == Card.Type.MODULE) {
                    money += entry.getKey().getValue()*(8-deck.getNumAvailable(entry.getKey()));
                }
            }
        }

        assertEquals(money, score);   
    }

}