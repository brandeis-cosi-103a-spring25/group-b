package edu.brandeis.cosi103a.groupb.Decks;


public class DiscardDeck extends PlayerDeck {
    
    public DiscardDeck() {
        super();
    }

    @Override
    public String toString() {
        return "Discard Deck: " + super.toString();
    }   

}