package edu.brandeis.cosi103a.groupb;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.brandeis.cosi103a.groupb.Cards.Card;

public final class GameDeck {
    private final Map<Card.Type, Integer> cardCounts;

    public GameDeck(Map<Card.Type, Integer> cardCounts) {
        this.cardCounts = new HashMap<>(cardCounts); // ✅ Change to mutable HashMap
    }

    public ImmutableMap<Card.Type, Integer> getCardCounts() {
        return ImmutableMap.copyOf(cardCounts);
    }

    public ImmutableSet<Card.Type> getCardTypes() {
        return ImmutableSet.copyOf(cardCounts.keySet());
    }

    public int getNumAvailable(Card.Type cardType) {
        return cardCounts.getOrDefault(cardType, 0);
    }

    // ✅ New method: Draw a card from the deck
    public Card drawCard(Card.Type type) {
        if (cardCounts.getOrDefault(type, 0) > 0) {
            cardCounts.put(type, cardCounts.get(type) - 1);
            return new Card(type, (int) (Math.random() * 1000));
        }
        return null; // No cards left
    }

    public static void main(String args[]) {
        
    }
}
