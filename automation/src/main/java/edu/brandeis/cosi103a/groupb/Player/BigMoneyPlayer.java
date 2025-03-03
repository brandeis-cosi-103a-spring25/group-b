package edu.brandeis.cosi103a.groupb.Player;

import java.util.List;
import java.util.Optional;

import edu.brandeis.cosi103a.groupb.Decisions.BuyDecision;
import edu.brandeis.cosi103a.groupb.Decisions.Decision;
import edu.brandeis.cosi103a.groupb.Decisions.EndPhaseDecision;
import edu.brandeis.cosi103a.groupb.Decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
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

        // Always buy the most expensive card in the BUY phase -- which is framework if present.
        Decision bestBuy = null;
        int highestCost = 0;
        for (Decision option : options) {
            if (option instanceof BuyDecision) {
                BuyDecision buy = (BuyDecision) option;
                if (buy.getCardType().getValue() > highestCost) {
                    highestCost = buy.getCardType().getValue();
                    bestBuy = buy;
                }
            }
        }
        if (bestBuy != null) return bestBuy;

        // If no actions left, end phase
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                return option;
            }
        }

        throw new IllegalStateException("Big Money Player could not find a valid decision.");
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
