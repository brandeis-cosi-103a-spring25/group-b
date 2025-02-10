package edu.brandeis.cosi.atg.api;

import edu.brandeis.cosi.atg.api.cards.Card;
import com.google.common.collect.ImmutableCollection;

public final class GameState {
    private final String currentPlayerName;
    private final Hand currentPlayerHand;
    private final TurnPhase phase;
    private final int spendableMoney;
    private final int availableBuys;
    private final GameDeck deck;

    public GameState(String currentPlayerName, Hand currentPlayerHand, TurnPhase phase, int spendableMoney, int availableBuys, GameDeck deck) {
        this.currentPlayerName = currentPlayerName;
        this.currentPlayerHand = currentPlayerHand;
        this.phase = phase;
        this.spendableMoney = spendableMoney;
        this.availableBuys = availableBuys;
        this.deck = deck;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public Hand getCurrentPlayerHand() {
        return currentPlayerHand;
    }

    public int getSpendableMoney() {
        return spendableMoney;
    }

    public int getAvailableBuys() {
        return availableBuys;
    }

    public GameDeck getDeck() {
        return deck;
    }

    public TurnPhase getTurnPhase() {
        return phase;
    }

    public enum TurnPhase {
        MONEY,
        BUY,
        CLEANUP;

        public static TurnPhase[] values() {
            return new TurnPhase[]{MONEY, BUY, CLEANUP};
        }

        public static TurnPhase valueOf(String name) {
            switch (name) {
                case "MONEY":
                    return MONEY;
                case "BUY":
                    return BUY;
                case "CLEANUP":
                    return CLEANUP;
                default:
                    throw new IllegalArgumentException("No enum constant " + name);
            }
        }
    }
}