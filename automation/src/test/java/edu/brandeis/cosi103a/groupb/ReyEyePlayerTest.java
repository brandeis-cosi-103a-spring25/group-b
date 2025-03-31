package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi.atg.api.Player;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReyEyePlayerTest {

    private ReyEyePlayer player;
    
    @Test
    void testIsWinningTrue() {
        // Create the player
        ReyEyePlayer player = new ReyEyePlayer("Rey");
        Hand realHand = new Hand(
            ImmutableList.of(), // played
            ImmutableList.of()  // unplayed
        );
        player.setHand(realHand);
    
        // Mock the player "Rey"
        Player mockPlayerRey = mock(Player.class);
        when(mockPlayerRey.getName()).thenReturn("Rey");
    
        // Mock another player
        Player mockOtherPlayer = mock(Player.class);
        when(mockOtherPlayer.getName()).thenReturn("OtherPlayer");
    
        // Create the scores list
        List<Player.ScorePair> scores = List.of(
                new Player.ScorePair(mockPlayerRey, 10), // Rey's score
                new Player.ScorePair(mockOtherPlayer, 8) // Other player's score
        );
    
        // Mock the static method GameEngine.getCurrentScores
        try (MockedStatic<GameEngine> mocked = mockStatic(GameEngine.class)) {
            mocked.when(GameEngine::getCurrentScores).thenReturn(scores);
    
            // Assert that Rey is winning
            assertTrue(player.isWinning(), "Player should be winning with the highest score.");
        }
    }

    // @Test
    // void testIsWinningTrue() {
    //     // Create the player
    //     ReyEyePlayer player = new ReyEyePlayer("Rey");
    //     Hand realHand = new Hand(
    //         ImmutableList.of(), // played
    //         ImmutableList.of()  // unplayed
    //     );
    //     player.setHand(realHand);

    //     // Mock the player "Rey"
    //     Player mockPlayer = mock(Player.class);
    //     when(mockPlayer.getName()).thenReturn("Rey");

    //     // Create the scores list
    //     List<Player.ScorePair> scores = List.of(
    //             new Player.ScorePair(mockPlayer, 10),
    //             new Player.ScorePair(mock(Player.class), 8)
    //     );

    //     // Mock the static method GameEngine.getCurrentScores
    //     try (MockedStatic<GameEngine> mocked = mockStatic(GameEngine.class)) {
    //         mocked.when(GameEngine::getCurrentScores).thenReturn(scores);

    //         // Assert that Rey is winning
    //         assertTrue(player.isWinning(), "Player should be winning with the highest score.");
    //     }
    // }

    @Test
    void testIsWinningFalse() {
        // Mock the player "Rey"
        Player mockPlayerRey = mock(Player.class);
        when(mockPlayerRey.getName()).thenReturn("Rey");
    
        // Mock another player
        Player mockOtherPlayer = mock(Player.class);
        when(mockOtherPlayer.getName()).thenReturn("OtherPlayer");
    
        // Create the scores list
        List<Player.ScorePair> scores = new ArrayList<>();
        scores.add(new Player.ScorePair(mockPlayerRey, 8)); // Rey's score
        scores.add(new Player.ScorePair(mockOtherPlayer, 10)); // Other player's score
    
        // Mock the static method GameEngine.getCurrentScores
        try (MockedStatic<GameEngine> mocked = mockStatic(GameEngine.class)) {
            mocked.when(GameEngine::getCurrentScores).thenReturn(scores);
    
            // Assert that Rey is not winning
            assertFalse(player.isWinning(), "Player should not be winning with a lower score.");
        }
    }

    @Test
    void testMakeDecision_playMoneyCard() {
        // Create the player
        ReyEyePlayer player = new ReyEyePlayer("Rey");
        Hand realHand = new Hand(
            ImmutableList.of(), // played
            ImmutableList.of()  // unplayed
        );
        player.setHand(realHand);

        // Mock GameState and other dependencies
        GameState mockState = mock(GameState.class);
        Card mockCard = mock(Card.class);
        PlayCardDecision mockDecision = mock(PlayCardDecision.class);

        // Stub methods
        when(mockState.getTurnPhase()).thenReturn(GameState.TurnPhase.MONEY);
        when(mockDecision.getCard()).thenReturn(mockCard);

        // Create decision options
        ImmutableList<Decision> options = ImmutableList.of(mockDecision);

        // Call the method under test
        Decision decision = player.makeDecision(mockState, options, Optional.empty());

        // Verify the result
        assertEquals(mockDecision, decision, "The decision should match the mocked PlayCardDecision.");
    }
    // @Test
    // void testMakeDecision_playMoneyCard() {
    //     // Mock GameState and other dependencies
    //     GameState mockState = mock(GameState.class);
    //     Card mockCard = mock(Card.class);
    //     PlayCardDecision mockDecision = mock(PlayCardDecision.class);
    
    //     // Stub methods
    //     when(mockState.getTurnPhase()).thenReturn(GameState.TurnPhase.MONEY);
    //     when(mockDecision.getCard()).thenReturn(mockCard);
    
    //     // Create decision options
    //     ImmutableList<Decision> options = ImmutableList.of(mockDecision);
    
    //     // Call the method under test
    //     Decision decision = player.makeDecision(mockState, options, Optional.empty());
    
    //     // Verify the result
    //     assertEquals(mockDecision, decision, "The decision should match the mocked PlayCardDecision.");
    // }

    // @Test
    // void testMakeDecision_gainCardFallback() {
    //     GameState mockState = mock(GameState.class);
    //     GainCardDecision gainDecision = mock(GainCardDecision.class);
    
    //     when(mockState.getTurnPhase()).thenReturn(GameState.TurnPhase.GAIN);
    //     ImmutableList<Decision> options = ImmutableList.of(gainDecision);
    
    //     Decision decision = player.makeDecision(mockState, options, Optional.empty());
    //     assertEquals(gainDecision, decision);
    // }

    @Test
    void testMakeDecision_gainCardFallback() {
        // Create the player
        ReyEyePlayer player = new ReyEyePlayer("Rey");
        Hand realHand = new Hand(
            ImmutableList.of(), // played
            ImmutableList.of()  // unplayed
        );
        player.setHand(realHand);

        // Mock GameState and other dependencies
        GameState mockState = mock(GameState.class);
        GainCardDecision gainDecision = mock(GainCardDecision.class);

        // Stub methods
        when(mockState.getTurnPhase()).thenReturn(GameState.TurnPhase.GAIN);

        // Create decision options
        ImmutableList<Decision> options = ImmutableList.of(gainDecision);

        // Call the method under test
        Decision decision = player.makeDecision(mockState, options, Optional.empty());

        // Verify the result
        assertEquals(gainDecision, decision, "The decision should match the mocked GainCardDecision.");
    }

    
}
