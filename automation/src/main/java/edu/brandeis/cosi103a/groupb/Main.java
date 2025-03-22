package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
import java.util.List;

import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi.atg.api.Engine;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.PlayerViolationException;
import edu.brandeis.cosi.atg.api.Player;

public class Main {
    public static void main(String[] args) {
        // Step 1: Create players
        AtgPlayer player1 = new BigMoneyPlayer("Bot1");
        AtgPlayer player2 = new BigMoneyPlayer("Bot2");

        // Step 2: Create a game observer
        GameObserver observer = new ConsoleGameObserver();

        // Step 3: Create the game engine
        Engine gameEngine = GameEngine.createEngine(player1, player2, observer);

        try {
            // Step 4: Start the game and get the results
            List<Player.ScorePair> results = gameEngine.play();

            // Step 5: Display the final scores
            System.out.println("\nGame Over! Final Scores:");
            int highestScore = -1;
            List<AtgPlayer> winners = new ArrayList<>();
            for (Player.ScorePair score : results) {
                System.out.println(score.player.getName() + ": " + score.getScore() + " points");

                // Determine the highest score and winners
                if (score.getScore() > highestScore) {
                    highestScore = score.getScore();
                    winners.clear();
                    winners.add((AtgPlayer) score.player);
                } else if (score.getScore() == highestScore) {
                    winners.add((AtgPlayer) score.player);
                }
            }

            // Step 6: Announce the winner(s)
            if (!winners.isEmpty()) {
                System.out.print("The winner(s): ");
                System.out.println(winners.stream().map(AtgPlayer::getName).toList());
            } else {
                System.out.println("No winner, as no players have scores.");
            }
        } catch (PlayerViolationException e) {
            // Handle invalid moves
            System.out.println("Game ended due to an invalid move: " + e.getMessage());
        }
    }
}