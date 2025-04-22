package edu.brandeis.cosi103a.groupb.Player;

import java.util.*;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi103a.groupb.Decks.*;
import edu.brandeis.cosi103a.groupb.Game.*;

/**
 * RedEye is an automated player that implements a more complex strategy.
 * This player focuses on action cards and special tactics,
 * particularly when it has few money cards in hand.
 */
public class RedEyePlayer implements AtgPlayer {
    // Strategy tracking metrics
    private int cardsBought;
    private int moneyCards;

    // Player components
    private final String name;
    private DiscardDeck discardDeck = new DiscardDeck();
    private DrawDeck drawDeck = new DrawDeck();
    private Hand hand = new Hand(
                ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty)
                ImmutableList.copyOf(new ArrayList<Card>())  // Unplayed cards (empty)
            );

    private final Optional<GameObserver> observer;

    /**
     * Creates a new RedEye player with the specified name.
     * 
     * @param name The player's name
     */
    public RedEyePlayer(String name) {
        this.name = name;
        this.observer = Optional.of(new ConsoleGameObserver());
    }

    /**
     * Returns the player's name.
     * 
     * @return The player's name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Counts the number of cards of a specific category in a hand.
     * 
     * @param hand The hand to check
     * @param category The card category to count
     * @return The number of cards of the specified category
     */
    public int countCardCategory(Hand hand, Card.Type.Category category) {
        int count = 0;
        for (Card card : hand.getAllCards()) {
            if (card.getType().getCategory() == category) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts the number of cards of a specific type in a hand.
     * 
     * @param hand The hand to check
     * @param type The card type to count
     * @return The number of cards of the specified type
     */
    public int countHardCardType(Hand hand, Card.Type type) {
        int count = 0;
        for (Card card : hand.getAllCards()) {
            if (card.getType() == type) {
                count++;
            }
        }
        return count;
    }

    /**
     * Makes a decision based on the current game state and available options.
     * Implements the RedEye strategy for card play and purchases.
     * 
     * @param state The current game state
     * @param options Available decision options
     * @param reason Optional event that prompted this decision
     * @return The chosen decision
     */
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        if (reason.isPresent()) {
            System.out.println("Decision prompted by event: " + reason);
        }

        // REACTION PHASE: Play reaction cards first
        for (Decision option : options) {
            if (option instanceof PlayCardDecision && state.getTurnPhase() == GameState.TurnPhase.REACTION) {
                return option;
            }
        }

        // ACTION PHASE: Strategy based on money cards in hand
        for (Decision option : options) {
            if (option instanceof PlayCardDecision decision && state.getTurnPhase() == GameState.TurnPhase.ACTION) {
                if (countCardCategory(hand, Card.Type.Category.MONEY) <= 2 && decision.getCard().getType() == Card.Type.BACKLOG) {
                    return option; // Play BACKLOG when low on money
                } else if (countCardCategory(hand, Card.Type.Category.MONEY) <= 1 && decision.getCard().getType() == Card.Type.HACK) {
                    return option; // Play HACK when very low on money
                }
            }
        }

        // MONEY PHASE: Always play all available money cards
        for (Decision option : options) {
            if (option instanceof PlayCardDecision && state.getTurnPhase() == GameState.TurnPhase.MONEY) {
                return option;
            }
        }
        
        // BUY PHASE: Advanced buying strategy
        Decision bestBuy = handleBuyPhase(state, options);
        if (bestBuy != null) {
            return bestBuy;
        }

        // DISCARD PHASE: Prefer discarding victory cards, then low-value cards
        Decision bestDiscard = handleDiscardPhase(state, options);
        if (bestDiscard != null) {
            return bestDiscard;
        }

        // GAIN PHASE: Take any card gain options
        for (Decision option: options) {
            if (option instanceof GainCardDecision && state.getTurnPhase() == GameState.TurnPhase.GAIN) {
                return option;
            }
        }

        // DEFAULT: End the current phase if no other option selected
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                return option;
            }
        }

        throw new IllegalStateException("RedEye Player could not find a valid decision.");
    }
    
    /**
     * Handles decisions during the buy phase.
     * 
     * @param state The current game state
     * @param options Available decision options
     * @return The best buy decision, or null if none found
     */
    private Decision handleBuyPhase(GameState state, ImmutableList<Decision> options) {
        Decision bestBuy = null;
        int highestCost = 0;
        
        for (Decision option : options) {
            if (option instanceof BuyDecision buy && state.getTurnPhase() == GameState.TurnPhase.BUY) {
                // Strategy: Avoid purchasing the last Framework if not winning
                if (buy.getCardType() == Card.Type.FRAMEWORK) {
                    int remainingFramework = state.getDeck().getNumAvailable(Card.Type.FRAMEWORK);
                    if (remainingFramework == 1 && !isWinning()) {
                        continue; // Skip this Framework
                    }
                }
                
                // Default strategy: Buy most expensive card
                int cost = buy.getCardType().getValue();
                if (cost > highestCost) {
                    highestCost = cost;
                    bestBuy = buy;
                }

                // Action strategy 1: Buy BACKLOG when money ratio is low
                if (cardsBought > 0 && moneyCards / (double) cardsBought < 0.3) {
                    if (buy.getCardType() == Card.Type.BACKLOG) {
                        bestBuy = buy;
                        break;
                    }
                }

                // Action strategy 2: Buy HACK when money ratio is very low
                if (cardsBought > 0 && moneyCards / (double) cardsBought < 0.2) {
                    if (buy.getCardType() == Card.Type.HACK) {
                        bestBuy = buy;
                        break;
                    }
                }
            }
        }
        
        // Track cards bought for future decisions
        if (bestBuy != null) {
            cardsBought++;
            if (((BuyDecision)bestBuy).getCardType().getCategory() == Card.Type.Category.MONEY) {
                moneyCards++;
            }
        }
        
        return bestBuy;
    }
    
    /**
     * Handles decisions during the discard phase.
     * 
     * @param state The current game state
     * @param options Available decision options
     * @return The best discard decision, or null if none found
     */
    private Decision handleDiscardPhase(GameState state, ImmutableList<Decision> options) {
        // Initialize with first discard option if available
        int lowestVal = -1;
        Decision bestDiscard = null;
        
        if (!options.isEmpty() && options.get(0) instanceof DiscardCardDecision discard && 
            state.getTurnPhase() == GameState.TurnPhase.DISCARD) {
            lowestVal = discard.getCard().getValue();
            bestDiscard = discard;
        }
        
        // Find the best card to discard
        for (Decision option: options) {
            if (option instanceof DiscardCardDecision discard && 
                state.getTurnPhase() == GameState.TurnPhase.DISCARD) {
                
                // Always discard victory cards first (they don't help during play)
                if (discard.getCard().getCategory() == Card.Type.Category.VICTORY) {
                    return discard;
                } 
                // Otherwise, discard lowest value card
                else if (discard.getCard().getValue() < lowestVal) {
                    lowestVal = discard.getCard().getValue();
                    bestDiscard = discard;
                }
            }
        }
        
        return bestDiscard;
    }

    /**
     * Determines if this player is currently winning the game.
     * 
     * @return True if this player has the highest score, false otherwise
     */
    public boolean isWinning() {
        List<Player.ScorePair> scores = GameEngine.getCurrentScores();
        
        // Find my score
        int myScore = 0;
        for (Player.ScorePair pair : scores) {
            if (pair.player.getName().equals(this.name)) {
                myScore = pair.getScore();
                break;
            }
        }
        
        // Check if any other player has a higher or equal score
        for (Player.ScorePair pair : scores) {
            if (!pair.player.getName().equals(this.name) && pair.getScore() >= myScore) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the game observer for this player.
     * 
     * @return The game observer
     */
    @Override
    public Optional<GameObserver> getObserver() {
        return observer;
    }
    
    /**
     * Gets the player's discard deck.
     * 
     * @return The player's discard deck
     */
    @Override
    public DiscardDeck getDiscardDeck() {
        return this.discardDeck;
    }

    /**
     * Gets the player's draw deck.
     * 
     * @return The player's draw deck
     */
    @Override
    public DrawDeck getDrawDeck() {
        return this.drawDeck;
    }

    /**
     * Gets the player's current hand.
     * 
     * @return The player's hand
     */
    @Override
    public Hand getHand() {
        return this.hand;
    }

    /**
     * Sets the player's hand.
     * 
     * @param hand The new hand to set
     */
    @Override
    public void setHand(Hand hand) {
        this.hand = hand;
    }
}
