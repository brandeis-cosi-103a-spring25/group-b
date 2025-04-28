package edu.brandeis.cosi103a.groupb;

import java.util.*;




import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi103a.groupb.Game.*;
import edu.brandeis.cosi103a.groupb.Player.*;

import edu.brandeis.cosi103a.groupb.Rating.RatingMain;

/**
 * Main application entry point for the card game.
 * This class provides the user interface for playing the game 
 * or running the player rating harness.
 */
public class Main {
    /**
     * Main method that handles user choices between playing a game
     * or running the rating harness.
     *
     * @param args Command line arguments (not used)
     */
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
    
    /**
     * Handles the game setup and execution process.
     * Allows user to select players, create the game, and displays results.
     *
     * @param scanner Scanner for reading user input
     */
    private static void playGame(Scanner scanner) {
        // Step 1: Choose players
        System.out.println("\nSelect player 1:");
        System.out.println("1. Human Player");
        System.out.println("2. BigMoney AI");
        System.out.println("3. RedEye AI");
        System.out.println("4. Final Boss AI");
        
        int player1Choice = getIntInput(scanner, 1, 4);
        
        System.out.println("\nSelect player 2:");
        System.out.println("1. Human Player");
        System.out.println("2. BigMoney AI");
        System.out.println("3. RedEye AI");
        System.out.println("4. Final Boss AI");
        
        int player2Choice = getIntInput(scanner, 1, 4);
        
        // Step 2: Create players based on choices
        AtgPlayer player1 = createPlayer("Player 1", player1Choice, scanner);
        AtgPlayer player2 = createPlayer("Player 2", player2Choice, scanner);
        
        // Step 3: Create game observer
        GameObserver observer = new ConsoleGameObserver();
        
        // Step 4: Create game engine
        Engine gameEngine = GameEngine.createEngine(player1, player2, observer);
        
        try {
            // Step 5: Start the game and get results
            List<Player.ScorePair> results = gameEngine.play();
            
            // Step 6: Display the final scores
            displayGameResults(results);
            
        } catch (PlayerViolationException e) {
            // Handle invalid moves
            System.out.println("Game ended due to an invalid move: " + e.getMessage());
        }
    }
    
    /**
     * Displays the final game results and determines the winner(s).
     *
     * @param results List of player score pairs from the game
     */
    private static void displayGameResults(List<Player.ScorePair> results) {
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
    }
    
    /**
     * Creates a player based on the user's choice.
     *
     * @param defaultName The default player name
     * @param choice The player type choice (1=Human, 2=BigMoney, 3=RedEye)
     * @param scanner Scanner for reading user input
     * @return The created player
     */
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
    
    /**
     * Gets an integer input from the user within a specified range.
     *
     * @param scanner Scanner for reading user input
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     * @return The validated integer input
     */
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