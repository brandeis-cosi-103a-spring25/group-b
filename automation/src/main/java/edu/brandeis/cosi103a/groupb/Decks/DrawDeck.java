package edu.brandeis.cosi103a.groupb.Decks;

import edu.brandeis.cosi103a.groupb.Cards.Card;

public class DrawDeck extends PlayerDeck {
    
    public DrawDeck() {
        super();
    }

    @Override
    public void printDeck() {
        System.out.println("Draw Deck: ");
        for (Card card : deck) {
            System.out.println(card);
        }
    }        
}
