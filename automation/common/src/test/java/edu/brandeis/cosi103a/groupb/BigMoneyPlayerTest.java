package edu.brandeis.cosi103a.groupb;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi.atg.api.GameDeck;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.BuyDecision;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.api.decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi.atg.api.Engine;


public class BigMoneyPlayerTest {

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
    
    // Additional test: Only EndPhaseDecision exists in MONEY phase
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
    
    // Additional test: Only EndPhaseDecision exists in BUY phase (simulate insufficient options).
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
    
    // Additional test: In MONEY phase, when multiple PlayCardDecision options exist, the first one is chosen.
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

    // Test on advanced strategy to skip the framework car assuming isWinning returns false
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

    // Test on advanced strategy with gameEngine along with isWinning to check if BigMoneyPlayer correctly determines the current situation and make an expected decision
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