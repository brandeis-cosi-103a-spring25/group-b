package edu.brandeis.cosi103a.groupb.Server.model;

public class GameResponse {
    private String id;
    private String[] players;
    
    public GameResponse() {
    }
    
    public GameResponse(String id, String[] players) {
        this.id = id;
        this.players = players;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String[] getPlayers() {
        return players;
    }
    
    public void setPlayers(String[] players) {
        this.players = players;
    }
}