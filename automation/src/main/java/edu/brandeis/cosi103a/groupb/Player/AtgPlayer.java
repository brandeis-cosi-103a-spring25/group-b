package edu.brandeis.cosi103a.groupb.Player;

import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;


/**
 * Represents an automated player, following the basic strategy.
 */
public interface AtgPlayer extends Player {
    
    public DiscardDeck getDiscardDeck();
    public DrawDeck getDrawDeck();
    public Hand getHand();
    public void setHand(Hand hand);
    
}
