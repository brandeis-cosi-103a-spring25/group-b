package edu.brandeis.cosi.atg.api.event;

import edu.brandeis.cosi.atg.api.Player;

public class EndTurnEvent extends Event {
    private final Player player;

    public EndTurnEvent(Player player) {
        super(player.getName() + " ended their turn");
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String getDescription() {
        return player.getName() + " ended their turn";
    }
}
