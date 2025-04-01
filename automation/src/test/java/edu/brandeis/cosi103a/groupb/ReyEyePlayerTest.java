package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.GameDeck;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.GameState.TurnPhase;
import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi.atg.api.Player.ScorePair;
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
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReyEyePlayerTest {
@Test
    public void testMakeDecisionPlayMoneyCard() {
        ReyEyePlayer player = new ReyEyePlayer("TestPlayer");

        // Create a MONEY card
        Card.Type typeMoney = mock(Card.Type.class);
        when(typeMoney.getCategory()).thenReturn(Card.Type.Category.MONEY);

        Card moneyCard = mock(Card.class);
        when(moneyCard.getType()).thenReturn(typeMoney);

        // Create a PlayCardDecision for the MONEY card
        PlayCardDecision playMoneyCardDecision = new PlayCardDecision(moneyCard);

        // Create a GameState in the MONEY phase
        GameState state = mock(GameState.class);
        when(state.getTurnPhase()).thenReturn(GameState.TurnPhase.MONEY);

        // Test makeDecision
        Decision decision = player.makeDecision(state, ImmutableList.of(playMoneyCardDecision), Optional.empty());
        assertEquals(playMoneyCardDecision, decision, "The decision should be to play the MONEY card.");
    }
    

    
    @Test
    public void testPlayCardInMoneyPhase() {
        ReyEyePlayer player = new ReyEyePlayer("TestPlayer");
        GameState state = Mockito.mock(GameState.class);
        when(state.getTurnPhase()).thenReturn(GameState.TurnPhase.MONEY);

        PlayCardDecision decision = mock(PlayCardDecision.class);
        Decision result = player.makeDecision(state, ImmutableList.of(decision), Optional.empty());

        assertEquals(decision, result);
    }
    
    
    @Test
    public void testDiscardVictoryCardFirst() {
        ReyEyePlayer player = new ReyEyePlayer("TestPlayer");
        GameState state = mock(GameState.class);
        when(state.getTurnPhase()).thenReturn(GameState.TurnPhase.DISCARD);

        Card card = mock(Card.class);
        when(card.getCategory()).thenReturn(Card.Type.Category.VICTORY);

        DiscardCardDecision discard = mock(DiscardCardDecision.class);
        when(discard.getCard()).thenReturn(card);

        Decision result = player.makeDecision(state, ImmutableList.of(discard), Optional.empty());
        assertEquals(discard, result);
    }



    @Test
    public void testGainCardDecision() {
        ReyEyePlayer player = new ReyEyePlayer("TestPlayer");
        GameState state = mock(GameState.class);
        when(state.getTurnPhase()).thenReturn(GameState.TurnPhase.GAIN);

        GainCardDecision gain = mock(GainCardDecision.class);
        Decision result = player.makeDecision(state, ImmutableList.of(gain), Optional.empty());

        assertEquals(gain, result);
    }

    @Test
    public void testEndPhaseDecision() {
        ReyEyePlayer player = new ReyEyePlayer("TestPlayer");
        GameState state = mock(GameState.class);

        EndPhaseDecision end = mock(EndPhaseDecision.class);
        Decision result = player.makeDecision(state, ImmutableList.of(end), Optional.empty());

        assertEquals(end, result);
    }

    @Test
    public void testCountCardCategory() {
        ReyEyePlayer player = new ReyEyePlayer("TestPlayer");

        Card card = mock(Card.class);
        Card.Type type = mock(Card.Type.class);
        when(type.getCategory()).thenReturn(Card.Type.Category.MONEY);
        when(card.getType()).thenReturn(type);

        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of(card, card, card));
        int result = player.countCardCategory(hand, Card.Type.Category.MONEY);

        assertEquals(3, result);
    }

    @Test
    public void testCountHardCardType() {
        ReyEyePlayer player = new ReyEyePlayer("TestPlayer");

        Card card = mock(Card.class);
        when(card.getType()).thenReturn(Card.Type.BACKLOG);

        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of(card, card));
        int result = player.countHardCardType(hand, Card.Type.BACKLOG);

        assertEquals(2, result);
    }

    // @Test
    // public void testIsWinningTrue() {
    //     ReyEyePlayer player = new ReyEyePlayer("TestPlayer");

    //     try (MockedStatic<GameEngine> mocked = Mockito.mockStatic(GameEngine.class)) {
    //         Player.ScorePair me = new Player.ScorePair(player, 9);
    //         Player opponent = mock(Player.class);
    //         when(opponent.getName()).thenReturn("Other");
    //         Player.ScorePair other = new Player.ScorePair(opponent, 5);

    //         mocked.when(GameEngine::getCurrentScores).thenReturn(List.of(me, other));

    //         assertTrue(player.isWinning());
    //     }
    // }

    @Test
    public void testIsWinningFalse() {
        ReyEyePlayer player = new ReyEyePlayer("TestPlayer");

        try (MockedStatic<GameEngine> mocked = Mockito.mockStatic(GameEngine.class)) {
            Player.ScorePair me = new Player.ScorePair(player, 5);
            Player opponent = mock(Player.class);
            when(opponent.getName()).thenReturn("Other");
            Player.ScorePair other = new Player.ScorePair(opponent, 7);

            mocked.when(GameEngine::getCurrentScores).thenReturn(List.of(me, other));

            assertFalse(player.isWinning());
        }
    }

   
    
    
    @Test
    void testIsWinningTrue() {
        // Create the player
        ReyEyePlayer player = new ReyEyePlayer("Marco");
        Hand realHand = new Hand(
            ImmutableList.of(), // played
            ImmutableList.of()  // unplayed
        );
        player.setHand(realHand);
    
        // Mock the player "Rey"
        Player mockPlayerRey = mock(Player.class);
        when(mockPlayerRey.getName()).thenReturn("Marco");
    
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



//     @Test
//     void testIsWinningFalse() {
//         // Mock the player "Rey"
//         Player mockPlayerRey = mock(Player.class);
//         when(mockPlayerRey.getName()).thenReturn("Rey");
    
//         // Mock another player
//         Player mockOtherPlayer = mock(Player.class);
//         when(mockOtherPlayer.getName()).thenReturn("OtherPlayer");
    
//         // Create the scores list
//         List<Player.ScorePair> scores = new ArrayList<>();
//         scores.add(new Player.ScorePair(mockPlayerRey, 8)); // Rey's score
//         scores.add(new Player.ScorePair(mockOtherPlayer, 10)); // Other player's score
    
//         // Mock the static method GameEngine.getCurrentScores
//         try (MockedStatic<GameEngine> mocked = mockStatic(GameEngine.class)) {
//             mocked.when(GameEngine::getCurrentScores).thenReturn(scores);
    
//             // Assert that Rey is not winning
//             assertFalse(player.isWinning(), "Player should not be winning with a lower score.");
//         }
//     }
// }
// //     @Test
// //     void testMakeDecision_playMoneyCard() {
// //         // Create the player
// //         ReyEyePlayer player = new ReyEyePlayer("Rey");
// //         Hand realHand = new Hand(
// //             ImmutableList.of(), // played
// //             ImmutableList.of()  // unplayed
// //         );
// //         player.setHand(realHand);

// //         // Mock GameState and other dependencies
// //         GameState mockState = mock(GameState.class);
// //         Card mockCard = mock(Card.class);
// //         PlayCardDecision mockDecision = mock(PlayCardDecision.class);

// //         // Stub methods
// //         when(mockState.getTurnPhase()).thenReturn(GameState.TurnPhase.MONEY);
// //         when(mockDecision.getCard()).thenReturn(mockCard);

// //         // Create decision options
// //         ImmutableList<Decision> options = ImmutableList.of(mockDecision);

// //         // Call the method under test
// //         Decision decision = player.makeDecision(mockState, options, Optional.empty());

// //         // Verify the result
// //         assertEquals(mockDecision, decision, "The decision should match the mocked PlayCardDecision.");
// //     }
// //     // @Test
// //     // void testMakeDecision_playMoneyCard() {
// //     //     // Mock GameState and other dependencies
// //     //     GameState mockState = mock(GameState.class);
// //     //     Card mockCard = mock(Card.class);
// //     //     PlayCardDecision mockDecision = mock(PlayCardDecision.class);
    
// //     //     // Stub methods
// //     //     when(mockState.getTurnPhase()).thenReturn(GameState.TurnPhase.MONEY);
// //     //     when(mockDecision.getCard()).thenReturn(mockCard);
    
// //     //     // Create decision options
// //     //     ImmutableList<Decision> options = ImmutableList.of(mockDecision);
    
// //     //     // Call the method under test
// //     //     Decision decision = player.makeDecision(mockState, options, Optional.empty());
    
// //     //     // Verify the result
// //     //     assertEquals(mockDecision, decision, "The decision should match the mocked PlayCardDecision.");
// //     // }

// //     // @Test
// //     // void testMakeDecision_gainCardFallback() {
// //     //     GameState mockState = mock(GameState.class);
// //     //     GainCardDecision gainDecision = mock(GainCardDecision.class);
    
// //     //     when(mockState.getTurnPhase()).thenReturn(GameState.TurnPhase.GAIN);
// //     //     ImmutableList<Decision> options = ImmutableList.of(gainDecision);
    
// //     //     Decision decision = player.makeDecision(mockState, options, Optional.empty());
// //     //     assertEquals(gainDecision, decision);
// //     // }

// //     @Test
// //     void testMakeDecision_gainCardFallback() {
// //         // Create the player
// //         ReyEyePlayer player = new ReyEyePlayer("Rey");
// //         Hand realHand = new Hand(
// //             ImmutableList.of(), // played
// //             ImmutableList.of()  // unplayed
// //         );
// //         player.setHand(realHand);

// //         // Mock GameState and other dependencies
// //         GameState mockState = mock(GameState.class);
// //         GainCardDecision gainDecision = mock(GainCardDecision.class);

// //         // Stub methods
// //         when(mockState.getTurnPhase()).thenReturn(GameState.TurnPhase.GAIN);

// //         // Create decision options
// //         ImmutableList<Decision> options = ImmutableList.of(gainDecision);

// //         // Call the method under test
// //         Decision decision = player.makeDecision(mockState, options, Optional.empty());

// //         // Verify the result
// //         assertEquals(gainDecision, decision, "The decision should match the mocked GainCardDecision.");
// //     }

    
}
