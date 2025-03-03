package edu.brandeis.cosi103a.groupb.Decks;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.brandeis.cosi103a.groupb.Cards.Card;

public final class GameDeck {
    private final Map<Card.Type, Integer> cardCounts;

    /**
     * Copy a mutable hashmap to immutable deck in this constructor. Preventing outside changes
     * @param cardCounts mutable card map
     */
    public GameDeck(Map<Card.Type, Integer> cardCounts) {
        this.cardCounts = new HashMap<>(cardCounts);
    }
    
    /**
     * @return Gets the number of available cards of each type.
     * ^^^E.g. {BITCOIN - 5; FRAMEWORK - 6.}
     */
    public ImmutableMap<Card.Type, Integer> getCardCounts() {
        return ImmutableMap.copyOf(cardCounts);
    }

    /**
     * @return a set of available card types inside the deck
     */
    public ImmutableSet<Card.Type> getCardTypes() {
        return ImmutableSet.copyOf(cardCounts.keySet());
    }

    /**
     * Checks how many cards of a given type are left in the deck.
     * @param cardType given type
     * @return given type left
     */
    public int getNumAvailable(Card.Type cardType) {
        return cardCounts.getOrDefault(cardType, 0);
    }

    //Draw a card from this deck
    public Card drawCard(Card.Type type) {
        if (cardCounts.getOrDefault(type, 0) > 0) {
            cardCounts.put(type, cardCounts.get(type) - 1); //One less card.
            return new Card(type, (int) (Math.random() * 1000));
        }
        return null; // If there are no cards in the beginning, no card can be drawn
    }

    /**
     * Print out deck information.
     * @return: deck information.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Card.Type, Integer> Entry: cardCounts.entrySet()) {
            sb.append(Entry.getKey()).append(" -- ").append(Entry.getValue()).append(" (Cost: ").append(Entry.getKey().getCost()).append(", Value: ").append(Entry.getKey().getValue()).append(");\n");
        }
        return sb.toString();
    }
}
