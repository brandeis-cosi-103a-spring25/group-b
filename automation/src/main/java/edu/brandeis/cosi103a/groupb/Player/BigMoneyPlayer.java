package edu.brandeis.cosi103a.groupb.Player;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi103a.groupb.Decisions.BuyDecision;
import edu.brandeis.cosi103a.groupb.Decisions.Decision;
import edu.brandeis.cosi103a.groupb.Decisions.EndPhaseDecision;
import edu.brandeis.cosi103a.groupb.Decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameState;

/**
 * Represents an AI-controlled player.
 */
public class BigMoneyPlayer implements Player {
    private final String name;
    private final Optional<GameObserver> observer;

    public BigMoneyPlayer(String name) {
        this.name = name;
        this.observer = Optional.of((GameObserver) new ConsoleGameObserver());  // Fix applied here
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

        // Always buy the most expensive card in the BUY phase
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

        throw new IllegalStateException("AIPlayer could not find a valid decision.");
    }

    @Override
    public Optional<GameObserver> getObserver() {
        return observer;
    }
}
