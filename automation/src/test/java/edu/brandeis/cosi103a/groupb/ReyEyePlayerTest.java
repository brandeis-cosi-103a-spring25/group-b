package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi.atg.api.GameDeck;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.GameState.TurnPhase;
import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi.atg.api.Player.ScorePair;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.BuyDecision;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.api.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.api.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.api.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.api.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Player.ReyEyePlayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class ReyEyePlayerTest {
   

@Test
    public void testMakeDecisionWithMoneyPhase() {
        ReyEyePlayer player = new ReyEyePlayer("Rey");
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
    
    @Test
    public void testMakeDecisionWithBuyPhase() {
        ReyEyePlayer player = new ReyEyePlayer("Rey");
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

    @Test
    public void testMakeDecisionWithDiscardPhase() {
        ReyEyePlayer player = new ReyEyePlayer("Rey");
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

    @Test
    public void testMakeDecisionWithGainPhase() {
        ReyEyePlayer player = new ReyEyePlayer("Rey");
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

    @Test
    public void testMakeDecisionWithEndPhase() {
        ReyEyePlayer player = new ReyEyePlayer("Rey");
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