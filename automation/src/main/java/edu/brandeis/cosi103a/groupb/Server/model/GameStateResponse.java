// Add this new model class first
package edu.brandeis.cosi103a.groupb.Server.model;

import java.util.*;

import edu.brandeis.cosi.atg.api.cards.*;

public class GameStateResponse {
    private String id;
    private String phase;
    private String currentPlayer;
    private PlayerStateInfo player1;
    private PlayerStateInfo player2;
    
    public static class PlayerStateInfo {
        private String name;
        private int score;
        private List<Card> hand;
        private List<Card> playedCards;
        
        public PlayerStateInfo() {}
        
        public PlayerStateInfo(String name, int score, List<Card> hand, List<Card> playedCards) {
            this.name = name;
            this.score = score;
            this.hand = hand;
            this.playedCards = playedCards;
        }
        
        // Getters and setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getScore() {
            return score;
        }
        
        public void setScore(int score) {
            this.score = score;
        }
        
        public List<Card> getHand() {
            return hand;
        }
        
        public void setHand(List<Card> hand) {
            this.hand = hand;
        }
        
        public List<Card> getPlayedCards() {
            return playedCards;
        }
        
        public void setPlayedCards(List<Card> playedCards) {
            this.playedCards = playedCards;
        }
    }
    
    public GameStateResponse() {}
    
    public GameStateResponse(String id, String phase, String currentPlayer, PlayerStateInfo player1, PlayerStateInfo player2) {
        this.id = id;
        this.phase = phase;
        this.currentPlayer = currentPlayer;
        this.player1 = player1;
        this.player2 = player2;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPhase() {
        return phase;
    }
    
    public void setPhase(String phase) {
        this.phase = phase;
    }
    
    public String getCurrentPlayer() {
        return currentPlayer;
    }
    
    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
    
    public PlayerStateInfo getPlayer1() {
        return player1;
    }
    
    public void setPlayer1(PlayerStateInfo player1) {
        this.player1 = player1;
    }
    
    public PlayerStateInfo getPlayer2() {
        return player2;
    }
    
    public void setPlayer2(PlayerStateInfo player2) {
        this.player2 = player2;
    }
}
