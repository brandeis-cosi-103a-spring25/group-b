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
 * An automated player that implements the "Big Money" strategy.
 * This strategy focuses on buying the most valuable cards available,
 * prioritizing money cards over action cards.
 */
public class BigMoneyPlayer implements AtgPlayer {
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
     * Creates a new BigMoney player with the specified name.
     * 
     * @param name The player's name
     */
    public BigMoneyPlayer(String name) {
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
     * Makes a decision based on the current game state and available options.
     * Implements the BigMoney strategy focusing on purchasing high-value cards.
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

        // ACTION PHASE: Play all action cards
        for (Decision option : options) {
            if (option instanceof PlayCardDecision && state.getTurnPhase() == GameState.TurnPhase.ACTION) {
                return option;
            }
        }

        // REACTION PHASE: Play all reaction cards
        for (Decision option : options) {
            if (option instanceof PlayCardDecision && state.getTurnPhase() == GameState.TurnPhase.REACTION) {
                return option;
            }
        }

        // MONEY PHASE: Play all money cards
        for (Decision option : options) {
            if (option instanceof PlayCardDecision && state.getTurnPhase() == GameState.TurnPhase.MONEY) {
                return option;
            }
        }
        
        // BUY PHASE: Buy the highest-value card available
        Decision bestBuy = handleBuyPhase(state, options);
        if (bestBuy != null) {
            return bestBuy;
        }

        // DISCARD PHASE: Discard victory cards first, then lowest value cards
        Decision bestDiscard = handleDiscardPhase(state, options);
        if (bestDiscard != null) {
            return bestDiscard;
        }

        // GAIN PHASE: Accept any card gain
        for (Decision option: options) {
            if (option instanceof GainCardDecision && state.getTurnPhase() == GameState.TurnPhase.GAIN) {
                return option;
            }
        }

        // Handle any trash card opportunities (typically from action cards)
        for (Decision option : options) {
            if (option instanceof TrashCardDecision && state.getTurnPhase() == GameState.TurnPhase.ACTION) {
                return option;
            }
        }

        // DEFAULT: End the current phase if no other action taken
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                return option;
            }
        }
        
        throw new IllegalStateException("Big Money Player could not find a valid decision.");
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
                // Strategic consideration: Avoid buying the last Framework if not winning
                if (buy.getCardType() == Card.Type.FRAMEWORK) {
                    int remainingFramework = state.getDeck().getNumAvailable(Card.Type.FRAMEWORK);
                    if (remainingFramework == 1 && !isWinning()) {
                        // Skip buying the last Framework as it could end the game
                        continue;
                    }
                }
                
                // Always buy the most expensive card available
                int cost = buy.getCardType().getValue();
                if (cost > highestCost) {
                    highestCost = cost;
                    bestBuy = buy;
                }
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
