package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.*;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi103a.groupb.Player.RedEyePlayer;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the RedEyePlayer implementation.
 * 
 * This class validates the decision-making capabilities of the RedEyePlayer, which implements
 * a strategic AI player that makes intelligent decisions based on the current game state.
 * 
 * The tests verify that the RedEyePlayer correctly prioritizes actions according to its strategy:
 * - In money phase: PlayCard decisions for money cards
 * - In buy phase: Buy decisions with preference for higher-value cards
 * - In discard phase: Discard decisions with priority for victory cards (low utility)
 * - In gain phase: Gain card decisions according to the player's strategy
 * - End phase decisions when no other options are available
 * 
 */
public class RedEyePlayerTest {
   
    /**
     * Tests the RedEyePlayer's decision making during the MONEY phase.
     * 
     * Expected behavior:
     * - When presented with a money card and an option to end the phase,
     *   the player should choose to play the money card to maximize available funds.
     */
    @Test
    public void testMakeDecisionWithMoneyPhase() {
        RedEyePlayer player = new RedEyePlayer("Rey");
        // Create dummy GameState for MONEY phase.
        Hand dummyHand = new Hand(ImmutableList.of(), ImmutableList.of());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 1, 0, 1, new GameDeck(ImmutableMap.of()));

        // Provide a PlayCardDecision option and an EndPhaseDecision.
        Card moneyCard = new Card(Card.Type.BITCOIN, 1);
        List<Decision> options = new ArrayList<>();
        options.add(new PlayCardDecision(moneyCard));
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());

        assertTrue(decision instanceof PlayCardDecision, "Expected decision to be PlayCardDecision in MONEY phase.");
    }
    
    /**
     * Tests the RedEyePlayer's decision making during the BUY phase.
     * 
     * Expected behavior:
     * - When presented with multiple cards to buy, the player should 
     *   prioritize higher-value cards (FRAMEWORK over BITCOIN in this case)
     *   when it has sufficient money to purchase.
     */
    @Test
    public void testMakeDecisionWithBuyPhase() {
        RedEyePlayer player = new RedEyePlayer("Rey");
        // Create dummy GameState for BUY phase with enough money.
        Hand dummyHand = new Hand(ImmutableList.of(), ImmutableList.of());
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
     * Tests the RedEyePlayer's decision making during the DISCARD phase.
     * 
     * Expected behavior:
     * - When forced to discard cards, the player should prioritize discarding
     *   victory cards (METHOD in this test case) over money cards (BITCOIN)
     *   as victory cards have less utility during gameplay.
     */
    @Test
    public void testMakeDecisionWithDiscardPhase() {
        RedEyePlayer player = new RedEyePlayer("Rey");
        // Create dummy GameState for DISCARD phase.
        Hand dummyHand = new Hand(ImmutableList.of(), ImmutableList.of());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.DISCARD, 1, 0, 1, new GameDeck(ImmutableMap.of()));

        // Provide DiscardCardDecision options.
        Card victoryCard = new Card(Card.Type.METHOD, 1); // Assume METHOD is a VICTORY card.
        Card moneyCard = new Card(Card.Type.BITCOIN, 2);
        List<Decision> options = new ArrayList<>();
        options.add(new DiscardCardDecision(victoryCard));
        options.add(new DiscardCardDecision(moneyCard));

        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());

        assertTrue(decision instanceof DiscardCardDecision, "Expected decision to be DiscardCardDecision.");
        DiscardCardDecision discardDecision = (DiscardCardDecision) decision;
        assertEquals(victoryCard, discardDecision.getCard(), "Expected to discard the VICTORY card first.");
    }

    /**
     * Tests the RedEyePlayer's decision making during the GAIN phase.
     * 
     * Expected behavior:
     * - When given the opportunity to gain a card for free, the player
     *   should accept the offer, particularly for valuable cards like FRAMEWORK.
     */
    @Test
    public void testMakeDecisionWithGainPhase() {
        RedEyePlayer player = new RedEyePlayer("Rey");
        // Create dummy GameState for GAIN phase.
        Hand dummyHand = new Hand(ImmutableList.of(), ImmutableList.of());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.GAIN, 1, 0, 1, new GameDeck(ImmutableMap.of()));

        // Provide GainCardDecision options.
        Card.Type gainType = Card.Type.FRAMEWORK;
        List<Decision> options = new ArrayList<>();
        options.add(new GainCardDecision(gainType));

        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());

        assertTrue(decision instanceof GainCardDecision, "Expected decision to be GainCardDecision.");
        GainCardDecision gainDecision = (GainCardDecision) decision;
        assertEquals(gainType, gainDecision.getCardType(), "Expected to gain the FRAMEWORK card.");
    }

    /**
     * Tests the RedEyePlayer's decision making when only EndPhase is available.
     * 
     * Expected behavior:
     * - When no other decisions are available, the player should choose
     *   to end the current phase to progress the game.
     */
    @Test
    public void testMakeDecisionWithEndPhase() {
        RedEyePlayer player = new RedEyePlayer("Rey");
        // Create dummy GameState for any phase.
        Hand dummyHand = new Hand(ImmutableList.of(), ImmutableList.of());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.ACTION, 1, 0, 1, new GameDeck(ImmutableMap.of()));

        // Provide only an EndPhaseDecision.
        List<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));

        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());

        assertTrue(decision instanceof EndPhaseDecision, "Expected decision to be EndPhaseDecision when no other valid options exist.");
    }
}