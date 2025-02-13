package edu.brandeis.cosi103a.groupb.Events;

import edu.brandeis.cosi103a.groupb.Cards.Card;

/**
 * Represents a player gaining a card.
 */
public final class GainCardEvent implements Event {
    private final Card.Type cardType;
    private final String playerName;

    public GainCardEvent(Card.Type cardType, String playerName) {
        this.cardType = cardType;
        this.playerName = playerName;
    }

    public Card.Type getCardType() {
        return cardType;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String getDescription() {
        return playerName + " gained " + cardType;
    }
}
