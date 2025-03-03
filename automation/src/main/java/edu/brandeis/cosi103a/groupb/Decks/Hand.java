package edu.brandeis.cosi103a.groupb.Decks;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi103a.groupb.Cards.Card;

/**
 * Represents a player's hand in the game.
 */
public final class Hand {
    private final ImmutableList<Card> playedCards;
    private final ImmutableList<Card> unplayedCards;

    public Hand(List<Card> playedCards, List<Card> unplayedCards) {
        //System.out.println("DEBUG: Initializing hand with " + unplayedCards.size() + " unplayed cards.\n");
        this.playedCards = ImmutableList.copyOf(playedCards);
        this.unplayedCards = ImmutableList.copyOf(unplayedCards);
    }

    public ImmutableList<Card> getUnplayedCards() {
        //System.out.println("DEBUG: Retrieving unplayed cards -> " + unplayedCards);
        return unplayedCards;
    }

    public ImmutableList<Card> getPlayedCards() {
        return playedCards;
    }

    @Override
    public String toString() {
        return "Hand:\n" +
           "Unplayed Cards: " + unplayedCards + "\n" +
           "Played Cards: " + playedCards;
}
}
