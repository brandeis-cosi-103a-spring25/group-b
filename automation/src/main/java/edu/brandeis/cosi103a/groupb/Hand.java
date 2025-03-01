package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi103a.groupb.Cards.Card;
import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Represents a player's hand in the game.
 */
public final class Hand {
    private final ImmutableList<Card> playedCards;
    private final ImmutableList<Card> unplayedCards;

    public Hand(List<Card> playedCards, List<Card> unplayedCards) {
        System.out.println("DEBUG: Initializing hand with " + unplayedCards.size() + " unplayed cards.");
        this.playedCards = ImmutableList.copyOf(playedCards);
        this.unplayedCards = ImmutableList.copyOf(unplayedCards);
    }

    public ImmutableList<Card> getUnplayedCards() {
        System.out.println("DEBUG: Retrieving unplayed cards -> " + unplayedCards);
        return unplayedCards;
    }
    public ImmutableList<Card> getPlayedCards() {
        return playedCards;
    }
}
