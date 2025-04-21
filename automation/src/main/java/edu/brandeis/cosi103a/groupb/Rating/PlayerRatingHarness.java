package edu.brandeis.cosi103a.groupb.Rating;

import java.util.*;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi103a.groupb.Game.*;
import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;

/**
 * A harness for comparing different automated players by simulating games and
 * tracking statistics.
 * 
 * This class manages the tournament between different AI players, runs multiple 
 * games between each pair, and collects performance statistics for comparison.
 */
public class PlayerRatingHarness {
    // List of player configurations available for the tournament
    private List<PlayerConfig> availablePlayers = new ArrayList<>();
    
    // Map of player names to their collected statistics
    private Map<String, PlayerStats> playerStats = new HashMap<>();
    
    // Flag to control console output during simulations
    private boolean silentMode = false;

    /**
     * Add a player configuration to the list of available players.
     * 
     * @param name The name identifier for the player type
     * @param playerSupplier A supplier function that creates a new instance of the player
     */
    public void registerPlayer(String name, Supplier<AtgPlayer> playerSupplier) {
        availablePlayers.add(new PlayerConfig(name, playerSupplier));
    }

    /**
     * Set silent mode to reduce console output during simulations.
     * 
     * @param silent True to enable silent mode, false for verbose output
     */
    public void setSilentMode(boolean silent) {
        this.silentMode = silent;
    }

    /**
     * Run a tournament where each pair of players plays the specified number of games.
     * 
     * @param numGames The number of games to simulate for each pair of players
     * @return A map of player names to their statistics
     */
    public Map<String, PlayerStats> runTournament(int numGames) {
        // Reset statistics
        playerStats.clear();
        
        // Initialize statistics for each player
        for (PlayerConfig config : availablePlayers) {
            playerStats.put(config.name, new PlayerStats());
        }
        
        // Run games for each pair of players
        for (int i = 0; i < availablePlayers.size(); i++) {
            for (int j = i + 1; j < availablePlayers.size(); j++) {
                PlayerConfig player1Config = availablePlayers.get(i);
                PlayerConfig player2Config = availablePlayers.get(j);
                
                if (!silentMode) {
                    System.out.println("\n========================================");
                    System.out.println("Running " + numGames + " games between " + 
                                      player1Config.name + " and " + player2Config.name);
                    System.out.println("========================================");
                }
                
                // Run the games between this pair
                for (int game = 0; game < numGames; game++) {
                    if (!silentMode) {
                        System.out.println("\nGame " + (game + 1) + " of " + numGames);
                    }
                    runGame(player1Config, player2Config);
                }
            }
        }
        
        return playerStats;
    }
    
    /**
     * Run a single game between two players and update their statistics.
     * 
     * @param player1Config Configuration for the first player
     * @param player2Config Configuration for the second player
     */
    private void runGame(PlayerConfig player1Config, PlayerConfig player2Config) {
        // Create fresh instances of players for this game
        AtgPlayer player1 = player1Config.playerSupplier.get();
        AtgPlayer player2 = player2Config.playerSupplier.get();
        
        // Create the game observer (using silent observer in silent mode)
        GameObserver observer = silentMode ? 
                               new SilentGameObserver() : 
                               new ConsoleGameObserver();
        
        // Create the game engine
        Engine gameEngine = GameEngine.createEngine(player1, player2, observer);
        
        try {
            // Run the game
            ImmutableList<Player.ScorePair> results = gameEngine.play();
            
            // Process results
            processGameResults(results, player1, player2, player1Config, player2Config);
            
        } catch (PlayerViolationException e) {
            if (!silentMode) {
                System.out.println("Game ended with an error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Process game results and update player statistics.
     * 
     * @param results The game results
     * @param player1 First player instance
     * @param player2 Second player instance
     * @param player1Config First player configuration
     * @param player2Config Second player configuration
     */
    private void processGameResults(ImmutableList<Player.ScorePair> results, 
                                   AtgPlayer player1, AtgPlayer player2,
                                   PlayerConfig player1Config, PlayerConfig player2Config) {
        if (results.size() < 2) {
            return;  // Not enough results to process
        }
        
        Player.ScorePair p1Result = null;
        Player.ScorePair p2Result = null;
        
        // Find results for our players
        for (Player.ScorePair result : results) {
            if (result.player.getName().equals(player1.getName())) {
                p1Result = result;
            } else if (result.player.getName().equals(player2.getName())) {
                p2Result = result;
            }
        }
        
        if (p1Result != null && p2Result != null) {
            int p1PointDifferential = p1Result.getScore() - p2Result.getScore();
            int p2PointDifferential = p2Result.getScore() - p1Result.getScore();
            
            // Update player 1 stats
            updatePlayerStats(playerStats.get(player1Config.name), p1Result.getScore(), p1PointDifferential);
            
            // Update player 2 stats
            updatePlayerStats(playerStats.get(player2Config.name), p2Result.getScore(), p2PointDifferential);
            
            // Determine and announce winner
            announceWinner(p1Result, p2Result, player1, player2);
        }
    }
    
    /**
     * Update statistics for a single player.
     * 
     * @param stats The player's statistics object
     * @param score The score achieved in this game
     * @param pointDifferential Point difference against opponent
     */
    private void updatePlayerStats(PlayerStats stats, int score, int pointDifferential) {
        stats.gamesPlayed++;
        stats.totalScore += score;
        stats.totalPointDifferential += pointDifferential;
        
        // Update biggest win and worst loss
        if (pointDifferential > 0 && pointDifferential > stats.biggestWin) {
            stats.biggestWin = pointDifferential;
        } else if (pointDifferential < 0 && pointDifferential < stats.worstLoss) {
            stats.worstLoss = pointDifferential;
        }
    }
    
    /**
     * Determine and announce the winner of a game.
     * 
     * @param p1Result First player's score pair
     * @param p2Result Second player's score pair
     * @param player1 First player instance
     * @param player2 Second player instance
     */
    private void announceWinner(Player.ScorePair p1Result, Player.ScorePair p2Result,
                               AtgPlayer player1, AtgPlayer player2) {
        PlayerStats stats1 = playerStats.get(player1.getName());
        PlayerStats stats2 = playerStats.get(player2.getName());
        
        if (p1Result.getScore() > p2Result.getScore()) {
            stats1.wins++;
            if (!silentMode) {
                System.out.println(player1.getName() + " wins with " + p1Result.getScore() + 
                                  " vs " + p2Result.getScore());
            }
        } else if (p2Result.getScore() > p1Result.getScore()) {
            stats2.wins++;
            if (!silentMode) {
                System.out.println(player2.getName() + " wins with " + p2Result.getScore() + 
                                  " vs " + p1Result.getScore());
            }
        } else {
            // It's a tie
            stats1.ties++;
            stats2.ties++;
            if (!silentMode) {
                System.out.println("Game ended in a tie with " + p1Result.getScore() + " points each");
            }
        }
    }
    
    /**
     * Generate a formatted report of player statistics.
     * 
     * @return A formatted string containing the tournament results
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n=================================================\n");
        report.append("                PLAYER RATINGS                   \n");
        report.append("=================================================\n");
        
        // Sort players by win rate
        List<Map.Entry<String, PlayerStats>> sortedPlayers = new ArrayList<>(playerStats.entrySet());
        sortedPlayers.sort((e1, e2) -> {
            double winRate1 = e1.getValue().getWinRate();
            double winRate2 = e2.getValue().getWinRate();
            return Double.compare(winRate2, winRate1); // Descending order
        });
        
        report.append(String.format("%-15s %-7s %-7s %-7s %-7s %-10s %-12s %-12s %-10s %-10s\n", 
                                   "Player", "Games", "Wins", "Ties", "Losses", "Win Rate", 
                                   "Avg Score", "Avg Diff", "Best Win", "Worst Loss"));
        report.append("------------------------------------------------------------------------------------------------------\n");
        
        for (Map.Entry<String, PlayerStats> entry : sortedPlayers) {
            PlayerStats stats = entry.getValue();
            report.append(String.format("%-15s %-7d %-7d %-7d %-7d %-10.2f%% %-12.2f %-+12.2f %-10d %-10d\n",
                                   entry.getKey(),
                                   stats.gamesPlayed,
                                   stats.wins,
                                   stats.ties,
                                   stats.getLosses(),
                                   stats.getWinRate() * 100,
                                   stats.getAverageScore(),
                                   stats.getAveragePointDifferential(),
                                   stats.biggestWin,
                                   stats.worstLoss));
        }
        
        report.append("\n=================================================\n");
        report.append("             HEAD-TO-HEAD RESULTS                \n");
        report.append("=================================================\n");
        
        // TODO: Implement head-to-head statistics if desired
        
        return report.toString();
    }
    
    /**
     * Configuration for a player in the tournament.
     */
    private static class PlayerConfig {
        final String name;
        final Supplier<AtgPlayer> playerSupplier;
        
        /**
         * Create a new player configuration.
         * 
         * @param name Player type name
         * @param playerSupplier Supplier to create new instances of this player
         */
        PlayerConfig(String name, Supplier<AtgPlayer> playerSupplier) {
            this.name = name;
            this.playerSupplier = playerSupplier;
        }
    }
    
    /**
     * Statistics tracked for each player.
     */
    public static class PlayerStats {
        int gamesPlayed = 0;
        int wins = 0;
        int ties = 0;
        double totalScore = 0;
        int totalPointDifferential = 0;    // Sum of all point differences (positive when winning, negative when losing)
        int biggestWin = 0;                // Largest margin of victory
        int worstLoss = 0;                 // Largest margin of defeat

        /**
         * Get the number of games lost.
         * 
         * @return Number of losses
         */
        public int getLosses() {
            return gamesPlayed - wins - ties;
        }
        
        /**
         * Get the win rate as a decimal (0.0 to 1.0).
         * 
         * @return Win rate
         */
        public double getWinRate() {
            if (gamesPlayed == 0) return 0;
            return (double) wins / gamesPlayed;
        }
        
        /**
         * Get the average score per game.
         * 
         * @return Average score
         */
        public double getAverageScore() {
            if (gamesPlayed == 0) return 0;
            return totalScore / gamesPlayed;
        }
        
        /**
         * Get the average point differential per game.
         * 
         * @return Average point differential
         */
        public double getAveragePointDifferential() {
            return gamesPlayed == 0 ? 0 : (double) totalPointDifferential / gamesPlayed;
        }
    }
    
    /**
     * Silent game observer to reduce console output during simulations.
     */
    private static class SilentGameObserver implements GameObserver {
        @Override
        public void notifyEvent(edu.brandeis.cosi.atg.api.GameState state, edu.brandeis.cosi.atg.api.event.Event event) {
            // Do nothing (silent)
        }
    }
}