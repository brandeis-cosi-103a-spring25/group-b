package edu.brandeis.cosi.atg.api;

import com.google.common.collect.ImmutableCollection;
import edu.brandeis.cosi.atg.api.cards.Card;

public final class Hand {
    private final ImmutableCollection<Card> playedCards;
    private final ImmutableCollection<Card> unplayedCards;

    public Hand(ImmutableCollection<Card> playedCards, ImmutableCollection<Card> unplayedCards) {
        this.playedCards = playedCards;
        this.unplayedCards = unplayedCards;
    }

    public ImmutableCollection<Card> getAllCards() {
        return ImmutableCollection.<Card>builder().addAll(playedCards).addAll(unplayedCards).build();
    }

    public ImmutableCollection<Card> getPlayedCards() {
        return playedCards;
    }

    public ImmutableCollection<Card> getUnplayedCards() {
        return unplayedCards;
    }
}