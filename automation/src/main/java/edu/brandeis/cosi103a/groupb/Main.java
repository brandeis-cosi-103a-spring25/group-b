package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
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
            int highestScore = -1;
            List<Player> winners = new ArrayList<>();
            List<Player.ScorePair> results = gameEngine.play();

            System.out.println("\nGame Over! Final Scores:");
            for (Player.ScorePair score : results) {
                System.out.println(score.player.getName() + ": " + score.getScore() + " points");

                if (score.getScore() > highestScore) {
                    highestScore = score.getScore();
                    winners.clear();  // ✅ New highest score, reset winners list
                    winners.add(score.player);
                } else if (score.getScore() == highestScore) {
                    winners.add(score.player);  // ✅ Add player in case of a tie
                }
            }

            if (!winners.isEmpty()) {
                System.out.print("The winner(s): ");
                System.out.println(winners.stream().map(Player::getName).toList());
            } else {
                System.out.println("No winner, as no players have scores.");
            }
        } catch (PlayerViolationException e) {
            System.out.println("Game ended due to an invalid move: " + e.getMessage());
        }
    }
}
