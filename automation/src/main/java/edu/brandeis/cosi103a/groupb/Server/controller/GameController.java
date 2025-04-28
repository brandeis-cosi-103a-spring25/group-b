package edu.brandeis.cosi103a.groupb.Server.controller;

import java.util.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi103a.groupb.Game.*;
import edu.brandeis.cosi103a.groupb.Player.*;
import edu.brandeis.cosi103a.groupb.Server.model.*;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private static final Map<String, Engine> activeGames = new HashMap<>();
    private static final Map<String, List<AtgPlayer>> gamePlayers = new HashMap<>();
    private static final Map<String, Boolean> gameStarted = new HashMap<>();
    
    @GetMapping
    /**
     * Creating a bunch of GameResponse instances according to the data fetched (GameResponse data structure in "model")
     * @return
     */
    public List<GameResponse> getAllGames() {
        List<GameResponse> games = new ArrayList<>();
        for (Map.Entry<String, Engine> entry : activeGames.entrySet()) {
            String gameId = entry.getKey();
            List<AtgPlayer> players = gamePlayers.get(gameId);
            games.add(new GameResponse(gameId, 
                players.stream().map(Player::getName).toArray(String[]::new)));
        }
        return games;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGame(@PathVariable String id) {
        if (activeGames.containsKey(id)) {
            List<AtgPlayer> players = gamePlayers.get(id);
            GameResponse response = new GameResponse(id, 
                players.stream().map(Player::getName).toArray(String[]::new));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<GameResponse> createGame(@RequestBody GameRequest request) {
        AtgPlayer player1 = createPlayer(request.getPlayer1Type(), request.getPlayer1Name());
        AtgPlayer player2 = createPlayer(request.getPlayer2Type(), request.getPlayer2Name());
        GameObserver observer = new ConsoleGameObserver();
        
        Engine engine = GameEngine.createEngine(player1, player2, observer);
        String gameId = UUID.randomUUID().toString();
        
        activeGames.put(gameId, engine);
        gamePlayers.put(gameId, List.of(player1, player2));
        gameStarted.put(gameId, false);
        
        GameResponse response = new GameResponse(gameId, 
            new String[]{player1.getName(), player2.getName()});
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    // Add this new endpoint to start the game
    @PostMapping("/{id}/start")
    public ResponseEntity<String> startGame(@PathVariable String id) {
        if (!activeGames.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        
        if (gameStarted.get(id)) {
            return ResponseEntity.ok("Game already started");
        }
        
        try {
            // Get the game engine
            Engine engine = activeGames.get(id);
            
            // Start the game in a separate thread so it doesn't block
            new Thread(() -> {
                try {
                    engine.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            
            gameStarted.put(id, true);
            return ResponseEntity.ok("Game started successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error starting game: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable String id) {
        if (activeGames.containsKey(id)) {
            activeGames.remove(id);
            gamePlayers.remove(id);
            gameStarted.remove(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    private AtgPlayer createPlayer(String type, String name) {
        switch (type.toLowerCase()) {
            case "bigmoney":
                return new BigMoneyPlayer(name);
            case "redeye":
                return new RedEyePlayer(name);
            default:
                return new BigMoneyPlayer(name); // Default
        }
    }

    @GetMapping("/{id}/state")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable String id) {
        if (!activeGames.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Get the game and players
        Engine engine = activeGames.get(id);
        List<AtgPlayer> players = gamePlayers.get(id);
        
        // Create a response with the game state
        GameStateResponse response = extractGameState(id, engine, players);
        
        return ResponseEntity.ok(response);
    }

    // Helper method to extract game state
    private GameStateResponse extractGameState(String gameId, Engine engine, List<AtgPlayer> players) {
        AtgPlayer player1 = players.get(0);
        AtgPlayer player2 = players.get(1);
        
        // Cast to GameEngine to access the getGameState() method
        GameEngine gameEngine = (GameEngine) engine;
        GameState gameState = null;
        
        // Get the game state if the game has started
        if (gameStarted.getOrDefault(gameId, false)) {
            gameState = gameEngine.getGameState();
        }
        
        // Extract current player and phase information
        String currentPlayer = "Unknown";
        String phase = "Unknown";
        
        if (gameState != null) {
            currentPlayer = gameState.getCurrentPlayerName();
            
            // Convert the TurnPhase enum to string
            switch (gameState.getTurnPhase()) {
                case ACTION:
                    phase = "Action Phase";
                    break;
                case MONEY:
                    phase = "Money Phase";
                    break;
                case BUY:
                    phase = "Buy Phase";
                    break;
                case CLEANUP:
                    phase = "Cleanup Phase";
                    break;
                case DISCARD:
                    phase = "Discard Phase";
                    break;
                case REACTION:
                    phase = "Reaction Phase";
                    break;
                case GAIN:
                    phase = "Gain Phase";
                    break;
                default:
                    phase = "Unknown";
            }
        }
        
        // Extract player 1's hand and played cards
        List<Card> player1Hand = new ArrayList<>(player1.getHand().getUnplayedCards());
        List<Card> player1Played = new ArrayList<>(player1.getHand().getPlayedCards());
        
        // Extract player 2's hand and played cards
        List<Card> player2Hand = new ArrayList<>(player2.getHand().getUnplayedCards());
        List<Card> player2Played = new ArrayList<>(player2.getHand().getPlayedCards());
        
        // Create player state info
        GameStateResponse.PlayerStateInfo player1Info = new GameStateResponse.PlayerStateInfo(
            player1.getName(),
            calculateScore(player1, gameEngine),
            player1Hand,
            player1Played
        );
        
        GameStateResponse.PlayerStateInfo player2Info = new GameStateResponse.PlayerStateInfo(
            player2.getName(),
            calculateScore(player2, gameEngine),
            player2Hand,
            player2Played
        );
        
        // Return the complete game state
        return new GameStateResponse(gameId, phase, currentPlayer, player1Info, player2Info);
    }

    // Helper method to calculate a player's score
    private int calculateScore(AtgPlayer player, GameEngine engine) {
        // Since the score calculation is in the GameEngine class, we'll try to use it
        try {
            // Use the static method to get current scores
            List<Player.ScorePair> scores = GameEngine.getCurrentScores();
            
            // Find the score for this player
            for (Player.ScorePair score : scores) {
                if (score.player.getName().equals(player.getName())) {
                    return score.getScore();
                }
            }
        } catch (Exception e) {
            // If there's an error, log it but don't crash
            System.err.println("Error calculating score: " + e.getMessage());
        }
        
        // Default to 0 if we couldn't get the score
        return 0;
    }
}