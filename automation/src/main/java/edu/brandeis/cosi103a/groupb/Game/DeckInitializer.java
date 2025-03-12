package edu.brandeis.cosi103a.groupb.Game;

import edu.brandeis.cosi103a.groupb.Cards.Card;
import edu.brandeis.cosi103a.groupb.Decks.GameDeck;

import java.util.HashMap;
import java.util.Map;

public class DeckInitializer {
    public GameDeck initializeDeck() {
        Map<Card.Type, Integer> deckMap = new HashMap<>();
        deckMap.put(Card.Type.BITCOIN, 60);
        deckMap.put(Card.Type.ETHEREUM, 40);
        deckMap.put(Card.Type.DOGECOIN, 30);
        deckMap.put(Card.Type.METHOD, 14);
        deckMap.put(Card.Type.MODULE, 8);
        deckMap.put(Card.Type.FRAMEWORK, 8);
        return new GameDeck(deckMap);
    }
}