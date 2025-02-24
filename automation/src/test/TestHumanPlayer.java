package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi103a.groupb.Decisions.*;
import edu.brandeis.cosi103a.groupb.GameObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

        GameState state = new GameState("TestPlayer", new Hand(new ArrayList<>(), new ArrayList<>()),
                                        GameState.TurnPhase.MONEY, 0, 1, new GameDeck(new ArrayList<>()));

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
