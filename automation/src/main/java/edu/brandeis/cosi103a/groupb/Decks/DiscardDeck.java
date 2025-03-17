package edu.brandeis.cosi103a.groupb.Decks;

import edu.brandeis.cosi.atg.api.cards.*;

public class DiscardDeck extends PlayerDeck {
    
    public DiscardDeck() {
        super();
    }

    @Override
    public String toString() {
        return "Draw Deck: " + super.toString();
    }   

}