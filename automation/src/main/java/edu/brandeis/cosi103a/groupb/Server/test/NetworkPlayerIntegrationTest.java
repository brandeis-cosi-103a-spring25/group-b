package edu.brandeis.cosi103a.groupb.Server.test;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi103a.groupb.Player.NetworkPlayer;

public class NetworkPlayerIntegrationTest {
    
    public static void main(String[] args) {
        // Create a NetworkPlayer that connects to your local server
        NetworkPlayer player = new NetworkPlayer("TestPlayer", "http://localhost:8080");
        System.out.println("Player created: " + player.getName());
        
        // Test the connection by making a simple request
        try {
            // Create mock game state and options for testing
            GameState mockState = createMockGameState();
            List<Decision> mockOptions = createMockOptions();
            
            // Test making a decision
            Decision decision = player.makeDecision(
                mockState, 
                ImmutableList.copyOf(mockOptions), 
                Optional.empty()
            );
            
            System.out.println("Connection successful! Received decision: " + decision);
            
            // Test logging an event
            player.logEvent(createMockEvent(), mockState);
            System.out.println("Event logged successfully");
            
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper methods to create test objects
    private static GameState createMockGameState() {
        // Create a minimal valid GameState for testing
        // You'll need to implement this based on your GameState structure
        return null; // Replace with actual implementation
    }
    
    private static List<Decision> createMockOptions() {
        // Create some test decision options
        // You'll need to implement this based on your Decision structure
        return null; // Replace with actual implementation
    }
    
    private static Event createMockEvent() {
        // Create a test event
        // You'll need to implement this based on your Event structure
        return null; // Replace with actual implementation
    }
}
