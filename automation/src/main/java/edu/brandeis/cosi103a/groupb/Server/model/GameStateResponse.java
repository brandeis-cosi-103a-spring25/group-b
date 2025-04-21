// Add this new model class first
package edu.brandeis.cosi103a.groupb.Server.model;

import java.util.List;

public class GameStateResponse {
    private String id;
    private String phase;
    private String currentPlayer;
    private PlayerStateInfo player1;
    private PlayerStateInfo player2;
    
    public static class PlayerStateInfo {
        private String name;
        private int score;
        private List<CardInfo> hand;
        private List<CardInfo> playedCards;
        
        public PlayerStateInfo() {}
        
        public PlayerStateInfo(String name, int score, List<CardInfo> hand, List<CardInfo> playedCards) {
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
        
        public List<CardInfo> getHand() {
            return hand;
        }
        
        public void setHand(List<CardInfo> hand) {
            this.hand = hand;
        }
        
        public List<CardInfo> getPlayedCards() {
            return playedCards;
        }
        
        public void setPlayedCards(List<CardInfo> playedCards) {
            this.playedCards = playedCards;
        }
    }
    
    public static class CardInfo {
        private String suit;
        private String rank;
        
        public CardInfo() {}
        
        public CardInfo(String suit, String rank) {
            this.suit = suit;
            this.rank = rank;
        }
        
        // Getters and setters
        public String getSuit() {
            return suit;
        }
        
        public void setSuit(String suit) {
            this.suit = suit;
        }
        
        public String getRank() {
            return rank;
        }
        
        public void setRank(String rank) {
            this.rank = rank;
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
