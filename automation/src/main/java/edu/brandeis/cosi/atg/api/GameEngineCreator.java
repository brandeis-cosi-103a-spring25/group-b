package edu.brandeis.cosi.atg.api;

import edu.brandeis.cosi.atg.api.event.GameObserver;

public class GameEngineCreator {

    @EngineCreator
    public static Engine createEngine(Player player1, Player player2, GameObserver observer) {
        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        return new GameEngine(players, observer);
    }
}