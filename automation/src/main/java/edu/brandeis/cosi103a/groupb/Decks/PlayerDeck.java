package edu.brandeis.cosi103a.groupb.Decks;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import edu.brandeis.cosi.atg.api.cards.*;

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

    public void moveDeck(PlayerDeck newDeck) {
        if (this.isEmpty()) return;
        this.shuffle();

        while (!this.isEmpty()) { 
            Card card = this.drawCard();
            if (card != null) {
                newDeck.addCard(card);
            }
        }
    }

    /**
     * Put all cards from the list into this deck
     * @param list: from which all cards are put into this.deck
     */
    public void addAllCards(List<Card> list) {
        for (Card card: list) {
            this.deck.push(card);
        }
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
