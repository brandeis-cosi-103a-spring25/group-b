package edu.brandeis.cosi103a.groupb;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

import com.google.common.collect.*;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi103a.groupb.Player.FinalBossPlayer;
import edu.brandeis.cosi.atg.api.decisions.TrashCardDecision;

public class FinalBossPlayerTest {
    
    private FinalBossPlayer player;
    private GameState gameState;
    private GameDeck deck;
    
    @BeforeEach
    public void setup() {
        player = new FinalBossPlayer("TestPlayer");
        
        // Setup a basic game deck
        Map<Card.Type, Integer> cardCounts = new HashMap<>();
        cardCounts.put(Card.Type.BITCOIN, 30);
        cardCounts.put(Card.Type.ETHEREUM, 20);
        cardCounts.put(Card.Type.METHOD, 8);
        cardCounts.put(Card.Type.MODULE, 8);
        cardCounts.put(Card.Type.FRAMEWORK, 8);
        cardCounts.put(Card.Type.IPO, 10);
        deck = new GameDeck(ImmutableMap.copyOf(cardCounts));
        
        // Setup a basic game state
        gameState = new GameState(
            "TestPlayer",
            new Hand(
                ImmutableList.of(), // Played cards
                ImmutableList.of()  // Unplayed cards
            ),
            GameState.TurnPhase.ACTION,
            1, // Actions
            0, // Money
            1, // Buys
            deck
        );
    }
    
    @Test
    public void testPlayerInitialization() {
        assertEquals("TestPlayer", player.getName());
        assertTrue(player.getHand().getAllCards().isEmpty());
        assertTrue(player.getDrawDeck().isEmpty());
        assertTrue(player.getDiscardDeck().isEmpty());
    }
    
    @Test
    public void testReactionPhaseWithMonitoring() {
        // Create a hand with a Monitoring card
        Card monitoringCard = new Card(Card.Type.MONITORING, 1);
        Hand hand = new Hand(
            ImmutableList.of(), // Played cards
            ImmutableList.of(monitoringCard) // Unplayed cards
        );
        player.setHand(hand);
        
        // Create a game state in Reaction phase
        GameState reactionState = new GameState(
            "TestPlayer",
            hand,
            GameState.TurnPhase.REACTION,
            1, 0, 1, deck
        );
        
        // Create options with a PlayCardDecision for Monitoring
        List<Decision> decisions = new ArrayList<>();
        decisions.add(new PlayCardDecision(monitoringCard));
        decisions.add(new EndPhaseDecision(GameState.TurnPhase.REACTION));
        
        // Test that player always chooses to reveal Monitoring
        Decision result = player.makeDecision(reactionState, ImmutableList.copyOf(decisions), Optional.empty());
        assertTrue(result instanceof PlayCardDecision);
        assertEquals(monitoringCard, ((PlayCardDecision) result).getCard());
    }
    
    @Test
    public void testActionPhaseWithPriorityActions() {
        // Create cards of various action types
        Card ipoCard = new Card(Card.Type.IPO, 1);
        Card hackCard = new Card(Card.Type.HACK, 2);
        Card codeReviewCard = new Card(Card.Type.CODE_REVIEW, 3);
        
        // Create a hand with these action cards
        Hand hand = new Hand(
            ImmutableList.of(), // Played cards
            ImmutableList.of(ipoCard, hackCard, codeReviewCard) // Unplayed cards
        );
        player.setHand(hand);
        
        // Create game state in Action phase
        GameState actionState = new GameState(
            "TestPlayer",
            hand,
            GameState.TurnPhase.ACTION,
            1, 0, 1, deck
        );
        
        // Create options for playing each card
        List<Decision> decisions = new ArrayList<>();
        decisions.add(new PlayCardDecision(ipoCard));
        decisions.add(new PlayCardDecision(hackCard));
        decisions.add(new PlayCardDecision(codeReviewCard));
        decisions.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));
        
        // Test that player prioritizes IPO first in early game
        Decision result = player.makeDecision(actionState, ImmutableList.copyOf(decisions), Optional.empty());
        assertTrue(result instanceof PlayCardDecision);
        assertEquals(ipoCard, ((PlayCardDecision) result).getCard());
    }
    
    @Test
    public void testMoneyPhasePlayAllMoney() {
        // Create money cards
        Card bitcoinCard = new Card(Card.Type.BITCOIN, 1);
        Card ethereumCard = new Card(Card.Type.ETHEREUM, 2);
        
        // Create a hand with money and non-money cards
        Card methodCard = new Card(Card.Type.METHOD, 3); // Victory card
        Hand hand = new Hand(
            ImmutableList.of(), // Played cards
            ImmutableList.of(bitcoinCard, ethereumCard, methodCard) // Unplayed cards
        );
        player.setHand(hand);
        
        // Create game state in Money phase
        GameState moneyState = new GameState(
            "TestPlayer",
            hand,
            GameState.TurnPhase.MONEY,
            0, 0, 1, deck
        );
        
        // Create options
        List<Decision> decisions = new ArrayList<>();
        decisions.add(new PlayCardDecision(bitcoinCard));
        decisions.add(new PlayCardDecision(ethereumCard));
        decisions.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        // First decision should be to play a money card
        Decision result = player.makeDecision(moneyState, ImmutableList.copyOf(decisions), Optional.empty());
        assertTrue(result instanceof PlayCardDecision);
        assertTrue(((PlayCardDecision) result).getCard().getType().getCategory() == Card.Type.Category.MONEY);
    }
    
    @Test
    public void testDiscardPrioritizesBugs() {
        // Create cards including a Bug
        Card bugCard = new Card(Card.Type.BUG, 1);
        Card bitcoinCard = new Card(Card.Type.BITCOIN, 2);
        
        // Create a hand with these cards
        Hand hand = new Hand(
            ImmutableList.of(), // Played cards
            ImmutableList.of(bugCard, bitcoinCard) // Unplayed cards
        );
        player.setHand(hand);
        
        // Create game state in Discard phase
        GameState discardState = new GameState(
            "TestPlayer",
            hand,
            GameState.TurnPhase.DISCARD,
            0, 0, 0, deck
        );
        
        // Create discard options
        List<Decision> decisions = new ArrayList<>();
        decisions.add(new DiscardCardDecision(bugCard));
        decisions.add(new DiscardCardDecision(bitcoinCard));
        decisions.add(new EndPhaseDecision(GameState.TurnPhase.DISCARD));
        
        // Test that player always discards Bugs first
        Decision result = player.makeDecision(discardState, ImmutableList.copyOf(decisions), Optional.empty());
        assertTrue(result instanceof DiscardCardDecision);
        assertEquals(bugCard, ((DiscardCardDecision) result).getCard());
    }
    
    @Test
    public void testTrashUpgradeStrategy() {
        // Create cards for trashing
        Card bitcoinCard = new Card(Card.Type.BITCOIN, 1);
        Card methodCard = new Card(Card.Type.METHOD, 2);
        
        // Create a hand with these cards
        Hand hand = new Hand(
            ImmutableList.of(), // Played cards
            ImmutableList.of(bitcoinCard, methodCard) // Unplayed cards
        );
        player.setHand(hand);
        
        // Create game state during Refactor (ACTION phase with TrashCardDecision)
        GameState trashState = new GameState(
            "TestPlayer",
            hand,
            GameState.TurnPhase.ACTION,
            1, 0, 1, deck
        );
        
        // Create trash options
        List<Decision> decisions = new ArrayList<>();
        decisions.add(new TrashCardDecision(bitcoinCard));
        decisions.add(new TrashCardDecision(methodCard));
        
        // Test trash decision logic
        Decision result = player.makeDecision(trashState, ImmutableList.copyOf(decisions), Optional.empty());
        assertTrue(result instanceof TrashCardDecision);
    }
    
    @Test
    public void testGainPhaseStrategy() {
        // Create game state in Gain phase
        GameState gainState = new GameState(
            "TestPlayer",
            player.getHand(),
            GameState.TurnPhase.GAIN,
            0, 0, 0, deck
        );
        
        // Create gain options
        List<Decision> decisions = new ArrayList<>();
        decisions.add(new GainCardDecision(Card.Type.FRAMEWORK));
        decisions.add(new GainCardDecision(Card.Type.MODULE));
        decisions.add(new GainCardDecision(Card.Type.ETHEREUM));
        decisions.add(new GainCardDecision(Card.Type.METHOD));
        
        // Test gain phase prioritization
        Decision result = player.makeDecision(gainState, ImmutableList.copyOf(decisions), Optional.empty());
        assertTrue(result instanceof GainCardDecision);
        assertEquals(Card.Type.FRAMEWORK, ((GainCardDecision) result).getCardType());
    }
}
