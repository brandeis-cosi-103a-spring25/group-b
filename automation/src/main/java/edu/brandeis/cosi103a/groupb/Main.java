package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi.atg.api.Engine;
import edu.brandeis.cosi.atg.api.GameDeck;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.PlayerViolationException;
import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi.atg.api.cards.Card;

public class Main {
    public static void main(String[] args) {
        // Step 1: Create players
        AtgPlayer player1 = new HumanPlayer("Alice");
        AtgPlayer player2 = new BigMoneyPlayer("Bot");

        // Step 2: Create a game observer
        GameObserver observer = new ConsoleGameObserver();

        // Step 3: Create the game deck
        GameDeck deck = new GameDeck(ImmutableMap.of(
            Card.Type.BITCOIN, 60,
            Card.Type.ETHEREUM, 40,
            Card.Type.DOGECOIN, 30,
            Card.Type.METHOD, 14,
            Card.Type.MODULE, 8,
            Card.Type.FRAMEWORK, 8
        ));

        // Step 4: Create the game engine
        Engine gameEngine = new GameEngine(player1, player2, observer, deck);

        try {
            // Step 5: Start the game and get the results
            List<Player.ScorePair> results = gameEngine.play();

            // Step 6: Display the final scores
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

            // Step 7: Announce the winner(s)
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