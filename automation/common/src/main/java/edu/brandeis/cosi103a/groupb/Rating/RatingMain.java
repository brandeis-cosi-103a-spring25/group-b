package edu.brandeis.cosi103a.groupb.Rating;

import java.util.Map;
import java.util.Scanner;

import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Player.ReyEyePlayer;
import edu.brandeis.cosi103a.groupb.Rating.PlayerRatingHarness.PlayerStats;

/**
 * Main class for running the player rating harness.
 */
public class RatingMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PlayerRatingHarness harness = new PlayerRatingHarness();
        
        // Register available players
        registerAvailablePlayers(harness);
        
        // Display welcome message
        System.out.println("=================================================");
        System.out.println("         AUTOMATED PLAYER RATING HARNESS         ");
        System.out.println("=================================================");
        System.out.println("This harness simulates games between automated players");
        System.out.println("and compares their performance statistics.");
        System.out.println();
        
        // Get number of games to simulate
        int numGames = getNumberOfGamesFromUser(scanner);
        
        // Ask if user wants silent mode
        System.out.print("Run in silent mode to speed up simulation? (y/n): ");
        boolean silentMode = scanner.next().toLowerCase().startsWith("y");
        harness.setSilentMode(silentMode);
        
        System.out.println("\nRunning tournament with " + numGames + " games per pairing...");
        
        // Run the tournament
        Map<String, PlayerStats> stats = harness.runTournament(numGames);
        
        // Display the results report
        System.out.println(harness.generateReport());
        
        scanner.close();
    }
    
    /**
     * Register all available automated players with the harness.
     */
    private static void registerAvailablePlayers(PlayerRatingHarness harness) {
        // Standard BigMoney with default settings
        harness.registerPlayer("BigMoney", () -> new BigMoneyPlayer("BigMoney"));
        
        // BigMoney variant with a parameterized name
        harness.registerPlayer("AggressiveBM", () -> new BigMoneyPlayer("AggressiveBM") {
            // An example of how you could override behavior for variants
            // Here we could override methods to change behavior if needed
        });
        
        // ReyEye player
        harness.registerPlayer("ReyEye", () -> new ReyEyePlayer("ReyEye"));
        
        // ReyEye variant with different parameters
        harness.registerPlayer("CautiousRE", () -> new ReyEyePlayer("CautiousRE") {
            // An example of how you could override behavior for variants
            // Here we could override methods to change behavior if needed
        });
    }
    
    /**
     * Get the number of games to simulate from the user.
     */
    private static int getNumberOfGamesFromUser(Scanner scanner) {
        int numGames = 0;
        boolean validInput = false;
        
        while (!validInput) {
            System.out.print("Enter number of games to simulate per player pairing: ");
            try {
                numGames = Integer.parseInt(scanner.next());
                if (numGames > 0) {
                    validInput = true;
                } else {
                    System.out.println("Please enter a positive number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        return numGames;
    }
}