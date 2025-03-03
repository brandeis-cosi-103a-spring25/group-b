package edu.brandeis.cosi103a.groupb.Decisions;

import edu.brandeis.cosi103a.groupb.Cards.Card;

/**
 * Represents a decision to buy a card.
 */
public final class BuyDecision implements Decision {
    private final Card.Type cardType;

    public BuyDecision(Card.Type cardType) {
        this.cardType = cardType;
    }

    public Card.Type getCardType() {
        return cardType;
    }

    @Override
    public String getDescription() {
        return "Buy " + cardType;
    }
}
