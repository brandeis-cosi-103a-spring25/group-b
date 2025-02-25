package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi103a.groupb.Events.GameEvent;
import edu.brandeis.cosi103a.groupb.Events.PlayCardEvent;
import edu.brandeis.cosi103a.groupb.Events.GainCardEvent;
import edu.brandeis.cosi103a.groupb.Decisions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import java.util.List;



public class TestGameEngine {

    private Player player1;
    private Player player2;
    private GameObserver observer;
    private GameEngine gameEngine;

    @BeforeEach
    public void setUp() {
        player1 = mock(Player.class);
        player2 = mock(Player.class);
        observer = mock(GameObserver.class);
        gameEngine = new GameEngine(player1, player2, observer);
    }

    @Test
    public void testInitializeGameState() {
        GameState gameState = gameEngine.initializeGameState();
        assertNotNull(gameState);
        //assertEquals(player1.getName(), gameState.getCurrentPlayer());
        assertEquals(5, gameState.getCurrentPlayerHand().getUnplayedCards().size());
        assertEquals(GameState.TurnPhase.MONEY, gameState.getTurnPhase());
    }

    @Test
    public void testPlayGame() throws PlayerViolationException {
        when(player1.makeDecision(any(GameState.class), anyList())).thenReturn(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        when(player2.makeDecision(any(GameState.class), anyList())).thenReturn(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        List<Player.ScorePair> scores = gameEngine.play();
        assertNotNull(scores);
        assertEquals(2, scores.size());
    }

    @Test
    public void testProcessTurn() throws PlayerViolationException {
        when(player1.makeDecision(any(GameState.class), anyList())).thenReturn(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        gameEngine.processTurn(player1);
        verify(observer, times(1)).notifyEvent(any(GameState.class), any(GameEvent.class));
    }

    @Test
    public void testHandleMoneyPhase() throws PlayerViolationException {
        when(player1.makeDecision(any(GameState.class), anyList())).thenReturn(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        gameEngine.handleMoneyPhase(player1);
        verify(observer, times(1)).notifyEvent(any(GameState.class), any(PlayCardEvent.class));
    }

    @Test
    public void testHandleBuyPhase() throws PlayerViolationException {
        when(player1.makeDecision(any(GameState.class), anyList())).thenReturn(new EndPhaseDecision(GameState.TurnPhase.BUY));
        gameEngine.handleBuyPhase(player1);
        verify(observer, times(1)).notifyEvent(any(GameState.class), any(GainCardEvent.class));
    }

    @Test
    public void testHandleCleanupPhase() {
        gameEngine.handleCleanupPhase(player1);
        verify(observer, times(1)).notifyEvent(any(GameState.class), any(GameEvent.class));
    }

    @Test
    public void testIsGameOver() {
        assertFalse(gameEngine.isGameOver());
    }

    @Test
    public void testComputeScores() {
        List<Player.ScorePair> scores = gameEngine.computeScores();
        assertNotNull(scores);
        assertEquals(2, scores.size());
    }

    @Test
    public void testCalculateScore() {
        int score = gameEngine.calculateScore(player1);
        assertEquals(0, score); // Assuming no victory cards in the deck initially
    }
}