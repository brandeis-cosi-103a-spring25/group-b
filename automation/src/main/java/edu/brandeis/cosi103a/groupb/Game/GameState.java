package edu.brandeis.cosi103a.groupb.Game;

import edu.brandeis.cosi103a.groupb.Decks.GameDeck;
import edu.brandeis.cosi103a.groupb.Decks.Hand;

public final class GameState {
    private final String currentPlayerName;
    private final Hand currentPlayerHand;
    private final TurnPhase phase;
    private final int spendableMoney;
    private final int availableBuys;
    private final GameDeck deck;

    public GameState(String currentPlayerName, Hand currentPlayerHand, TurnPhase phase, 
                     int spendableMoney, int availableBuys, GameDeck deck) {
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

    public TurnPhase getTurnPhase() {
        return phase;
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

    public enum TurnPhase {
        MONEY, BUY, CLEANUP;
    }
}
