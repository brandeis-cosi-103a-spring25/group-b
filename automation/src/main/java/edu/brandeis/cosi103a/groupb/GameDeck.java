package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi103a.groupb.Cards.Card;
import java.util.*;

public final class GameDeck {
    private final List<Card> cards;

    public GameDeck(Map<Card.Type, Integer> cardCounts) {
        this.cards = new ArrayList<>();
        for (Map.Entry<Card.Type, Integer> entry : cardCounts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                cards.add(new Card(entry.getKey(), i));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(cards.size() - 1);
    }

    public void reshuffle(List<Card> discardPile) {
        cards.addAll(discardPile);
        shuffle();
    }

    public int getNumAvailable(Card.Type type) {
        int count = 0;
        for (Card card : cards) {
            if (card.getType() == type) {
                count++;
            }
        }
        return count;
    }

    public Set<Card.Type> getCardTypes() {
        Set<Card.Type> types = new HashSet<>();
        for (Card card : cards) {
            types.add(card.getType());
        }
        return types;
    }
}
