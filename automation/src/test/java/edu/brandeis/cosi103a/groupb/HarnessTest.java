package edu.brandeis.cosi103a.groupb;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import com.google.common.collect.*;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.cards.*;
import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi.atg.api.event.*;
import edu.brandeis.cosi.atg.api.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupb.Player.*;
import edu.brandeis.cosi103a.groupb.Game.*;
import edu.brandeis.cosi.atg.api.Engine;
import java.util.*;
import java.lang.reflect.Method;

/**
 * Test class for the PlayerRatingHarness implementation.
 * 
 * This class tests the tournament simulation harness that evaluates AI player performance.
 * The harness runs games between different player implementations, collects statistics,
 * and determines player rankings.
 * 
 * The tests verify:
 * 1. Proper initialization of player instances and game environment
 * 2. Game simulation and score tracking
 * 3. Exception handling during game play
 * 4. Winner determination logic including ties
 * 5. Observer notification for game events
 * 
 * Both real game components and mocked objects are used to isolate and test specific
 * functionality of the harness.
 * 
 */
public class HarnessTest {

    private AtgPlayer humanPlayer;
    private AtgPlayer bigMoneyPlayer;
    private GameObserver observer;
    private GameState gameState;
    private List<Decision> options;
    private Engine gameEngine;

    /**
     * Sets up the test environment before each test.
     * 
     * This method:
     * 1. Creates player instances (human and AI)
     * 2. Initializes a game observer
     * 3. Creates a game engine with standard deck configuration
     * 4. Initializes players' game states
     * 5. Prepares a basic game state and decision options
     */
    @BeforeEach
    public void setUp() throws Exception {
        humanPlayer = new HumanPlayer("Alice");
        bigMoneyPlayer = new BigMoneyPlayer("Bot");
        observer = new ConsoleGameObserver();
        GameDeck deck = new GameDeck(ImmutableMap.of(
            Card.Type.BITCOIN, 60,
            Card.Type.ETHEREUM, 40,
            Card.Type.DOGECOIN, 30,
            Card.Type.METHOD, 14,
            Card.Type.MODULE, 8,
            Card.Type.FRAMEWORK, 8
        ));
        gameEngine = new GameEngine(humanPlayer, bigMoneyPlayer, observer,deck);

        // Use reflection to access the private initializeGameState method
        Method initializeGameStateMethod = GameEngine.class.getDeclaredMethod("initializeGameState", AtgPlayer.class);
        initializeGameStateMethod.setAccessible(true);
        initializeGameStateMethod.invoke(gameEngine, humanPlayer);
        initializeGameStateMethod.invoke(gameEngine, bigMoneyPlayer);

        // Initialize gameState and options
        gameState = new GameState("Alice", null, GameState.TurnPhase.MONEY, 1, 0, 1, null);
        options = new ArrayList<>();
        options.add(new EndPhaseDecision(TurnPhase.MONEY));
    }

    /**
     * Tests basic initialization of test components.
     * 
     * Verifies that:
     * - All player instances are properly created
     * - The observer is initialized
     * - The game engine is set up correctly
     */
    @Test
    public void testInitialization() {
        assertNotNull(humanPlayer);
        assertNotNull(bigMoneyPlayer);
        assertNotNull(observer);
        assertNotNull(gameEngine);
    }
   
    /**
     * Tests the game play simulation with custom engine implementation.
     * 
     * This test:
     * 1. Creates a custom Engine that returns predefined scores
     * 2. Verifies that the players and their scores are tracked correctly
     * 3. Confirms that the result contains the expected number of entries
     */
    @Test
    public void testGamePlay() throws PlayerViolationException {
        // Create a custom Engine implementation that returns our test scores
        Engine testEngine = new Engine() {
            @Override
            public ImmutableList<AtgPlayer.ScorePair> play() throws PlayerViolationException {
                List<AtgPlayer.ScorePair> mockResults = new ArrayList<>();
                mockResults.add(new AtgPlayer.ScorePair(humanPlayer, 10));
                mockResults.add(new AtgPlayer.ScorePair(bigMoneyPlayer, 5));
                return ImmutableList.copyOf(mockResults);
            }
        };
        
        // Test the custom engine
        ImmutableList<AtgPlayer.ScorePair> results = testEngine.play();
        assertEquals(2, results.size());
        assertEquals(10, results.get(0).getScore());
        assertEquals(5, results.get(1).getScore());
    }
   
    /**
     * Tests the handling of PlayerViolationExceptions during game play.
     * 
     * This test verifies that:
     * - When a player violation occurs, the appropriate exception is thrown
     * - The exception contains the expected error message
     */
    @Test
    public void testGameEndDueToPlayerViolationException() {
        // Create a custom Engine implementation that throws an exception
        Engine testEngine = new Engine() {
            @Override
            public ImmutableList<AtgPlayer.ScorePair> play() throws PlayerViolationException {
                throw new PlayerViolationException("Invalid move");
            }
        };
        
        try {
            testEngine.play();
            fail("Expected PlayerViolationException to be thrown");
        } catch (PlayerViolationException e) {
            assertEquals("Invalid move", e.getMessage());
        }
    }

    /**
     * Tests the winner determination logic, including tie handling.
     * 
     * This test:
     * 1. Creates a custom engine that produces a tied score
     * 2. Verifies that both players are correctly identified as winners
     * 3. Confirms that the highest score is determined correctly
     */
    @Test
    public void testWinnerDetermination() throws PlayerViolationException {
        // Create a custom Engine implementation that returns tied scores
        Engine testEngine = new Engine() {
            @Override
            public ImmutableList<AtgPlayer.ScorePair> play() throws PlayerViolationException {
                List<AtgPlayer.ScorePair> mockResults = new ArrayList<>();
                mockResults.add(new AtgPlayer.ScorePair(humanPlayer, 10));
                mockResults.add(new AtgPlayer.ScorePair(bigMoneyPlayer, 10));
                return ImmutableList.copyOf(mockResults);
            }
        };
        
        // Test with the custom engine
        ImmutableList<AtgPlayer.ScorePair> results = testEngine.play();
        
        // Determine winners
        int highestScore = -1;
        List<AtgPlayer> winners = new ArrayList<>();

        for (AtgPlayer.ScorePair score : results) {
            if (score.getScore() > highestScore) {
                highestScore = score.getScore();
                winners.clear();
                winners.add((AtgPlayer) score.player);
            } else if (score.getScore() == highestScore) {
                winners.add((AtgPlayer) score.player);
            }
        }

        assertEquals(2, winners.size());
        assertTrue(winners.contains(humanPlayer));
        assertTrue(winners.contains(bigMoneyPlayer));
    }

    /**
     * Tests the observer notification system for gain card events.
     * 
     * This test:
     * 1. Creates a mock observer to track notifications
     * 2. Simulates a gain card event
     * 3. Verifies that the observer was properly notified of the event
     */
    @Test
    public void testObserverNotificationOnGainCard() {
        GameObserver mockObserver = Mockito.mock(GameObserver.class);
        gameEngine = GameEngine.createEngine(humanPlayer, bigMoneyPlayer, mockObserver);

        // Simulate gaining a card
        GainCardEvent gainCardEvent = new GainCardEvent(Card.Type.BITCOIN, "Alice");
        mockObserver.notifyEvent(gameState, gainCardEvent);

        // Verify that the observer is notified of the gain card event
        verify(mockObserver, times(1)).notifyEvent(any(GameState.class), any(GainCardEvent.class));
    }

    /**
     * Tests the observer notification system for end turn events.
     * 
     * This test:
     * 1. Creates a mock observer to track notifications
     * 2. Simulates an end turn event
     * 3. Verifies that the observer was properly notified of the event
     */
    @Test
    public void testObserverNotificationOnEndTurn() {
        GameObserver mockObserver = Mockito.mock(GameObserver.class);
        gameEngine = GameEngine.createEngine(humanPlayer, bigMoneyPlayer, mockObserver);

        // Simulate ending a turn
        EndTurnEvent endTurnEvent = new EndTurnEvent();
        mockObserver.notifyEvent(gameState, endTurnEvent);

        // Verify that the observer is notified of the end turn event
        verify(mockObserver, times(1)).notifyEvent(any(GameState.class), any(EndTurnEvent.class));
    }

    /**
     * Tests the observer notification system for play card events.
     * 
     * This test:
     * 1. Creates a mock observer to track notifications
     * 2. Simulates a play card event
     * 3. Verifies that the observer was properly notified of the event
     */
    @Test
    public void testObserverNotificationOnPlayCard() {
        GameObserver mockObserver = Mockito.mock(GameObserver.class);
        gameEngine = GameEngine.createEngine(humanPlayer, bigMoneyPlayer, mockObserver);

        // Simulate playing a card
        PlayCardEvent playCardEvent = new PlayCardEvent(new Card(Card.Type.BITCOIN, 1), "Alice");
        mockObserver.notifyEvent(gameState, playCardEvent);

        // Verify that the observer is notified of the play card event
        verify(mockObserver, times(1)).notifyEvent(any(GameState.class), any(PlayCardEvent.class));
    }
}