package edu.brandeis.cosi103a.groupb.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
 * Represents an automated player, following the basic strategy.
 */
public class RedEyePlayer implements AtgPlayer {
    private int cardsBought;
    private int moneyCards;

    private final String name;
    private DiscardDeck discardDeck = new DiscardDeck();
    private DrawDeck drawDeck = new DrawDeck();
    private Hand hand = new Hand(
                ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty)
                ImmutableList.copyOf(new ArrayList<Card>())  // Unplayed cards (empty)
            );

    private final Optional<GameObserver> observer;

    public RedEyePlayer (String name) {
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

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        if (reason.isPresent()) {
            System.out.println("Decision prompted by event: " + reason);
        }

        // Plays whatever action cards so far
        for (Decision option : options) {
            if (option instanceof PlayCardDecision && state.getTurnPhase() == GameState.TurnPhase.REACTION) {
                return option;
            }
        }

        // Action strategies
        for (Decision option : options) {
            if (option instanceof PlayCardDecision decision && state.getTurnPhase() == GameState.TurnPhase.ACTION) {
                if (countCardCategory(hand, Card.Type.Category.MONEY) <= 2 && decision.getCard().getType() == Card.Type.BACKLOG) {
                    return option; // Attempts a save
                } else if (countCardCategory(hand, Card.Type.Category.MONEY) <= 1 && decision.getCard().getType() == Card.Type.HACK) {
                    return option; // Mutual destruction
                }
            }
        }


        // Always play all available money cards in the MONEY phase
        for (Decision option : options) {
            if (option instanceof PlayCardDecision && state.getTurnPhase() == GameState.TurnPhase.MONEY) {
                return option;
            }
        }
        
        // BUY phase logic with advanced strategy.
        Decision bestBuy = null;
        int highestCost = 0;
        for (Decision option : options) {
            if (option instanceof BuyDecision buy && state.getTurnPhase() == GameState.TurnPhase.BUY) {

                // Implemented advanced Strategy 2: Avoid purchasing the last Framework if not winning.
                if (buy.getCardType() == Card.Type.FRAMEWORK) {
                    int remainingFramework = state.getDeck().getNumAvailable(Card.Type.FRAMEWORK);
                    if (remainingFramework == 1 && !isWinning()) {
                        // Skip buying framework even if it is normally the most expensive.
                        continue;
                    }
                }
                int cost = buy.getCardType().getValue();
                if (cost > highestCost) {
                    highestCost = cost;
                    bestBuy = buy;
                }

                // Action strategy1: This double can be a parameter later
                if (cardsBought > 0 && moneyCards/ (double) cardsBought < 0.3) {
                    if (buy.getCardType() == Card.Type.BACKLOG) {
                        bestBuy = buy;
                        break;
                    }
                }

                // Action strategy2: This double can be a parameter later
                if (cardsBought > 0 && moneyCards/ (double) cardsBought < 0.2) {
                    if (buy.getCardType() == Card.Type.HACK) {
                        bestBuy = buy;
                        break;
                    }
                }
            }
        }
        if (bestBuy != null) {
            cardsBought++;
            if (((BuyDecision)bestBuy).getCardType().getCategory() == Card.Type.Category.MONEY) {
                moneyCards++;
            }
            return bestBuy;
        }

        int lowestVal = -1;
        Decision bestDiscard = null;
        for (Decision option: options) {
            if (option instanceof DiscardCardDecision discard && state.getTurnPhase() == GameState.TurnPhase.DISCARD) {
                if (discard.getCard().getCategory() == Card.Type.Category.VICTORY) { //Victory points don't do anything in the hand
                    return discard;
                } else {
                    if (discard.getCard().getValue() < lowestVal) {
                        lowestVal = discard.getCard().getValue();
                            bestDiscard = discard;
                    }
                }
            }
        }
        if (bestDiscard != null) return bestDiscard;

        for (Decision option: options) {
            if (option instanceof GainCardDecision gain && state.getTurnPhase() == GameState.TurnPhase.GAIN) {
                return gain;
            }
        }

        // If no other option qualifies, check for an EndPhaseDecision.
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                return option;
            }
        }
        
        throw new IllegalStateException("Big Money Player could not find a valid decision.");
    }

    /**
     * Calculates if the player is winning based on the current game state.
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
