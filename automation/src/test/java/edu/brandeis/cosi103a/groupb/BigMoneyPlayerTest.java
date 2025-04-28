package edu.brandeis.cosi103a.groupb;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi103a.groupb.Game.*;
import edu.brandeis.cosi103a.groupb.Player.*;
import edu.brandeis.cosi.atg.api.Engine;

/**
 * Test class for the BigMoneyPlayer implementation.
 * 
 * This class tests the BigMoneyPlayer AI, which follows a "big money" strategy 
 * commonly used in deck-building games. The strategy focuses on acquiring high-value
 * cards and maximizing money generation to purchase more expensive cards.
 * 
 * The tests verify that the BigMoneyPlayer:
 * 1. Plays money cards during the MONEY phase
 * 2. Buys the most expensive cards available during the BUY phase
 * 3. Makes appropriate decisions when limited options are available
 * 4. Implements advanced strategies based on the game state (last Framework card, winning position)
 * 
 * Both simple decision tests and more complex strategic tests using game engine integration
 * are included to validate the player's behavior.
 * 
 */
public class BigMoneyPlayerTest {

    /**
     * Tests that BigMoneyPlayer plays money cards during the MONEY phase.
     * 
     * Expected behavior:
     * - When presented with a money card and an option to end the phase,
     *   the player should choose to play the money card to maximize available funds.
     */
    @Test
    public void testMakeDecisionWithMoneyPhase() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        // Create dummy GameState for MONEY phase.
        Hand dummyHand = new Hand(ImmutableList.copyOf(new ArrayList<>()), ImmutableList.copyOf(new ArrayList<>()));
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 1, 0, 1, new GameDeck(ImmutableMap.copyOf(new HashMap<>())));
        // Provide a PlayCardDecision option and an EndPhaseDecision.
        Card dummyCard = new Card(Card.Type.BITCOIN, 1);
        List<Decision> options = new ArrayList<>();
        
        options.add(new PlayCardDecision(dummyCard));
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());

        assertTrue(decision instanceof PlayCardDecision, "Expected decision to be PlayCardDecision in MONEY phase.");
    }
    
    /**
     * Tests that BigMoneyPlayer buys the most expensive card available during the BUY phase.
     * 
     * Expected behavior:
     * - When presented with multiple cards to buy, the player should 
     *   prioritize higher-value cards (FRAMEWORK over BITCOIN in this case)
     *   when it has sufficient money to purchase.
     */
    @Test
    public void testMakeDecisionWithBuyPhase() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        // Create dummy GameState for BUY phase with enough money.
        Hand dummyHand = new Hand(ImmutableList.copyOf(new ArrayList<>()), ImmutableList.copyOf(new ArrayList<>()));
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.BUY, 1, 5, 1, new GameDeck(ImmutableMap.of(Card.Type.FRAMEWORK, 2, Card.Type.BITCOIN, 2)));

        // Provide BuyDecision options (one cheap and one expensive) and an EndPhaseDecision.
        List<Decision> options = new ArrayList<>();
        options.add(new BuyDecision(Card.Type.BITCOIN));
        options.add(new BuyDecision(Card.Type.FRAMEWORK)); // Assume FRAMEWORK has a higher cost.
        options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));

        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());

        assertTrue(decision instanceof BuyDecision, "Expected decision to be BuyDecision.");
        BuyDecision buyDecision = (BuyDecision) decision;
        assertEquals(Card.Type.FRAMEWORK, buyDecision.getCardType(), "Expected to choose FRAMEWORK card.");
    }
    
    /**
     * Tests that BigMoneyPlayer ends the MONEY phase when no cards can be played.
     * 
     * Expected behavior:
     * - When only an EndPhaseDecision is available during the MONEY phase,
     *   the player should choose to end the phase.
     */
    @Test
    public void testMakeDecisionWithMoneyPhaseOnlyEndPhase() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        Hand dummyHand = new Hand(ImmutableList.copyOf(new ArrayList<>()), ImmutableList.copyOf(new ArrayList<>()));
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 1, 0, 1, new GameDeck(ImmutableMap.copyOf(new HashMap<>())));
        
        // Only provide an EndPhaseDecision.
        List<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());
        
        assertTrue(decision instanceof EndPhaseDecision, "Expected decision to be EndPhaseDecision when no PlayCardDecision exists in MONEY phase.");
    }
    
    /**
     * Tests that BigMoneyPlayer ends the BUY phase when no cards can be purchased.
     * 
     * Expected behavior:
     * - When only an EndPhaseDecision is available during the BUY phase,
     *   the player should choose to end the phase.
     */
    @Test
    public void testMakeDecisionWithBuyPhaseOnlyEndPhase() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        Hand dummyHand = new Hand(ImmutableList.copyOf(new ArrayList<>()), ImmutableList.copyOf(new ArrayList<>()));
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.BUY, 1, 5, 1, new GameDeck(ImmutableMap.copyOf(new HashMap<>())));
        
        // Only provide an EndPhaseDecision.
        List<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
        
        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());
        
        assertTrue(decision instanceof EndPhaseDecision, "Expected decision to be EndPhaseDecision when no BuyDecision exists in BUY phase.");
    }
    
    /**
     * Tests that BigMoneyPlayer chooses the first money card when multiple options exist.
     * 
     * Expected behavior:
     * - When multiple PlayCardDecision options are available, the player
     *   should select the first one in the list.
     */
    @Test
    public void testMakeDecisionWithMultiplePlayCardDecisions() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        Hand dummyHand = new Hand(ImmutableList.copyOf(new ArrayList<>()), ImmutableList.copyOf(new ArrayList<>()));
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 1, 0, 1, new GameDeck(ImmutableMap.copyOf(new HashMap<>())));

        // Create two PlayCardDecision options.
        Card firstCard = new Card(Card.Type.BITCOIN, 1);
        Card secondCard = new Card(Card.Type.ETHEREUM, 2);
        List<Decision> options = new ArrayList<>();
        options.add(new PlayCardDecision(firstCard));
        options.add(new PlayCardDecision(secondCard));
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());
        
        // Expect the first PlayCardDecision to be returned.
        assertTrue(decision instanceof PlayCardDecision, "Expected decision to be PlayCardDecision when multiple are provided.");
        PlayCardDecision playDecision = (PlayCardDecision) decision;
        assertEquals(firstCard, playDecision.getCard(), "Expected the first PlayCardDecision option to be chosen.");
    }

    /**
     * Tests the advanced strategy where BigMoneyPlayer skips buying the last Framework card
     * when not in a winning position.
     * 
     * Expected behavior:
     * - When the player is not winning and the last Framework card is available,
     *   the player should choose an alternative card (ETHEREUM) to avoid helping
     *   opponents who might be in a better position to win.
     */
    @Test
    public void testAdvancedStrategySkipsFramework() {
        // Override isWinning() to avoid calling GameEngine.getCurrentScores()
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney") {
            @Override
            public boolean isWinning() {
                return false;
            }
        };

        // Create a deck where exactly 1 Framework card is available 
        // (i.e. the last Framework card) and several ETHEREUM cards are available.
        ImmutableMap<Card.Type, Integer> deckMap = ImmutableMap.of(
            Card.Type.FRAMEWORK, 1,
            Card.Type.ETHEREUM, 5
        );
        GameDeck deck = new GameDeck(deckMap);
        
        // Create a dummy hand and a game state for the BUY phase with enough money.
        Hand dummyHand = new Hand(ImmutableList.copyOf(new ArrayList<>()), ImmutableList.copyOf(new ArrayList<>()));
        // Setting money to 8 so that both options are affordable.
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.BUY, 1, 8, 1, deck);
        
        // Provide BuyDecision options: one for FRAMEWORK and one for ETHEREUM,
        // plus an EndPhaseDecision if no valid purchase can be made.
        List<Decision> options = new ArrayList<>();
        options.add(new BuyDecision(Card.Type.FRAMEWORK));
        options.add(new BuyDecision(Card.Type.ETHEREUM));
        options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
        
        // With isWinning overridden to false, the advanced strategy should skip
        // buying the last Framework card and choose ETHEREUM.
        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());
        
        assertTrue(decision instanceof BuyDecision, "Expected decision to be BuyDecision.");
        BuyDecision buyDecision = (BuyDecision) decision;
        assertNotEquals(Card.Type.FRAMEWORK, buyDecision.getCardType(), "Advanced strategy should skip buying the Framework card when it is the last one and the player is not winning.");
        assertEquals(Card.Type.ETHEREUM, buyDecision.getCardType(), "Expected to choose ETHEREUM as the alternative purchase option.");
    }

    /**
     * Tests the advanced strategy with a real game engine to verify the player's
     * decision-making in a more realistic scenario.
     * 
     * This test:
     * 1. Creates a real game engine with BigMoneyPlayer and an opponent
     * 2. Sets up a game state where the opponent has a higher score
     * 3. Offers the last Framework card as a purchase option
     * 4. Verifies that BigMoneyPlayer skips buying it in favor of an alternative
     * 
     * This tests the integration between the player's strategy and the game engine.
     */
    @Test
    public void testAdvancedStrategySkipsFrameworkWithEngine() throws Exception {
        // Create primary player and a dummy opponent.
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        // Create a HumanPlayer with dummy input (won't be used in this test)
        HumanPlayer opponent = new HumanPlayer("Opponent", new Scanner(new ByteArrayInputStream("0\n".getBytes())));
        
        // Create an observer (or use a dummy implementation)
        GameObserver observer = new ConsoleGameObserver();
        
        // Initialize GameEngine with both players so that currentEngine is set.
        Engine engine = GameEngine.createEngine(player, opponent, observer);
        
        // Force the opponent to have a higher victory score.
        // For example, add a victory card to the opponent's discard deck.
        opponent.getDiscardDeck().addCard(new Card(Card.Type.METHOD, 99));
        
        // Create a controlled deck:
        // Exactly 1 Framework card (the last one) and several ETHEREUM cards.
        Map<Card.Type, Integer> deckMap = new HashMap<>();
        deckMap.put(Card.Type.FRAMEWORK, 1);  // Last Framework card available
        deckMap.put(Card.Type.ETHEREUM, 5);   // Alternative purchase option
        GameDeck controlledDeck = new GameDeck(ImmutableMap.copyOf(deckMap));
        
        // Create a dummy hand (for this test it can be empty) and then a game state for BUY phase.
        Hand dummyHand = new Hand(ImmutableList.copyOf(new ArrayList<>()), ImmutableList.copyOf(new ArrayList<>()));
        // Set money to 8 so both buy options are affordable.
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.BUY, 1, 8, 1, controlledDeck);
        
        // Force the GameEngine's gameState to our controlled state (using reflection).
        Field gameStateField = engine.getClass().getDeclaredField("gameState");
        gameStateField.setAccessible(true);
        gameStateField.set(engine, state);
        
        // Prepare the decision options for BUY phase.
        List<Decision> options = new ArrayList<>();
        options.add(new BuyDecision(Card.Type.FRAMEWORK));
        options.add(new BuyDecision(Card.Type.ETHEREUM));
        options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
        
        // Now call makeDecision on our player.
        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());
        
        // Because the opponent has a higher victory score, isWinning() should return false.
        // Therefore, the advanced strategy should skip Framework (the last card)
        // and choose ETHEREUM.
        assertTrue(decision instanceof BuyDecision, "Expected decision to be BuyDecision.");
        BuyDecision buyDecision = (BuyDecision) decision;
        assertNotEquals(Card.Type.FRAMEWORK, buyDecision.getCardType(), 
                "Advanced strategy should skip buying the Framework card when it is the last one and the player is not winning.");
        assertEquals(Card.Type.ETHEREUM, buyDecision.getCardType(), 
                "Expected to choose ETHEREUM as the alternative purchase option.");
    }
}