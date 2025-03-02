package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi103a.groupb.Decisions.*;
import edu.brandeis.cosi103a.groupb.Cards.Card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class TestHumanPlayer {

    private HumanPlayer humanPlayer;
    private final InputStream originalIn = System.in;

    @BeforeEach
    public void setUp() {
        humanPlayer = new HumanPlayer("TestPlayer");
    }
    
    @AfterEach
    public void tearDown() {
        System.setIn(originalIn);
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
        
        // Create a new scanner using the new System.in
        Scanner testScanner = new Scanner(System.in);
        HumanPlayer humanPlayer = new HumanPlayer("TestPlayer", testScanner);

        List<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        Map<Card.Type, Integer> deckMap = new HashMap<>();
        deckMap.put(Card.Type.BITCOIN, 60);
        deckMap.put(Card.Type.ETHEREUM, 40);
        GameDeck deck = new GameDeck(deckMap);

        GameState state = new GameState("TestPlayer",
                                        new Hand(new ArrayList<>(), new ArrayList<>()),
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
