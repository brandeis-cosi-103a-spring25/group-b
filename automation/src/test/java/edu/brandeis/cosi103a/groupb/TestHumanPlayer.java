package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi103a.groupb.Decisions.*;
import edu.brandeis.cosi103a.groupb.GameObserver;
import edu.brandeis.cosi103a.groupb.HumanPlayer;
import edu.brandeis.cosi103a.groupb.GameState;
import edu.brandeis.cosi103a.groupb.Hand;
import edu.brandeis.cosi103a.groupb.GameDeck;
import edu.brandeis.cosi103a.groupb.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Cards.Card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestHumanPlayer {

    private HumanPlayer humanPlayer;

    @BeforeEach
    public void setUp() {
        humanPlayer = new HumanPlayer("TestPlayer");
    }

    @Test
    public void testGetName() {
        assertEquals("TestPlayer", humanPlayer.getName());
    }

    @Test
    public void testMakeDecision() {
        String input = "0\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        List<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        Map<Card.Type, Integer> deckMap = new HashMap<>();
        deckMap.put(Card.Type.BITCOIN, 60);
        deckMap.put(Card.Type.ETHEREUM, 40);
        GameDeck deck = new GameDeck(deckMap);

        GameState state = new GameState("TestPlayer", new Hand(new ArrayList<>(), new ArrayList<>()),
                                        GameState.TurnPhase.MONEY, 0, 1, deck);

        Decision decision = humanPlayer.makeDecision(state, options);
        assertTrue(decision instanceof EndPhaseDecision);
    }

    @Test
    public void testGetObserver() {
        Optional<GameObserver> observer = humanPlayer.getObserver();
        assertTrue(observer.isPresent());
        assertTrue(observer.get() instanceof ConsoleGameObserver);
    }
}
