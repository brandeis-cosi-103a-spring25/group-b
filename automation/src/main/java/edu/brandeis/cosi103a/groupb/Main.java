package edu.brandeis.cosi103a.groupb;

import java.util.List;

import edu.brandeis.cosi103a.groupb.Player.Player;
import edu.brandeis.cosi103a.groupb.Player.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.Engine;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Game.GameObserver;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;

public class Main {
    public static void main(String[] args) {
        Player player1 = new HumanPlayer("Alice");
        Player player2 = new BigMoneyPlayer("Bot");

        GameObserver observer = (GameObserver) new ConsoleGameObserver(); // Fix applied here
        Engine gameEngine = new GameEngine(player1, player2, observer);

        try {
            List<Player.ScorePair> results = gameEngine.play();
            System.out.println("\nGame Over! Final Scores:");
            for (Player.ScorePair score : results) {
                System.out.println(score.player.getName() + ": " + score.getScore() + " points");
            }
        } catch (PlayerViolationException e) {
            System.out.println("Game ended due to an invalid move: " + e.getMessage());
        }
    }
}
