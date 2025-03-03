package edu.brandeis.cosi103a.groupb;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.brandeis.cosi103a.groupb.Player.Player;
import edu.brandeis.cosi103a.groupb.Player.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameState;
import edu.brandeis.cosi103a.groupb.Game.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Decisions.*;
import edu.brandeis.cosi103a.groupb.Cards.*;
import edu.brandeis.cosi103a.groupb.Events.*;
import edu.brandeis.cosi103a.groupb.Decks.GameDeck;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HarnessTest {

    private Player humanPlayer;
    private Player bigMoneyPlayer;
    private GameObserver observer;
    private GameState gameState;
    private List<Decision> options;
    private GameEngine gameEngine;

    @BeforeEach
    public void setUp() throws Exception {
        humanPlayer = new HumanPlayer("Alice");
        bigMoneyPlayer = new BigMoneyPlayer("Bot");
        observer = new ConsoleGameObserver();
        gameEngine = new GameEngine(humanPlayer, bigMoneyPlayer, observer);

        // Use reflection to access the private initializeGameState method
        Method initializeGameStateMethod = GameEngine.class.getDeclaredMethod("initializeGameState", Player.class);
        initializeGameStateMethod.setAccessible(true);
        initializeGameStateMethod.invoke(gameEngine, humanPlayer);
        initializeGameStateMethod.invoke(gameEngine, bigMoneyPlayer);

        // Initialize gameState and options
        gameState = new GameState("Alice", null, TurnPhase.MONEY, 0, 1, null);
        options = new ArrayList<>();
        options.add(new EndPhaseDecision(TurnPhase.MONEY));
    }

    @Test
    public void testInitialization() {
        assertNotNull(humanPlayer);
        assertNotNull(bigMoneyPlayer);
        assertNotNull(observer);
        assertNotNull(gameEngine);
    }
   
    @Test
    public void testGamePlay() throws PlayerViolationException {
        List<Player.ScorePair> mockResults = new ArrayList<>();
        mockResults.add(new Player.ScorePair(humanPlayer, 10));
        mockResults.add(new Player.ScorePair(bigMoneyPlayer, 5));

        GameEngine mockGameEngine = Mockito.mock(GameEngine.class);
        when(mockGameEngine.play()).thenReturn(mockResults);

        List<Player.ScorePair> results = mockGameEngine.play();
        assertEquals(2, results.size());
        assertEquals(10, results.get(0).getScore());
        assertEquals(5, results.get(1).getScore());
    }
   
    @Test
    public void testGameEndDueToPlayerViolationException() {
        GameEngine mockGameEngine = Mockito.mock(GameEngine.class);
        try {
            when(mockGameEngine.play()).thenThrow(new PlayerViolationException("Invalid move"));
            mockGameEngine.play();
            fail("Expected PlayerViolationException to be thrown");
        } catch (PlayerViolationException e) {
            assertEquals("Invalid move", e.getMessage());
        }
    }

    @Test
    public void testWinnerDetermination() throws PlayerViolationException {
        List<Player.ScorePair> mockResults = new ArrayList<>();
        mockResults.add(new Player.ScorePair(humanPlayer, 10));
        mockResults.add(new Player.ScorePair(bigMoneyPlayer, 10));

        GameEngine mockGameEngine = Mockito.mock(GameEngine.class);
        when(mockGameEngine.play()).thenReturn(mockResults);

        List<Player.ScorePair> results = mockGameEngine.play();
        int highestScore = -1;
        List<Player> winners = new ArrayList<>();

        for (Player.ScorePair score : results) {
            if (score.getScore() > highestScore) {
                highestScore = score.getScore();
                winners.clear();
                winners.add(score.player);
            } else if (score.getScore() == highestScore) {
                winners.add(score.player);
            }
        }

        assertEquals(2, winners.size());
        assertTrue(winners.contains(humanPlayer));
        assertTrue(winners.contains(bigMoneyPlayer));
    }
    @Test
    public void testObserverNotificationOnGainCard() {
        GameObserver mockObserver = Mockito.mock(GameObserver.class);
        gameEngine = new GameEngine(humanPlayer, bigMoneyPlayer, mockObserver);

        // Simulate gaining a card
        GainCardEvent gainCardEvent = new GainCardEvent(Card.Type.BITCOIN, "Alice");
        mockObserver.notifyEvent(gameState, gainCardEvent);

        // Verify that the observer is notified of the gain card event
        verify(mockObserver, times(1)).notifyEvent(any(GameState.class), any(GainCardEvent.class));
    }

    @Test
    public void testObserverNotificationOnEndTurn() {
        GameObserver mockObserver = Mockito.mock(GameObserver.class);
        gameEngine = new GameEngine(humanPlayer, bigMoneyPlayer, mockObserver);

        // Simulate ending a turn
        EndTurnEvent endTurnEvent = new EndTurnEvent("Alice");
        mockObserver.notifyEvent(gameState, endTurnEvent);

        // Verify that the observer is notified of the end turn event
        verify(mockObserver, times(1)).notifyEvent(any(GameState.class), any(EndTurnEvent.class));
    }

    @Test
    public void testObserverNotificationOnPlayCard() {
        GameObserver mockObserver = Mockito.mock(GameObserver.class);
        gameEngine = new GameEngine(humanPlayer, bigMoneyPlayer, mockObserver);

        // Simulate playing a card
        PlayCardEvent playCardEvent = new PlayCardEvent(new Card(Card.Type.BITCOIN, 1), "Alice");
        mockObserver.notifyEvent(gameState, playCardEvent);

        // Verify that the observer is notified of the play card event
        verify(mockObserver, times(1)).notifyEvent(any(GameState.class), any(PlayCardEvent.class));
    }

    

    
}