package edu.brandeis.cosi.atg.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.api.cards.Card;

public final class GameDeck {
    private final ImmutableMap<Card.Type, Integer> cardCounts;

    public GameDeck(ImmutableMap<Card.Type, Integer> cardCounts) {
        this.cardCounts = cardCounts;
    }

    public ImmutableMap<Card.Type, Integer> getCardCounts() {
        return cardCounts;
    }

    public int getNumAvailable(Card.Type cardType) {
        return cardCounts.getOrDefault(cardType, 0);
    }

    public ImmutableSet<Card.Type> getCardTypes() {
        return cardCounts.keySet();
    }
}