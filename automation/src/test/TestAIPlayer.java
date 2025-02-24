package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi103a.groupb.Decisions.*;
import edu.brandeis.cosi103a.groupb.Events.GameObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestAIPlayer {

    private AIPlayer aiPlayer;

    @BeforeEach
    public void setUp() {
        aiPlayer = new AIPlayer("AIPlayer1");
    }

    @Test
    public void testGetName() {
        assertEquals("AIPlayer1", aiPlayer.getName());
    }

    @Test
    public void testMakeDecisionMoneyPhase() {
        List<Decision> options = new ArrayList<>();
        options.add(new PlayCardDecision(new Card(Card.Type.BITCOIN, 1)));
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        GameState state = new GameState("AIPlayer1", new Hand(new ArrayList<>(), new ArrayList<>()),
                                        GameState.TurnPhase.MONEY, 0, 1, new GameDeck(new ArrayList<>()));

        Decision decision = aiPlayer.makeDecision(state, options);
        assertTrue(decision instanceof PlayCardDecision);
    }

    @Test
    public void testMakeDecisionBuyPhase() {
        List<Decision> options = new ArrayList<>();
        options.add(new BuyDecision(Card.Type.BITCOIN));
        options.add(new BuyDecision(Card.Type.ETHEREUM));
        options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));

        GameState state = new GameState("AIPlayer1", new Hand(new ArrayList<>(), new ArrayList<>()),
                                        GameState.TurnPhase.BUY, 0, 1, new GameDeck(new ArrayList<>()));

        Decision decision = aiPlayer.makeDecision(state, options);
        assertTrue(decision instanceof BuyDecision);
        assertEquals(Card.Type.ETHEREUM, ((BuyDecision) decision).getCardType());
    }

    @Test
    public void testMakeDecisionEndPhase() {
        List<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        GameState state = new GameState("AIPlayer1", new Hand(new ArrayList<>(), new ArrayList<>()),
                                        GameState.TurnPhase.MONEY, 0, 1, new GameDeck(new ArrayList<>()));

        Decision decision = aiPlayer.makeDecision(state, options);
        assertTrue(decision instanceof EndPhaseDecision);
    }

    @Test
    public void testGetObserver() {
        Optional<GameObserver> observer = aiPlayer.getObserver();
        assertTrue(observer.isPresent());
        assertTrue(observer.get() instanceof ConsoleGameObserver);
    }
}