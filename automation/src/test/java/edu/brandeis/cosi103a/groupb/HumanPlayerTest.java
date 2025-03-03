package edu.brandeis.cosi103a.groupb;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.Scanner;

import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;
import edu.brandeis.cosi103a.groupb.Decisions.Decision;
import edu.brandeis.cosi103a.groupb.Decisions.EndPhaseDecision;
import edu.brandeis.cosi103a.groupb.Decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupb.Game.GameState;
import edu.brandeis.cosi103a.groupb.Decks.GameDeck;
import edu.brandeis.cosi103a.groupb.Decks.Hand;
import edu.brandeis.cosi103a.groupb.Cards.Card;

public class HumanPlayerTest {

    @Test
    public void testMakeDecisionEndPhase() {
        // Simulate user input: "0" to choose first option.
        String simulatedInput = "0\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());
        Scanner scanner = new Scanner(inputStream);
        HumanPlayer player = new HumanPlayer("TestPlayer", scanner);
        
        // Create a dummy GameState with an empty hand and a dummy deck.
        Hand dummyHand = new Hand(new ArrayList<>(), new ArrayList<>());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 0, 1, new GameDeck(new HashMap<>()));
        
        List<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        Decision decision = player.makeDecision(state, options);
        assertTrue(decision instanceof EndPhaseDecision, "Expected decision to be EndPhaseDecision.");
    }
    
    @Test
    public void testMakeDecisionInvalidThenValid() {
        // Simulate invalid input followed by valid "0"
        String simulatedInput = "abc\n0\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());
        Scanner scanner = new Scanner(inputStream);
        HumanPlayer player = new HumanPlayer("TestPlayer", scanner);
        
        // Create dummy GameState
        Hand dummyHand = new Hand(new ArrayList<>(), new ArrayList<>());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 0, 1, new GameDeck(new HashMap<>()));
        
        // Two options available: index 0 and 1.
        List<Decision> options = new ArrayList<>();
        Card dummyCard = new Card(Card.Type.BITCOIN, 1);
        options.add(new PlayCardDecision(dummyCard));
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        // The invalid input should be consumed and then "0" selects the first option.
        Decision decision = player.makeDecision(state, options);
        assertTrue(decision instanceof PlayCardDecision, "Expected decision to be PlayCardDecision after invalid input followed by valid input.");
    }
    
    @Test
    public void testMakeDecisionMultipleOptions() {
        // Simulate user input: "1" to choose second option.
        String simulatedInput = "1\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());
        Scanner scanner = new Scanner(inputStream);
        HumanPlayer player = new HumanPlayer("TestPlayer", scanner);
        
        // Create dummy GameState.
        Hand dummyHand = new Hand(new ArrayList<>(), new ArrayList<>());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 0, 1, new GameDeck(new HashMap<>()));
        
        // Provide three options.
        List<Decision> options = new ArrayList<>();
        // Option 0: PlayCardDecision
        options.add(new PlayCardDecision(new Card(Card.Type.BITCOIN, 1)));
        // Option 1: EndPhaseDecision
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        // Option 2: Another EndPhaseDecision (for variety).
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        Decision decision = player.makeDecision(state, options);
        // With input "1", we expect the second option to be chosen.
        assertTrue(decision instanceof EndPhaseDecision, "Expected decision to be EndPhaseDecision from multiple options.");
    }
    
    @Test
    public void testGetName() {
        // Ensure that getName() returns the proper name.
        String simulatedInput = "0\n"; // input not used in this test.
        ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());
        Scanner scanner = new Scanner(inputStream);
        HumanPlayer player = new HumanPlayer("TestPlayer", scanner);
        assertEquals("TestPlayer", player.getName());
    }
}