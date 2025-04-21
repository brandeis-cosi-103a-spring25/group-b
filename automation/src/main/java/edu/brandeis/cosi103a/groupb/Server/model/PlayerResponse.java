package edu.brandeis.cosi103a.groupb.Server.model;

public class PlayerResponse {
    private String type;
    
    public PlayerResponse() {
    }
    
    public PlayerResponse(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}