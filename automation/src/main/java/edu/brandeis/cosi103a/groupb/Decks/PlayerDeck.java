package edu.brandeis.cosi103a.groupb.Decks;

import java.util.Collections;
import java.util.Stack;

import edu.brandeis.cosi103a.groupb.Cards.Card;

public abstract class PlayerDeck {
    protected Stack<Card> discardDeck;

    public PlayerDeck() {
        discardDeck = new Stack<>();
    }

    // Add a card to the discard pile
    public void addCard(Card card) {
        discardDeck.push(card);
    }

    // Remove and return a card from the discard pile
    public Card drawCard() {
        if (!discardDeck.isEmpty()) {
            return discardDeck.pop();
        }
        return null; // No cards left
    }

    // Shuffle the discard pile
    public void shuffle() {
        Collections.shuffle(discardDeck);
    }

    // Check if discard pile is empty
    public boolean isEmpty() {
        return discardDeck.isEmpty();
    }

    // Move discard pile into the deck when deck is empty
    // public Stack<Card> transferToDeck() {
    //     this.shuffle();
    //     Stack<Card> temp = this.discardDeck;
    //     this.discardDeck = new Stack<>();  // Reset discard pile
    //     return temp; // Return shuffled discard pile as new deck, and the draw deck will take the returned type as new.
    // }

    public void printDeck() {
        System.out.println("This Deck: ");
        for (Card card : discardDeck) {
            System.out.println(card); // Assumes Card has a toString() method
        }
    }
}
