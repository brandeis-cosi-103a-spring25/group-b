package edu.brandeis.cosi103a.groupb.Decks;

import edu.brandeis.cosi.atg.api.cards.*;

public class DiscardDeck extends PlayerDeck {
    
    public DiscardDeck() {
        super();
    }

    @Override
    public void printDeck() {
        System.out.println("Discard Deck: ");
        for (Card card : deck) {
            System.out.println(card);
        }
    }    

}