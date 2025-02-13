package edu.brandeis.cosi103a.groupb.Events;

import edu.brandeis.cosi103a.groupb.Cards.Card;

/**
 * Represents a player playing a card.
 */
public final class PlayCardEvent implements Event {
    private final Card card;
    private final String playerName;

    public PlayCardEvent(Card card, String playerName) {
        this.card = card;
        this.playerName = playerName;
    }

    public Card getCard() {
        return card;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String getDescription() {
        return playerName + " played " + card.getType();
    }
}