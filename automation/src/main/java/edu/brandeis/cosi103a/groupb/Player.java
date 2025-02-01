package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    private List<Card> deck;
    private List<Card> hand;
    private List<Card> discardPile;

    public Player() {
        deck = new ArrayList<>();
        hand = new ArrayList<>();
        discardPile = new ArrayList<>();
    }

    public void addCardToDeck(Card card) {
        deck.add(card);
    }

    public void drawHand() {
        if (deck.size() < 5) {
            reshuffle();
        }
        for (int i = 0; i < 5 && !deck.isEmpty(); i++) {
            hand.add(deck.remove(0));
        }
    }

    public void discardHand() {
        discardPile.addAll(hand);
        hand.clear();
    }

    private void reshuffle() {
        Collections.shuffle(discardPile);
        deck.addAll(discardPile);
        discardPile.clear();
    }

    public List<Card> getHand() {
        return hand;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public List<Card> getDiscardPile() {
        return discardPile;
    }
}
