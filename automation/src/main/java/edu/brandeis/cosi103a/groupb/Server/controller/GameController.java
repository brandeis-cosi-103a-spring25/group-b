package edu.brandeis.cosi103a.groupb.Server.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.Engine;
import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Player.RedEyePlayer;
import edu.brandeis.cosi103a.groupb.Server.model.GameRequest;
import edu.brandeis.cosi103a.groupb.Server.model.GameResponse;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private static final Map<String, Engine> activeGames = new HashMap<>();
    private static final Map<String, List<AtgPlayer>> gamePlayers = new HashMap<>();
    
    @GetMapping
    /**
     * Creating a bunch of GameResponse instances according to the data fetched (GameResponse data strcture in "model")
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
        
        GameResponse response = new GameResponse(gameId, 
            new String[]{player1.getName(), player2.getName()});
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable String id) {
        if (activeGames.containsKey(id)) {
            activeGames.remove(id);
            gamePlayers.remove(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    private AtgPlayer createPlayer(String type, String name) {
        switch (type.toLowerCase()) {
            case "bigmoney":
                return new BigMoneyPlayer(name);
            case "reyeye":
                return new RedEyePlayer(name);
            default:
                return new BigMoneyPlayer(name); // Default
        }
    }
}