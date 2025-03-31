package edu.brandeis.cosi103a.groupb.Rating;

import java.util.*;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.Engine;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.PlayerViolationException;
import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;

/**
 * A harness for comparing different automated players by simulating games and
 * tracking statistics.
 */
public class PlayerRatingHarness {
    private List<PlayerConfig> availablePlayers = new ArrayList<>();
    private Map<String, PlayerStats> playerStats = new HashMap<>();
    private boolean silentMode = false;

    /**
     * Add a player configuration to the list of available players.
     */
    public void registerPlayer(String name, Supplier<AtgPlayer> playerSupplier) {
        availablePlayers.add(new PlayerConfig(name, playerSupplier));
    }

    /**
     * Set silent mode to reduce console output during simulations
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
            if (results.size() >= 2) {
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
                    PlayerStats stats1 = playerStats.get(player1Config.name);
                    stats1.gamesPlayed++;
                    stats1.totalScore += p1Result.getScore();
                    stats1.totalPointDifferential += p1PointDifferential;
                    // Update biggest win and worst loss
                    if (p1PointDifferential > 0 && p1PointDifferential > stats1.biggestWin) {
                        stats1.biggestWin = p1PointDifferential;
                    } else if (p1PointDifferential < 0 && p1PointDifferential < stats1.worstLoss) {
                        stats1.worstLoss = p1PointDifferential;
                    }
                    
                    // Update player 2 stats
                    PlayerStats stats2 = playerStats.get(player2Config.name);
                    stats2.gamesPlayed++;
                    stats2.totalScore += p2Result.getScore();
                    stats2.totalPointDifferential += p2PointDifferential;
                    
                    if (p2PointDifferential > 0 && p2PointDifferential > stats2.biggestWin) {
                        stats2.biggestWin = p2PointDifferential;
                    } else if (p2PointDifferential < 0 && p2PointDifferential < stats2.worstLoss) {
                        stats2.worstLoss = p2PointDifferential;
                    }
                    
                    // Determine winner
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
            }
        } catch (PlayerViolationException e) {
            if (!silentMode) {
                System.out.println("Game ended with an error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Generate a formatted report of player statistics
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
     * Configuration for a player in the tournament
     */
    private static class PlayerConfig {
        final String name;
        final Supplier<AtgPlayer> playerSupplier;
        
        PlayerConfig(String name, Supplier<AtgPlayer> playerSupplier) {
            this.name = name;
            this.playerSupplier = playerSupplier;
        }
    }
    
    /**
     * Statistics tracked for each player
     */
    public static class PlayerStats {
        int gamesPlayed = 0;
        int wins = 0;
        int ties = 0;
        double totalScore = 0;
        int totalPointDifferential = 0;    // Sum of all point differences (positive when winning, negative when losing)
        int biggestWin = 0;                // Largest margin of victory
        int worstLoss = 0;  

        public int getLosses() {
            return gamesPlayed - wins - ties;
        }
        
        public double getWinRate() {
            if (gamesPlayed == 0) return 0;
            return (double) wins / gamesPlayed;
        }
        
        public double getAverageScore() {
            if (gamesPlayed == 0) return 0;
            return totalScore / gamesPlayed;
        }
        public double getAveragePointDifferential() {
            return gamesPlayed == 0 ? 0 : (double) totalPointDifferential / gamesPlayed;
        }
    }
    
    /**
     * Silent game observer to reduce console output during simulations
     */
    private static class SilentGameObserver implements GameObserver {
        @Override
        public void notifyEvent(edu.brandeis.cosi.atg.api.GameState state, edu.brandeis.cosi.atg.api.event.Event event) {
            // Do nothing (silent)
        }
    }
}