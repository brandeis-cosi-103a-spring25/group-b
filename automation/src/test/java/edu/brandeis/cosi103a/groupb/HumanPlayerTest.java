package edu.brandeis.cosi103a.groupb;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.google.common.collect.*;

import java.io.ByteArrayInputStream;
import java.util.*;

import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi.atg.api.cards.*;
import edu.brandeis.cosi103a.groupb.Player.*;
import edu.brandeis.cosi.atg.api.*;

/**
 * Test class for the HumanPlayer implementation.
 * 
 * This class tests the HumanPlayer, which is designed to allow human input
 * via console during gameplay. Since we can't test with actual human input,
 * these tests use simulated input streams to verify:
 * 
 * 1. Basic decision making with a single option
 * 2. Handling of invalid input (non-numeric or out-of-range values)
 * 3. Selection from multiple options
 * 4. Basic player properties like name retrieval
 * 
 * Each test simulates console input by providing a ByteArrayInputStream
 * configured with predefined input that the HumanPlayer will read.
 * 
 */
public class HumanPlayerTest {

    /**
     * Tests that the HumanPlayer can make a basic decision with a single option.
     * 
     * This test simulates a user selecting option "0" (the first and only option)
     * which is an EndPhaseDecision. It verifies that the player returns the
     * correct decision type.
     */
    @Test
    public void testMakeDecisionEndPhase() {
        // Simulate user input: "0" to choose first option.
        String simulatedInput = "0\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());
        Scanner scanner = new Scanner(inputStream);
        HumanPlayer player = new HumanPlayer("TestPlayer", scanner);
        
        // Create a dummy GameState with an empty hand and a dummy deck.
        Hand dummyHand = new Hand(ImmutableList.copyOf(new ArrayList<>()), ImmutableList.copyOf(new ArrayList<>()));
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 1, 0, 1, new GameDeck(ImmutableMap.copyOf(new HashMap<>())));
        
        List<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());
        assertTrue(decision instanceof EndPhaseDecision, "Expected decision to be EndPhaseDecision.");
    }
    
    /**
     * Tests that the HumanPlayer correctly handles invalid input.
     * 
     * This test simulates a user first entering "abc" (invalid input),
     * followed by "0" (valid input). It verifies that the player properly
     * rejects the invalid input and then processes the valid input to
     * return the correct decision.
     */
    @Test
    public void testMakeDecisionInvalidThenValid() {
        // Simulate invalid input followed by valid "0"
        String simulatedInput = "abc\n0\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());
        Scanner scanner = new Scanner(inputStream);
        HumanPlayer player = new HumanPlayer("TestPlayer", scanner);
        
        // Create dummy GameState
        Hand dummyHand = new Hand(ImmutableList.copyOf(new ArrayList<>()), ImmutableList.copyOf(new ArrayList<>()));
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 1, 0, 1, new GameDeck(ImmutableMap.copyOf(new HashMap<>())));
        
        // Two options available: index 0 and 1.
        List<Decision> options = new ArrayList<>();
        Card dummyCard = new Card(Card.Type.BITCOIN, 1);
        options.add(new PlayCardDecision(dummyCard));
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        // The invalid input should be consumed and then "0" selects the first option.
        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());
        assertTrue(decision instanceof PlayCardDecision, "Expected decision to be PlayCardDecision after invalid input followed by valid input.");
    }
    
    /**
     * Tests that the HumanPlayer can select from multiple options.
     * 
     * This test simulates a user entering "1" to select the second option
     * from a list of three. It verifies that the player correctly returns
     * the selected decision.
     */
    @Test
    public void testMakeDecisionMultipleOptions() {
        // Simulate user input: "1" to choose second option.
        String simulatedInput = "1\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());
        Scanner scanner = new Scanner(inputStream);
        HumanPlayer player = new HumanPlayer("TestPlayer", scanner);
        
        // Create dummy GameState.
        Hand dummyHand = new Hand(ImmutableList.copyOf(new ArrayList<>()), ImmutableList.copyOf(new ArrayList<>()));
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 1, 0, 1, new GameDeck(ImmutableMap.copyOf(new HashMap<>())));
        
        // Provide three options.
        List<Decision> options = new ArrayList<>();
        // Option 0: PlayCardDecision
        options.add(new PlayCardDecision(new Card(Card.Type.BITCOIN, 1)));
        // Option 1: EndPhaseDecision
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        // Option 2: Another EndPhaseDecision (for variety).
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());
        // With input "1", we expect the second option to be chosen.
        assertTrue(decision instanceof EndPhaseDecision, "Expected decision to be EndPhaseDecision from multiple options.");
    }
    
    /**
     * Tests that the HumanPlayer correctly returns its name.
     * 
     * This simple test verifies that the getName() method returns
     * the name that was provided in the constructor.
     */
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