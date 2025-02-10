package edu.brandeis.cosi.atg.api.decisions;

import edu.brandeis.cosi.atg.api.cards.Card;

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
        return "Buy " + cardType.getDescription();
    }

    @Override
    public String toString() {
        return "BuyDecision{" +
                "cardType=" + cardType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuyDecision that = (BuyDecision) o;

        return cardType == that.cardType;
    }

    @Override
    public int hashCode() {
        return cardType.hashCode();
    }
}
