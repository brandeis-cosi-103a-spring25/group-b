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
public class BigMoneyPlayer implements AtgPlayer {
    private final String name;
    private DiscardDeck discardDeck = new DiscardDeck();
    private DrawDeck drawDeck = new DrawDeck();
    private Hand hand = new Hand(
                ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty)
                ImmutableList.copyOf(new ArrayList<Card>())  // Unplayed cards (empty)
            );

    private final Optional<GameObserver> observer;

    public BigMoneyPlayer(String name) {
        this.name = name;
        this.observer = Optional.of((GameObserver) new ConsoleGameObserver());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        if (reason.isPresent()) {
            System.out.println("Decision prompted by event: " + reason);
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
            if (option instanceof BuyDecision buy) {
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
            }
        }
        if (bestBuy != null) return bestBuy;

        int lowestValue = -1;
        Decision bestTrash = null;
        for (Decision option: options) {
            if (option instanceof TrashCardDecision trash) {  // Trash card phase logic (so far) -- throw away the card with the lowest value, either AP or purchase power
                if (trash.getCard().getValue() < lowestValue) {
                    lowestValue = trash.getCard().getValue();
                    bestTrash = trash;
                }
            }
        }
        if (bestTrash != null) return bestTrash;

        int lowestVal = -1;
        Decision bestDiscard = null;
        for (Decision option: options) {
            if (option instanceof DiscardCardDecision discard) {
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
        
        // If no buy option qualifies, check for an EndPhaseDecision.
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                return option;
            }
        }

        // int highestValue = 0;
        // Decision bestGain = null;
        // for (Decision option: options) {
        //     if (option instanceof GainCardDecision gain) {
        //         if (gain.getCardType().getValue() > highestValue) {
        //             highestValue = gain.getCardType().getValue();
        //             bestGain = gain;
        //         }
        //     }
        // }
        // if (bestGain != null) return bestGain;

        for (Decision option: options) {
            if (option instanceof GainCardDecision gain) {
                return gain;
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
