package edu.brandeis.cosi103a.groupb.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.stream.Collectors;

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

/**
 * FinalBossPlayer implements an advanced strategy for the game.
 * 
 * The strategy adapts based on game phase (early, mid, late) and targets optimal
 * deck composition through carefully planned buying, trashing, and card play decisions.
 * 
 * Core strategic elements:
 *   1. Early game (turns 1-6): Builds economic foundation with strategic action cards
 *   2. Mid game (turns 7-12): Balances action cards with buying power, begins acquiring victory points
 *   3. Late game (turns 13+): Focuses on high-value victory cards and optimizing final score
 *   4. Card upgrade strategy that evaluates each card's potential improvement value
 *   5. Tactical discarding based on card utility and situation (forced vs. optional)
 *   6. Situational awareness to avoid helping opponents by not buying the last Framework when behind
 * 
 * The player analyzes the current game state for each decision and applies
 * prioritized action lists based on the game phase to maximize effectiveness.
 */
public class FinalBossPlayer implements AtgPlayer {
    private int turnCount = 0;
    private int actionsPlayed = 0;

    private final String name;
    private DiscardDeck discardDeck = new DiscardDeck();
    private DrawDeck drawDeck = new DrawDeck();
    private Hand hand = new Hand(
                ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty)
                ImmutableList.copyOf(new ArrayList<Card>())  // Unplayed cards (empty)
            );

    private final Optional<GameObserver> observer;

    public FinalBossPlayer(String name) {
        this.name = name;
        this.observer = Optional.of((GameObserver) new ConsoleGameObserver());
    }

    @Override
    public String getName() {
        return name;
    }

    public int countCardCategory(Hand hand, Card.Type.Category category) {
        int count = 0;
        for (Card card : hand.getAllCards()) {
            if (card.getType().getCategory() == category) {
                count++;
            }
        }
        return count;
    }

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
     * Determines the current game phase based on turn count.
     * Early game focuses on building economy and initial action cards.
     */
    private boolean isEarlyGame() { return turnCount < 7; }
    
    /**
     * Mid game balances between economy, action cards, and starting to acquire victory points.
     */
    private boolean isMidGame() { return turnCount >= 7 && turnCount < 13; }
    
    /**
     * Late game prioritizes high-value victory cards and optimizing final score.
     */
    private boolean isLateGame() { return turnCount >= 13; }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        // Main decision tree - processes each game phase according to strategy
        
        // Handle reaction cards immediately
        if (state.getTurnPhase() == GameState.TurnPhase.REACTION) {
            for (Decision option : options) {
                if (option instanceof PlayCardDecision) {
                    return option; // Always reveal Monitoring to avoid attacks
                }
            }
        }

        // Split trash and discard handling
        if (state.getTurnPhase() == GameState.TurnPhase.DISCARD) {
            return handleDiscardDecision(options, state);
        }

        // Handle REFACTOR trash decisions - strategic upgrading of cards
        if (state.getTurnPhase() == GameState.TurnPhase.ACTION && options.get(0) instanceof TrashCardDecision) {
            return handleTrashDecision(options, state);
        }

        // Handle gain decisions for Refactor
        if (state.getTurnPhase() == GameState.TurnPhase.GAIN) {
            return handleGainPhase(options);
        }

        // Action phase strategy
        if (state.getTurnPhase() == GameState.TurnPhase.ACTION) {
            Decision actionDecision = handleActionPhase(options, state);
            if (actionDecision != null) return actionDecision;
        }

        // Money phase - maximize available money by playing all money cards
        if (state.getTurnPhase() == GameState.TurnPhase.MONEY) {
            for (Decision option : options) {
                if (option instanceof PlayCardDecision) {
                    return option;
                }
            }
        }

        // Buy phase strategy
        if (state.getTurnPhase() == GameState.TurnPhase.BUY) {
            Decision buyDecision = handleBuyPhase(options, state);
            if (buyDecision != null) return buyDecision;
        }

        // If no other decisions were made, end the phase
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                return option;
            }
        }

        throw new IllegalStateException("GoatBot could not find a valid decision");
    }

    /**
     * Evaluates cards to trash for maximum upgrade value.
     * Prioritizes cards based on upgrade potential and current deck composition.
     */
    private Decision handleTrashDecision(ImmutableList<Decision> options, GameState state) {
        // Evaluate cards by their upgrade potential
        Card bestToTrash = null;
        int bestUpgradeValue = -1;

        for (Decision option : options) {
            if (option instanceof TrashCardDecision trashDecision) {
                Card card = trashDecision.getCard();
                int currentCost = card.getType().getCost();
                int potentialCost = currentCost + 2; // Max cost of card we can gain
                int upgradeValue = evaluateUpgradePotential(card, potentialCost);
                
                if (upgradeValue > bestUpgradeValue) {
                    bestUpgradeValue = upgradeValue;
                    bestToTrash = card;
                }
            }
        }

        // Find and return the decision for the best card to trash
        for (Decision option : options) {
            if (option instanceof TrashCardDecision trashDecision && 
                trashDecision.getCard().equals(bestToTrash)) {
                return option;
            }
        }

        // Fallback to first option if something went wrong
        return options.get(0);
    }

    /**
     * Calculates the strategic value of upgrading a card.
     * Adjusts values based on card type, game phase, and current deck composition.
     */
    private int evaluateUpgradePotential(Card card, int maxNewCost) {
        // Base value is how much we can upgrade the cost
        int upgradeValue = maxNewCost - card.getType().getCost();

        // Adjust value based on card type and game phase
        switch (card.getType().getCategory()) {
            case MONEY:
                if (card.getType() == Card.Type.BITCOIN && countMoneyCards() > 10) {
                    upgradeValue += 3; // Good to upgrade basic money if we have enough
                } else if (card.getType() == Card.Type.ETHEREUM && countMoneyCards() > 15) {
                    upgradeValue += 2; // Potentially upgrade mid-tier money late game
                }
                break;
            case VICTORY:
                if (card.getType() == Card.Type.BUG) {
                    upgradeValue += 5; // Always great to upgrade bugs (PARAMETERS TBD)
                } else if (card.getType() == Card.Type.METHOD && isLateGame()) {
                    upgradeValue += 4; // Very good to upgrade basic victory cards late game
                }
                break;
            case ACTION:
                if (countActionCards() > 8) {
                    upgradeValue += 2; // Good to upgrade actions if we have too many
                }
                break;
        }

        // Adjust based on game phase
        if (isLateGame()) {
            if (card.getType().getCategory() == Card.Type.Category.VICTORY) {
                upgradeValue -= 2; // Less likely to trash victory cards late game
            }
        }

        return upgradeValue;
    }

    /**
     * Manages discard decisions with different priorities for forced and optional discards.
     * Prioritizes discarding negative cards (Bugs) and victory cards during action phases.
     */
    private Decision handleDiscardDecision(ImmutableList<Decision> options, GameState state) {
        // First check if this is a forced discard (no EndPhaseDecision available)
        boolean isForced = !options.stream().anyMatch(d -> d instanceof EndPhaseDecision);

        // Check if this is BACKLOG discard phase
        boolean isBacklogDiscard = state.getTurnPhase() == GameState.TurnPhase.DISCARD && 
                                 !isForced;

        // First priority: Always discard bugs - they provide negative points
        for (Decision option : options) {
            if (option instanceof DiscardCardDecision discardDecision) {
                if (discardDecision.getCard().getType() == Card.Type.BUG) {
                    return option;
                }
            }
        }

        // Second priority: Other Victory cards in hand (no effect to the phase)
        for (Decision option : options) {
            if (option instanceof DiscardCardDecision discardDecision) {
                if (discardDecision.getCard().getType().getCategory() == Card.Type.Category.VICTORY) {
                    return option;
                }
            }
        }

        // For BACKLOG: Consider discarding low-value cards to draw more
        if (isBacklogDiscard) {
            // If no more actions available, discard action cards since they can't be played
            if (state.getAvailableActions() < 1) {
                for (Decision option : options) {
                    if (option instanceof DiscardCardDecision discardDecision) {
                        if (discardDecision.getCard().getType().getCategory() == Card.Type.Category.ACTION) {
                            return option;
                        }
                    }
                }
            }

            // Mid/Late game: Discard low cost cards
            if (isMidGame() || isLateGame()) {
                for (Decision option : options) {
                    if (option instanceof DiscardCardDecision discardDecision) {
                        if (discardDecision.getCard().getType().getCost() <= 2) {
                            return option;
                        }
                    }
                }
            }
        } 
        // For forced discards
        else if (isForced) {
            // Go for lowest cost card
            Decision lowestCostOption = null;
            int lowestCost = Integer.MAX_VALUE;
            
            for (Decision option : options) {
                if (option instanceof DiscardCardDecision discardDecision) {
                    int cardCost = discardDecision.getCard().getType().getCost();
                    if (cardCost < lowestCost) {
                        lowestCost = cardCost;
                        lowestCostOption = option;
                    }
                }
            }
            
            if (lowestCostOption != null) {
                return lowestCostOption;
            }
        }

        // If nothing else matches, end phase if possible
        if (!isForced) {
            return options.stream()
                .filter(d -> d instanceof EndPhaseDecision)
                .findFirst()
                .orElse(options.get(0));
        }

        return options.get(0);
    }

    /**
     * Prioritizes action cards based on the current game phase.
     * Each phase has different priorities to maximize effectiveness.
     */
    private Decision handleActionPhase(ImmutableList<Decision> options, GameState state) {
        // Create priority lists based on game phase
        List<Card.Type> priorityActions;
        
        if (isEarlyGame()) {
            // Early game: focus on card draw and economy building
            priorityActions = Arrays.asList(
                Card.Type.IPO,                // Get money
                Card.Type.CODE_REVIEW,        // Card draw
                Card.Type.BACKLOG,            // Card churn through discards
                Card.Type.PARALLELIZATION,    // Additional actions
                Card.Type.HACK                // Flexible utility
            );
        } else if (isMidGame()) {
            // Mid game: balance economy with deck improvement
            priorityActions = Arrays.asList(
                Card.Type.PARALLELIZATION,    // More actions = more options
                Card.Type.IPO,                // Economy still important
                Card.Type.TECH_DEBT,          // Card improvement
                Card.Type.REFACTOR,           // Card upgrading for late game
                Card.Type.CODE_REVIEW,        // Card draw still valuable
                Card.Type.BACKLOG,            // Cycle through deck
                Card.Type.HACK,               // Flexibility
                Card.Type.EVERGREEN_TEST      // Card protection and draw
            );
        } else {
            // Late game: focus on maximizing points and final push
            priorityActions = Arrays.asList(
                Card.Type.IPO,                // Money for high-value cards
                Card.Type.PARALLELIZATION,    // More actions for final push
                Card.Type.CODE_REVIEW,        // Find victory cards
                Card.Type.EVERGREEN_TEST,     // Protect key cards
                Card.Type.HACK                // Flexible utility
            );
        }

        // Look for priority actions in hand
        for (Card.Type actionType : priorityActions) {
            for (Decision option : options) {
                if (option instanceof PlayCardDecision playDecision && 
                    playDecision.getCard().getType() == actionType) {
                    return option;
                }
            }
        }

        return options.get(0);
    }

    /**
     * Implements a phase-based buying strategy.
     * Adapts purchases based on available money, current deck composition,
     * and game phase to optimize long-term strategy.
     */
    private Decision handleBuyPhase(ImmutableList<Decision> options, GameState state) {
        int availableMoney = state.getSpendableMoney();
        // Track turn progression - last buy action updates turn count
        if (state.getAvailableBuys() == 1) {
            turnCount++;
        }
        
        // Strategic decision: Skip buying last Framework if not winning
        // This prevents ending the game when behind, giving more time to catch up
        if (!isWinning() && state.getDeck().getNumAvailable(Card.Type.FRAMEWORK) == 1) {
            options = ImmutableList.copyOf(options.stream()
                .filter(d -> !(d instanceof BuyDecision && ((BuyDecision)d).getCardType() == Card.Type.FRAMEWORK))
                .collect(Collectors.toList()));
        }

        // Early game buying strategy: focus on economy and basic engine pieces
        if (isEarlyGame()) {
            if (availableMoney >= 5) {
                // Prefer IPO or Module based on action card count
                if (countActionCards() < 2 && canBuy(options, Card.Type.IPO)) {
                    return findBuyDecision(options, Card.Type.IPO);
                }
                if (canBuy(options, Card.Type.MODULE)) {
                    return findBuyDecision(options, Card.Type.MODULE);
                }
            } else if (availableMoney >= 3) {
                if (countMoneyCards() < 10 && canBuy(options, Card.Type.ETHEREUM)) {
                    return findBuyDecision(options, Card.Type.ETHEREUM);
                }
            } else if (availableMoney >= 2) {
                return findBuyDecision(options, Card.Type.METHOD);
            } else {
                Decision decision = findBuyDecision(options, Card.Type.BITCOIN);
                return decision != null ? decision : options.get(0);
            }
        }
        // Mid game buying strategy: balance action cards, money and victory points
        else if (isMidGame()) {
            int actionCardCount = countActionCards();
            
            if (availableMoney >= 8) {
                // High priority on Framework cards - major victory points
                return findBuyDecision(options, Card.Type.FRAMEWORK);
            } else if (availableMoney >= 6) {
                // Balance between action cards and money
                if (actionCardCount < 5) {
                    if (canBuy(options, Card.Type.EVERGREEN_TEST)) {
                        return findBuyDecision(options, Card.Type.EVERGREEN_TEST);
                    }
                    if (canBuy(options, Card.Type.PARALLELIZATION)) {
                        return findBuyDecision(options, Card.Type.PARALLELIZATION);
                    }
                }
                // Improve money if needed
                if (countMoneyCards() < 15 && canBuy(options, Card.Type.DOGECOIN)) {
                    return findBuyDecision(options, Card.Type.DOGECOIN);
                }
            } else if (availableMoney >= 5) {
                if (actionCardCount < 6) {
                    // Prioritize key action cards at 5 cost
                    if (canBuy(options, Card.Type.IPO)) {
                        return findBuyDecision(options, Card.Type.IPO);
                    }
                    if (canBuy(options, Card.Type.DAILY_SCRUM)) {
                        return findBuyDecision(options, Card.Type.DAILY_SCRUM);
                    }
                }
                return findBuyDecision(options, Card.Type.MODULE);
            } else if (availableMoney >= 4) {
                // Get utility action cards if we don't have enough
                if (actionCardCount < 7) {
                    if (canBuy(options, Card.Type.TECH_DEBT)) {
                        return findBuyDecision(options, Card.Type.TECH_DEBT);
                    }
                    if (canBuy(options, Card.Type.HACK)) {
                        return findBuyDecision(options, Card.Type.HACK);
                    }
                }
            } else if (availableMoney >= 3 && actionCardCount < 8) {
                // Get cheap utility cards
                if (canBuy(options, Card.Type.CODE_REVIEW)) {
                    return findBuyDecision(options, Card.Type.CODE_REVIEW);
                }
            } else {
                if (canBuy(options, Card.Type.ETHEREUM)) {
                    return findBuyDecision(options, Card.Type.ETHEREUM);
                } else if (canBuy(options, Card.Type.METHOD)) {
                    return findBuyDecision(options, Card.Type.METHOD);
                } else if (canBuy(options, Card.Type.BITCOIN)) {
                    return findBuyDecision(options, Card.Type.BITCOIN);
                } else {
                    return options.get(0); // Fallback to first option if no priorities found
                }
            }
        }
        // Late game buying strategy: maximize victory points
        else {
            if (availableMoney >= 8) {
                return findBuyDecision(options, Card.Type.FRAMEWORK);
            } else if (availableMoney >= 5) {
                return findBuyDecision(options, Card.Type.MODULE);
            } else if (availableMoney >= 4) {
                if (canBuy(options, Card.Type.IPO)) {
                    return findBuyDecision(options, Card.Type.IPO);
                }
                if (canBuy(options, Card.Type.PARALLELIZATION)) {
                    return findBuyDecision(options, Card.Type.PARALLELIZATION);
                }
            } else if (availableMoney >= 3) {
                if (canBuy(options, Card.Type.DOGECOIN)) {
                    return findBuyDecision(options, Card.Type.DOGECOIN);
                }
            } else if (availableMoney >= 2) {
                if (canBuy(options, Card.Type.METHOD)) {
                    return findBuyDecision(options, Card.Type.METHOD);
                }
            } else {
                if (canBuy(options, Card.Type.BITCOIN)) {
                    return findBuyDecision(options, Card.Type.BITCOIN);
                }
                return options.get(0);
            }
        }

        return options.get(0); // Take first option if no priorities matched
    }

    /**
     * Prioritizes gained cards for maximum strategic value.
     * Used primarily with Refactor and other gain effects.
     */
    private Decision handleGainPhase(ImmutableList<Decision> options) {
        // For gained cards, prioritize in this order:
        Card.Type[] gainPriorities = {
            Card.Type.FRAMEWORK,    // Highest victory points
            Card.Type.MODULE,       // Good victory points
            Card.Type.DOGECOIN,     // Best money
            Card.Type.ETHEREUM,     // Good money
            Card.Type.METHOD        // Basic victory points
        };

        for (Card.Type type : gainPriorities) {
            for (Decision option : options) {
                if (option instanceof GainCardDecision && ((GainCardDecision)option).getCardType() == type) {
                    return option;
                }
            }
        }

        return options.get(0); // Take first option if no priorities found
    }

    // Helper methods for finding buy decisions
    private Decision findBuyDecision(ImmutableList<Decision> options, Card.Type type) {
        for (Decision option : options) {
            if (option instanceof BuyDecision && ((BuyDecision)option).getCardType() == type) {
                return option;
            }
        }
        return null;
    }

    private boolean canBuy(ImmutableList<Decision> options, Card.Type type) {
        return findBuyDecision(options, type) != null;
    }

    /**
     * Counts money cards across all decks to inform economic decisions.
     * Important for determining if basic money cards should be upgraded.
     */
    private int countMoneyCards() {
        int count = 0;
        for (Card card : hand.getAllCards()) {
            if (card.getType().getCategory() == Card.Type.Category.MONEY) {
                count++;
            }
        }
        // Also count cards in draw and discard piles
        for (Card card : drawDeck.getCards()) {
            if (card.getType().getCategory() == Card.Type.Category.MONEY) {
                count++;
            }
        }
        for (Card card : discardDeck.getCards()) {
            if (card.getType().getCategory() == Card.Type.Category.MONEY) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts action cards across all decks to balance actions vs. other card types.
     * Too few action cards limits options; too many reduces deck efficiency.
     */
    private int countActionCards() {
        int count = 0;
        for (Card card : hand.getAllCards()) {
            if (card.getType().getCategory() == Card.Type.Category.ACTION) {
                count++;
            }
        }
        // Also count cards in draw and discard piles
        for (Card card : drawDeck.getCards()) {
            if (card.getType().getCategory() == Card.Type.Category.ACTION) {
                count++;
            }
        }
        for (Card card : discardDeck.getCards()) {
            if (card.getType().getCategory() == Card.Type.Category.ACTION) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculates if the player is winning based on the current game state.
     * Used for strategic decisions like avoiding buying the last Framework when behind.
     */
    public boolean isWinning() {
        List<Player.ScorePair> scores = GameEngine.getCurrentScores();
        int myScore = 0;
        for (Player.ScorePair pair : scores) {
            if (pair.player.getName().equals(this.name)) {
                myScore = pair.getScore();
                break;
            }
        }
        for (Player.ScorePair pair : scores) {
            if (!pair.player.getName().equals(this.name) && pair.getScore() >= myScore) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<GameObserver> getObserver() {
        return observer;
    }
    
    @Override
    public DiscardDeck getDiscardDeck() {
        return this.discardDeck;
    }

    @Override
    public DrawDeck getDrawDeck() {
        return this.drawDeck;
    }

    @Override
    public Hand getHand() {
        return this.hand;
    }

    @Override
    public void setHand(Hand hand) {
        this.hand = hand;
    }
}
