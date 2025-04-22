package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.brandeis.cosi.atg.api.Engine;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi.atg.api.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Player.FinalBossPlayer;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;
import edu.brandeis.cosi103a.groupb.Player.RedEyePlayer;
import edu.brandeis.cosi103a.groupb.Rating.RatingMain;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Choose an option:");
        System.out.println("1. Play a game");
        System.out.println("2. Run player rating harness");
        
        int choice = getIntInput(scanner, 1, 2);
        
        if (choice == 1) {
            playGame(scanner);
        } else {
            // Run the rating harness
            RatingMain.main(args);
        }
        
        scanner.close();
    }
    
    private static void playGame(Scanner scanner) {
        // Step 1: Choose players
        System.out.println("\nSelect player 1:");
        System.out.println("1. Human Player");
        System.out.println("2. BigMoney AI");
        System.out.println("3. ReyEye AI");
        System.out.println("4. Final Boss AI");
        
        int player1Choice = getIntInput(scanner, 1, 4);
        
        System.out.println("\nSelect player 2:");
        System.out.println("1. Human Player");
        System.out.println("2. BigMoney AI");
        System.out.println("3. ReyEye AI");
        System.out.println("4. Final Boss AI");
        
        int player2Choice = getIntInput(scanner, 1, 4);
        
        // Create players based on choices
        AtgPlayer player1 = createPlayer("Player 1", player1Choice, scanner);
        AtgPlayer player2 = createPlayer("Player 2", player2Choice, scanner);
        
        // Create game observer
        GameObserver observer = new ConsoleGameObserver();
        
        // Create game engine
        Engine gameEngine = GameEngine.createEngine(player1, player2, observer);
        
        try {
            // Start the game and get results
            List<Player.ScorePair> results = gameEngine.play();
            
            // Display the final scores
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
            
            // Announce the winner(s)
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
    
    private static AtgPlayer createPlayer(String defaultName, int choice, Scanner scanner) {
        System.out.print("Enter name for " + defaultName + ": ");
        String name = scanner.next();
        
        switch (choice) {
            case 1:
                return new HumanPlayer(name);
            case 2:
                return new BigMoneyPlayer(name);
            case 3:
                return new RedEyePlayer(name);
            case 4:
                return new FinalBossPlayer(name);
            default:
                return new HumanPlayer(name);
        }
    }
    
    private static int getIntInput(Scanner scanner, int min, int max) {
        int choice = min - 1;
        boolean validInput = false;
        
        while (!validInput) {
            System.out.print("Enter your choice (" + min + "-" + max + "): ");
            try {
                choice = Integer.parseInt(scanner.next());
                if (choice >= min && choice <= max) {
                    validInput = true;
                } else {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        return choice;
    }

}