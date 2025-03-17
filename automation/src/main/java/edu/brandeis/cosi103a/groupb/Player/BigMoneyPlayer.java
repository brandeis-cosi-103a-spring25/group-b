package edu.brandeis.cosi103a.groupb.Player;

import java.util.Optional;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Game.GameObserverImpl;
import edu.brandeis.cosi.atg.api.decisions.BuyDecision;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.api.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;


/**
 * Represents an automated player, following the basic strategy.
 */
public class BigMoneyPlayer implements AtgPlayer {
    private final String name;
    private DiscardDeck discardDeck = new DiscardDeck();
    private DrawDeck drawDeck = new DrawDeck();

    private final Optional<GameObserver> observer;

    public BigMoneyPlayer(String name) {
        this.name = name;
        this.observer = Optional.of(new GameObserverImpl());

    }

    @Override
    public String getName() {
        return name;
    }

    //api changes applied here; fix later.
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        if (reason.isPresent()) {
            System.out.println("Decision prompted by event: " + reason);
        }
        
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
    
    public DiscardDeck getDiscardDeck() {
        return this.discardDeck;
    }

    public DrawDeck getDrawDeck() {
        return this.drawDeck;
    }
}