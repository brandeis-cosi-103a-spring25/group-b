package edu.brandeis.cosi103a.groupb.Player;

import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;

/**
 * Interface representing a player in the ATG card game.
 * Extends the base Player interface with additional methods
 * for managing decks and hand.
 */
public interface AtgPlayer extends Player {
    
    /**
     * Gets the player's discard deck.
     * 
     * @return The player's discard deck
     */
    public DiscardDeck getDiscardDeck();
    
    /**
     * Gets the player's draw deck.
     * 
     * @return The player's draw deck
     */
    public DrawDeck getDrawDeck();
    
    /**
     * Gets the player's current hand.
     * 
     * @return The player's hand
     */
    public Hand getHand();
    
    /**
     * Sets the player's hand.
     * 
     * @param hand The new hand to set
     */
    public void setHand(Hand hand);
}
