package edu.brandeis.cosi103a.groupb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue; //Use java reflection to test private method
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.GameDeck;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi103a.groupb.Decks.PlayerDeck;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;
import edu.brandeis.cosi.atg.api.Engine;

public class EngineTest {
    private AtgPlayer player1 = new BigMoneyPlayer("Nancy");
    private AtgPlayer player2 = new HumanPlayer("Abby");
    private GameObserver observer = new ConsoleGameObserver();
    private Engine gameEngine = GameEngine.createEngine(player1, player2, observer);

    @Test
    void testInitializeDeck() throws Exception {
        Field deckField = gameEngine.getClass().getDeclaredField("deck");
        deckField.setAccessible(true);
        GameDeck deck = (GameDeck) deckField.get(gameEngine);

        assertEquals(60, deck.getNumAvailable(Card.Type.BITCOIN));
        assertEquals(40, deck.getNumAvailable(Card.Type.ETHEREUM));
        assertEquals(30, deck.getNumAvailable(Card.Type.DOGECOIN));
        assertEquals(14, deck.getNumAvailable(Card.Type.METHOD));
        assertEquals(8, deck.getNumAvailable(Card.Type.MODULE));
        assertEquals(8, deck.getNumAvailable(Card.Type.FRAMEWORK));
        // Check that each action card type has 10 copies.
        for (Card.Type type : Card.Type.values()) {
            if (type.getCategory() == Card.Type.Category.ACTION) {
                assertEquals(10, deck.getNumAvailable(type));
            }
        }
    }

    @Test
    void testInitializeGameState() throws Exception {
        Method initializeGameState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initializeGameState.setAccessible(true);
        initializeGameState.invoke(gameEngine, player1);
        initializeGameState.invoke(gameEngine, player2);

        PlayerDeck discardDeck = player1.getDiscardDeck();
        PlayerDeck drawDeck = player2.getDrawDeck();

        assertTrue(discardDeck.size() == 0);
        // initializeGameState distributed 5 cards to each player's hand
        assertTrue(drawDeck.size() == 5);
    }

    // Only testing Big Money Player here manually with the initial simple conditions for the first 1-2 rounds to check if conditions are set up correctly
    @Test
    void testPhasesAgainAndAgain() throws Exception {
        Field mainDeck = gameEngine.getClass().getDeclaredField("deck"); 
        mainDeck.setAccessible(true);

        Method initializeGameState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initializeGameState.setAccessible(true);
        initializeGameState.invoke(gameEngine, player1);

        Method actionPhase = gameEngine.getClass().getDeclaredMethod("handleActionPhase", AtgPlayer.class);
        actionPhase.setAccessible(true);
        actionPhase.invoke(gameEngine, player1);

        Method moneyPhase = gameEngine.getClass().getDeclaredMethod("handleMoneyPhase", AtgPlayer.class);
        moneyPhase.setAccessible(true);
        moneyPhase.invoke(gameEngine, player1);

        assertTrue(player1.getDrawDeck().size() == 5);

        Method buyPhase = gameEngine.getClass().getDeclaredMethod("handleBuyPhase", AtgPlayer.class);
        buyPhase.setAccessible(true);
        buyPhase.invoke(gameEngine, player1);

        assertEquals(player1.getDiscardDeck().size(), 1); //Bought card directly goes into the discard deck.

        Method cleanUpPhase = gameEngine.getClass().getDeclaredMethod("handleCleanupPhase", AtgPlayer.class);
        cleanUpPhase.setAccessible(true);
        cleanUpPhase.invoke(gameEngine, player1);

        assertEquals(player1.getDiscardDeck().size(), 6);
        assertEquals(player1.getDrawDeck().size(), 0); // deal 5 cards at the cleanup phase

        // Check if all cards in hand and played cards moved to discard
        int handSize = ((GameEngine)gameEngine).getGameState().getCurrentPlayerHand().getUnplayedCards().size();
        int playedCardsSize = ((GameEngine)gameEngine).getGameState().getCurrentPlayerHand().getPlayedCards().size();
        assertEquals(5, handSize + playedCardsSize); // All played/unplayed cards should move to discard, and a new hand is dealt;

        // Verify draw deck has correct count (10-5)
        assertEquals(0, player1.getDrawDeck().size()); // Draw deck should have 0 card after dealing a new hand in cleanup;

        //-----------------------------------------------------That is turn 1-----------------------------------------------------------
        actionPhase.invoke(gameEngine, player1);
        assertTrue(player1.getDrawDeck().size() == 5-5);

        moneyPhase.invoke(gameEngine, player1);
        assertTrue(player1.getDrawDeck().size() == 0);

        buyPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 1+1+5); // 2 bought cards, 5 cards from last round

        cleanUpPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 0); // the discard deck should be reshuffled and moved after the cleanup phase
        assertEquals(player1.getDrawDeck().size(), 12-5);

        // Check if all cards in hand and played cards moved to discard
        int handSize2 = ((GameEngine)gameEngine).getGameState().getCurrentPlayerHand().getUnplayedCards().size();
        int playedCardsSize2 = ((GameEngine)gameEngine).getGameState().getCurrentPlayerHand().getPlayedCards().size();
        assertEquals(5, handSize2 + playedCardsSize2); // // All played/unplayed cards should move to discard, and a new hand is dealt;

        // Verify draw deck has correct count (10-5)
        assertEquals(7, player1.getDrawDeck().size()); // Draw deck should -5 cards after this cleanup;

        //-----------------------------------------------------That is turn 2-----------------------------------------------------------
        //Testing if the reshuffling process going smoothly

        actionPhase.invoke(gameEngine, player1);
        assertTrue(player1.getDrawDeck().size() == 7);
        
        int newSize = player1.getDiscardDeck().size(); // 0 here
        moneyPhase.invoke(gameEngine, player1);
        assertTrue(player1.getDrawDeck().size() == newSize+5+1+1); // 7 here

        buyPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 1); // After renewing draw deck, discard deck should only have 1 newly bought card now.

        cleanUpPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 6); // 1 newly bought card + 5 cards in the hand
        assertEquals(player1.getDrawDeck().size(), newSize+1+1); // Draw deck should 2 cards after this cleanup with the new hand and all new purchases

        // Check if all cards in hand and played cards moved to discard and a new hand is dealt
        int handSize3 = ((GameEngine)gameEngine).getGameState().getCurrentPlayerHand().getUnplayedCards().size();
        int playedCardsSize3 = ((GameEngine)gameEngine).getGameState().getCurrentPlayerHand().getPlayedCards().size();
        assertEquals(5, handSize3 + playedCardsSize3); // All played/unplayed cards should move to discard, a new hand is dealt;

        // Verify draw deck has correct count
        assertEquals(newSize+1+1, player1.getDrawDeck().size()); //No action - still 2

        //-----------------------------------------------------That is turn 3-----------------------------------------------------------
        //Testing if after a reshuffling of draw deck the normal game is going ok

        actionPhase.invoke(gameEngine, player1);
        assertTrue(player1.getDrawDeck().size() == 2);

        moneyPhase.invoke(gameEngine, player1);
        assertTrue(player1.getDrawDeck().size() == 7-5); // 2

        buyPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 6+1); // +1 card = 7 cards now

        cleanUpPhase.invoke(gameEngine, player1);
        assertEquals(player1.getDiscardDeck().size(), 0); // discard deck should be reshuffled and moved after the cleanup phase
        assertEquals(player1.getDrawDeck().size(), 7+5+2-5); // a new hand is dealt, residue in dicard deck 2 + original no. of cards in discard deck

        // Check if all cards in hand and played cards moved to discard
        int handSize4 = ((GameEngine)gameEngine).getGameState().getCurrentPlayerHand().getUnplayedCards().size();
        int playedCardsSize4 = ((GameEngine)gameEngine).getGameState().getCurrentPlayerHand().getPlayedCards().size();
        assertEquals(5, handSize4 + playedCardsSize4); // All played/unplayed cards should move to discard, a new hand is dealt;

        // Verify draw deck has correct count (10-5)
        assertEquals(7+2, player1.getDrawDeck().size()); // Draw deck should 9 cards after this cleanup;
    }

    //Test if the calculate point mechanism is correct by calculating card values at the end of one simulated 1-player game.
    @Test void calculateAPs() throws Exception {
        Method initializeGameState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initializeGameState.setAccessible(true);
        initializeGameState.invoke(gameEngine, player1);

        Method processTurn = gameEngine.getClass().getDeclaredMethod("processTurn", AtgPlayer.class);
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
        ImmutableList<AtgPlayer.ScorePair> scorePair = (ImmutableList<AtgPlayer.ScorePair>) computeScores.invoke(gameEngine);
        int score = scorePair.get(0).getScore(); // Only one player in the test so far

        Field mainDeck = gameEngine.getClass().getDeclaredField("deck"); 
        mainDeck.setAccessible(true);
        GameDeck deck = (GameDeck) mainDeck.get(gameEngine);

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

    // Example of testing one action card effect.
    // Here we simulate BACKLOG. In a real test you may need to override player's makeDecision method 
    // so that it automatically returns an EndPhaseDecision (i.e. no discarding).
    @Test
    void testProcessBacklogEffect() throws Exception {
        // Use reflection to get the private method.
        Method backlogMethod = gameEngine.getClass().getDeclaredMethod("processBacklogEffect", AtgPlayer.class, GameState.class);
        backlogMethod.setAccessible(true);
        
        // Initialize game state for player1.
        Method initState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initState.setAccessible(true);
        initState.invoke(gameEngine, player1);
        
        // Create a dummy GameState (from the engine’s field).
        // (Assuming gameState is now set by initializeGameState)
        GameState beforeState = ((GameEngine)gameEngine).getGameState();
        
        // For testing, assume player's discarding phase immediately ends (i.e. no card is trashed/discarded).
        // In this dummy scenario the number of actions should be increased by 1.
        GameState afterState = (GameState) backlogMethod.invoke(gameEngine, player1, beforeState);
        
        // Verify that available actions increased by 1.
        assertEquals(beforeState.getAvailableActions() + 1, afterState.getAvailableActions());
    }

    // Example test for an Attack card effect: HACK.
    // For HACK, we expect the active player’s money to increase by 2, and for the opponent to be forced to discard.
    // In a real test, you would simulate the opponent decision (for instance always choosing not to reveal MONITORING).
    @Test
    void testProcessHackEffect() throws Exception {
        // Initialize game state for player1.
        Method initState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initState.setAccessible(true);
        initState.invoke(gameEngine, player1);
        
        // Get the current GameState.
        GameState beforeState = ((GameEngine)gameEngine).getGameState();
        
        // Use reflection to invoke processHackEffect.
        Method hackMethod = gameEngine.getClass().getDeclaredMethod("processHackEffect", AtgPlayer.class, GameState.class);
        hackMethod.setAccessible(true);
        
        // In our testing environment, ensure that opponent (player2) does not reveal a MONITORING card.
        // (This may be done via a dummy makeDecision method or by setting player2’s hand appropriately.)
        GameState afterState = (GameState) hackMethod.invoke(gameEngine, player1, beforeState);
        
        // Verify that player1’s spendable money is increased by 2.
        assertEquals(beforeState.getSpendableMoney() + 2, afterState.getSpendableMoney());
            
        // You may also check that player2’s hand size is now at most 3.
        assertTrue(player2.getHand().getUnplayedCards().size() <= 3);
    }

    // Similarly, you can add tests for DAILY_SCRUM, IPO, CODE_REVIEW, TECH_DEBT, REFACTOR, PARALLELIZATION, and EVERGREEN_TEST.
    // For instance, test that after playing DAILY_SCRUM, the active player draws 4 cards and the opponent draws 1 card:
    @Test
    void testProcessDailyScrumEffect() throws Exception {
        // Initialize game state for player1.
        Method initState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initState.setAccessible(true);
        initState.invoke(gameEngine, player1);
        
        GameState beforeState = ((GameEngine)gameEngine).getGameState();
        
        Method dailyScrumMethod = gameEngine.getClass().getDeclaredMethod("processDailyScrumEffect", AtgPlayer.class, GameState.class);
        dailyScrumMethod.setAccessible(true);
        GameState afterState = (GameState) dailyScrumMethod.invoke(gameEngine, player1, beforeState);
        
        // Verify active player's hand has increased (by 4 cards drawn) and available buys increased.
        // Exact numbers may depend on previous state; adjust assertions accordingly.
        assertTrue(afterState.getAvailableBuys() > beforeState.getAvailableBuys());
        
        // Also check that opponent's hand now has 1 additional card.
        // (Depending on your implementation, you may need to compare hand sizes before and after.)
    }

    // Additional tests for the remaining action cards should be similarly structured,
    // invoking the appropriate private method via reflection, setting up dummy decisions if needed,
    // and asserting that the game state changes as expected.

    @Test
    void testProcessIpoEffect() throws Exception {
        Method initState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initState.setAccessible(true);
        initState.invoke(gameEngine, player1);

        GameState beforeState = ((GameEngine) gameEngine).getGameState();

        Method ipoMethod = gameEngine.getClass().getDeclaredMethod("processIpoEffect", AtgPlayer.class, GameState.class);
        ipoMethod.setAccessible(true);
        GameState afterState = (GameState) ipoMethod.invoke(gameEngine, player1, beforeState);

        // Check that available actions increased by 1 and money increased by 2.
        assertEquals(beforeState.getAvailableActions() + 1, afterState.getAvailableActions());
        assertEquals(beforeState.getSpendableMoney() + 2, afterState.getSpendableMoney());
    }

    @Test
    void testProcessCodeReviewEffect() throws Exception {
        Method initState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initState.setAccessible(true);
        initState.invoke(gameEngine, player1);

        GameState beforeState = ((GameEngine) gameEngine).getGameState();

        Method crMethod = gameEngine.getClass().getDeclaredMethod("processCodeReviewEffect", AtgPlayer.class, GameState.class);
        crMethod.setAccessible(true);
        GameState afterState = (GameState) crMethod.invoke(gameEngine, player1, beforeState);

        // Check that available actions increased by 2.
        assertEquals(beforeState.getAvailableActions() + 2, afterState.getAvailableActions());
        // Check that at least one card was drawn.
        assertTrue(afterState.getCurrentPlayerHand().getUnplayedCards().size() >= 
                   beforeState.getCurrentPlayerHand().getUnplayedCards().size());
    }

    @Test
    void testProcessTechDebtEffect() throws Exception {
        // For this test, assume no empty supply piles so that no discarding occurs.
        Method initState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initState.setAccessible(true);
        initState.invoke(gameEngine, player1);

        GameState beforeState = ((GameEngine) gameEngine).getGameState();

        Method techDebtMethod = gameEngine.getClass().getDeclaredMethod("processTechDebtEffect", AtgPlayer.class, GameState.class);
        techDebtMethod.setAccessible(true);
        GameState afterState = (GameState) techDebtMethod.invoke(gameEngine, player1, beforeState);

        // Check bonus: +1 action and +1 money and one card is drawn.
        assertEquals(beforeState.getAvailableActions() + 1, afterState.getAvailableActions());
        assertEquals(beforeState.getSpendableMoney() + 1, afterState.getSpendableMoney());
    }

    // Refactor test has to be rewritten to properly test the trashCardDecision and GainCardDecision

    // @Test
    // void testProcessRefactorEffect() throws Exception {
    //     // Create a test-specific player that will always provide a trash decision.
    //     AtgPlayer testPlayer = new BigMoneyPlayer("Nancy") {
    //         @Override
    //         public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<?> reason)
    //                 throws PlayerViolationException {
    //             // If no trash decision exists in the options, build a fallback trash decision.
    //             if (options.isEmpty()) {
    //                 // Assume that player's hand is not empty
    //                 Card cardToTrash = this.getHand().getUnplayedCards().get(0);
    //                 return new TrashCardDecision(cardToTrash);
    //             }
    //             // If a trash decision is available, return the first one.
    //             for (Decision option : options) {
    //                 if (option instanceof TrashCardDecision) {
    //                     return option;
    //                 }
    //             }
    //             // As a fallback, return the first available option.
    //             return options.get(0);
    //         }
    //     };

    //     // Reinitialize the engine with the test-specific player.
    //     Engine engine = GameEngine.createEngine(testPlayer, player2, observer);

    //     Method initState = engine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
    //     initState.setAccessible(true);
    //     initState.invoke(engine, testPlayer);

    //     GameState beforeState = ((GameEngine) engine).getGameState();

    //     // Invoke processRefactorEffect via reflection.
    //     Method refactorMethod = engine.getClass().getDeclaredMethod("processRefactorEffect", AtgPlayer.class, GameState.class);
    //     refactorMethod.setAccessible(true);
    //     GameState afterState = (GameState) refactorMethod.invoke(engine, testPlayer, beforeState);

    //     // For testing purposes, verify that available actions and spendable money remain consistent.
    //     assertEquals(beforeState.getAvailableActions(), afterState.getAvailableActions());
    //     assertEquals(beforeState.getSpendableMoney(), afterState.getSpendableMoney());
    // }

    @Test
    void testProcessParallelizationEffect() throws Exception {
        // Initialize state and assume the player has at least one valid action card to duplicate.
        Method initState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initState.setAccessible(true);
        initState.invoke(gameEngine, player1);

        GameState beforeState = ((GameEngine) gameEngine).getGameState();

        Method parMethod = gameEngine.getClass().getDeclaredMethod("processParallelizationEffect", AtgPlayer.class, GameState.class);
        parMethod.setAccessible(true);
        GameState afterState = (GameState) parMethod.invoke(gameEngine, player1, beforeState);

        // In this test we simply assert the turn phase remains unchanged.
        assertEquals(beforeState.getTurnPhase(), afterState.getTurnPhase());
    }

    @Test
    void testProcessEvergreenTestEffect() throws Exception {
        // Initialize state for the active player.
        Method initState = gameEngine.getClass().getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initState.setAccessible(true);
        initState.invoke(gameEngine, player1);

        // Get the current GameState.
        GameState beforeState = ((GameEngine)gameEngine).getGameState();
        
        // Invoke the Evergreen Test effect.
        Method evergreenMethod = gameEngine.getClass().getDeclaredMethod("processEvergreenTestEffect", AtgPlayer.class, GameState.class);
        evergreenMethod.setAccessible(true);
        GameState afterState = (GameState) evergreenMethod.invoke(gameEngine, player1, beforeState);

        assertEquals(afterState.getCurrentPlayerHand().getUnplayedCards().size(), beforeState.getCurrentPlayerHand().getUnplayedCards().size() + 2);

        // Verify that the opponent (player2) received a BUG card.
        int bugCount = 0;
        for (Card card : player2.getDiscardDeck().getCards()) {
            if (card.getType() == Card.Type.BUG) {
                bugCount++;
            }
        }
        assertTrue(bugCount > 0);
    }
}