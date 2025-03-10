package edu.brandeis.cosi103a.groupb.Player;

import java.util.List;
import java.util.Optional;

import edu.brandeis.cosi103a.groupb.Cards.Card;
import edu.brandeis.cosi103a.groupb.Decisions.BuyDecision;
import edu.brandeis.cosi103a.groupb.Decisions.Decision;
import edu.brandeis.cosi103a.groupb.Decisions.EndPhaseDecision;
import edu.brandeis.cosi103a.groupb.Decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Game.GameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameState;

/**
 * Represents an automated player, following the basic strategy.
 */
public class BigMoneyPlayer implements Player {
    private final String name;
    private DiscardDeck discardDeck = new DiscardDeck();
    private DrawDeck drawDeck = new DrawDeck();

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
    public Decision makeDecision(GameState state, List<Decision> options) {
        // Always play all available money cards in the MONEY phase
        for (Decision option : options) {
            if (option instanceof PlayCardDecision) {
                return option;
            }
        }
        
        // BUY phase logic with advanced strategy.
        Decision bestBuy = null;
        int highestCost = 0;
        for (Decision option : options) {
            if (option instanceof BuyDecision) {
                BuyDecision buy = (BuyDecision) option;
                // Implemented advanced Strategy 2: Avoid purchasing the last Framework if not winning.
                if (buy.getCardType() == Card.Type.FRAMEWORK) {
                    int remainingFramework = state.getDeck().getNumAvailable(Card.Type.FRAMEWORK);
                    if (remainingFramework == 1 && !isWinning(state)) {
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
        
        // If no buy option qualifies, check for an EndPhaseDecision.
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
    public boolean isWinning(GameState state) {
        List<Player.ScorePair> scores = GameEngine.getCurrentScores();
        int myScore = 0;
        for (Player.ScorePair pair : scores) {
            if (pair.getPlayer().getName().equals(this.name)) {
                myScore = pair.getScore();
                break;
            }
        }
        for (Player.ScorePair pair : scores) {
            if (!pair.getPlayer().getName().equals(this.name) && pair.getScore() >= myScore) {
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
}
