package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi103a.groupb.Decisions.*;
import edu.brandeis.cosi103a.groupb.ConsoleGameObserver;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        Player player1 = new HumanPlayer("Alice");
        Player player2 = new AIPlayer("Bot");

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
