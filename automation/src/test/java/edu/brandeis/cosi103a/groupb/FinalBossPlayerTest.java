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

/**
 * Test class for the FinalBossPlayer implementation.
 * 
 * This class tests the FinalBossPlayer AI, which implements the most advanced strategy
 * in the game. The FinalBossPlayer combines tactical elements from other player types
 * with enhanced decision-making capabilities for a more challenging opponent.
 * 
 * The tests verify that the FinalBossPlayer:
 * 1. Makes strategically optimal decisions in different game phases
 * 2. Properly prioritizes cards based on their strategic value
 * 3. Handles special cases like reaction cards and card trashing effectively
 * 4. Applies different strategies based on the game situation
 * 
 * Each test focuses on a specific game phase or scenario to verify the player's
 * strategic decision-making capabilities.
 * 
 */
public class FinalBossPlayerTest {
    
    private FinalBossPlayer player;
    private GameState gameState;
    private GameDeck deck;
    
    /**
     * Sets up the test environment before each test.
     * 
     * This method:
     * 1. Creates a new FinalBossPlayer instance
     * 2. Initializes a game deck with standard card distributions
     * 3. Creates a basic game state in the ACTION phase
     */
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
    
    /**
     * Tests the FinalBossPlayer's initialization and basic properties.
     * 
     * Verifies that:
     * - The player's name is set correctly
     * - Initial hand is empty
     * - Initial draw and discard decks are empty
     */
    @Test
    public void testPlayerInitialization() {
        assertEquals("TestPlayer", player.getName());
        assertTrue(player.getHand().getAllCards().isEmpty());
        assertTrue(player.getDrawDeck().isEmpty());
        assertTrue(player.getDiscardDeck().isEmpty());
    }
    
    /**
     * Tests the FinalBossPlayer's reaction phase decision making with Monitoring cards.
     * 
     * Expected behavior:
     * - When in reaction phase and the player has a Monitoring card,
     *   it should always choose to play/reveal the card for its reaction effect
     *   to prevent negative effects from opponent's attack cards.
     */
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
    
    /**
     * Tests the FinalBossPlayer's action phase card prioritization.
     * 
     * Expected behavior:
     * - When multiple action cards are available, the player should prioritize
     *   playing high-value action cards like IPO first to maximize early game advantage
     */
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
    
    /**
     * Tests the FinalBossPlayer's money phase decision making.
     * 
     * Expected behavior:
     * - During the money phase, the player should play all available money cards
     *   before ending the phase to maximize purchasing power
     * - The player should choose money cards over victory cards
     */
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
    
    /**
     * Tests the FinalBossPlayer's discard phase priorities, focusing on Bug cards.
     * 
     * Expected behavior:
     * - When forced to discard cards, the player should prioritize discarding
     *   Bug cards first, as they provide no benefit and take up hand space
     */
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
    
    /**
     * Tests the FinalBossPlayer's card trashing strategy.
     * 
     * Expected behavior:
     * - When presented with the opportunity to trash cards (e.g., from REFACTOR),
     *   the player should make a strategic decision about which cards to trash
     *   to optimize their deck
     */
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
    
    /**
     * Tests the FinalBossPlayer's strategy during the gain phase.
     * 
     * Expected behavior:
     * - When offered free cards during the gain phase, the player should
     *   prioritize high-value victory cards like FRAMEWORK over other options
     */
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
