package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
import java.util.List;

import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;
import edu.brandeis.cosi103a.groupb.AtgPlayer.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.Engine;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Game.GameObserver;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;

public class Main {
    public static void main(String[] args) {
        AtgPlayer player1 = new HumanPlayer("Alice");
        AtgPlayer player2 = new BigMoneyPlayer("Bot");

        GameObserver observer = (GameObserver) new ConsoleGameObserver(); // Fix applied here
        Engine gameEngine = new GameEngine(player1, player2, observer);

        try {
            int highestScore = -1;
            List<AtgPlayer> winners = new ArrayList<>();
            List<AtgPlayer.ScorePair> results = gameEngine.play();

            System.out.println("\nGame Over! Final Scores:");
            for (AtgPlayer.ScorePair score : results) {
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
                System.out.println(winners.stream().map(AtgPlayer::getName).toList());
            } else {
                System.out.println("No winner, as no players have scores.");
            }
        } catch (edu.brandeis.cosi.atg.api.PlayerViolationException e) {
            System.out.println("Game ended due to an invalid move: " + e.getMessage());
        }
    }
}
