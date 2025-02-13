package edu.brandeis.cosi103a.groupb.Decisions;

import edu.brandeis.cosi103a.groupb.Cards.Card;

/**
 * Represents a decision to play a card.
 */
public final class PlayCardDecision implements Decision {
    private final Card card;

    public PlayCardDecision(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    @Override
    public String getDescription() {
        return "Play " + card.getType();
    }
}
