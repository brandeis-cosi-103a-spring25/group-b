package edu.brandeis.cosi.atg.api.decisions;

import edu.brandeis.cosi.atg.api.cards.Card;

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
        return "Play " + card.getDescription();
    }

    @Override
    public String toString() {
        return "PlayCardDecision{" +
                "card=" + card +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayCardDecision that = (PlayCardDecision) o;

        return card.equals(that.card);
    }

    @Override
    public int hashCode() {
        return card.hashCode();
    }
}
