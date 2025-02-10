package edu.brandeis.cosi.atg.api.event;

import edu.brandeis.cosi.atg.api.cards.Card;

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
        return playerName + " played " + card.getDescription();
    }
}
