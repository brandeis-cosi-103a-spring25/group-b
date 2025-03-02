package edu.brandeis.cosi103a.groupb.Decks;

import java.util.Collections;
import java.util.Stack;

import edu.brandeis.cosi103a.groupb.Cards.Card;

public abstract class PlayerDeck {
    protected Stack<Card> deck;

    public PlayerDeck() {
        deck = new Stack<>();
    }

    // Add a card to the discard pile
    public void addCard(Card card) {
        deck.push(card);
    }

    // Remove and return a card from the discard pile
    public Card drawCard() {
        if (!deck.isEmpty()) {
            return deck.pop();
        }
        return null; // No cards left
    }

    // Shuffle the discard pile
    public void shuffle() {
        Collections.shuffle(deck);
    }

    // Check if discard pile is empty
    public boolean isEmpty() {
        return deck.isEmpty();
    }

    public int size() {
        return deck.size();
    }

    public void printDeck() {
        System.out.println("This Deck: ");
        for (Card card : deck) {
            System.out.println(card); // Assumes Card has a toString() method
        }
    }
}
