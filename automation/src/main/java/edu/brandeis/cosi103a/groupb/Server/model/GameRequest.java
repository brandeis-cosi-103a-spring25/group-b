package edu.brandeis.cosi103a.groupb.Server.model;

public class GameRequest {
    private String player1Type;
    private String player1Name;
    private String player2Type;
    private String player2Name;
    
    public GameRequest() {
    }
    
    public GameRequest(String player1Type, String player1Name, String player2Type, String player2Name) {
        this.player1Type = player1Type;
        this.player1Name = player1Name;
        this.player2Type = player2Type;
        this.player2Name = player2Name;
    }
    
    public String getPlayer1Type() {
        return player1Type;
    }
    
    public void setPlayer1Type(String player1Type) {
        this.player1Type = player1Type;
    }
    
    public String getPlayer1Name() {
        return player1Name;
    }
    
    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }
    
    public String getPlayer2Type() {
        return player2Type;
    }
    
    public void setPlayer2Type(String player2Type) {
        this.player2Type = player2Type;
    }
    
    public String getPlayer2Name() {
        return player2Name;
    }
    
    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }
}