package edu.brandeis.cosi103a.groupb.Player;

import java.util.Optional;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi.atg.api.decisions.BuyDecision;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.api.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.Player;


/**
 * Represents an automated player, following the basic strategy.
 */
public interface AtgPlayer extends Player {
    
    public DiscardDeck getDiscardDeck();
    public DrawDeck getDrawDeck();
    
}
