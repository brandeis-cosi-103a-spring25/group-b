package edu.brandeis.cosi103a.groupb;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.*;
import com.sun.net.httpserver.*;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi103a.groupb.Player.NetworkPlayer;
import edu.brandeis.cosi103a.groupb.Server.model.DecisionResponse;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Test class for the NetworkPlayer implementation.
 * 
 * This class tests the client-side functionality of the NetworkPlayer, which acts as a 
 * proxy between the game engine and a remote decision server. The NetworkPlayer sends
 * game state and decision options to the server and receives decisions back.
 * 
 * The test uses a mock HTTP server that simulates the remote decision server, allowing
 * tests to run without requiring an actual server deployment.
 * 
 * Three main aspects are tested:
 * 1. Making decisions through HTTP requests
 * 2. Logging events to the server
 * 3. Handling error scenarios when the server is unavailable
 */
public class NetworkPlayerTest {
    /** ObjectMapper for JSON serialization/deserialization */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /** Mock HTTP server instance */
    private static HttpServer server;
    
    /** Port the mock server is running on */
    private static int port;
    
    /**
     * Sets up the test environment before running any tests.
     * This method:
     * 1. Finds an available port to use
     * 2. Creates a mock HTTP server on that port
     * 3. Configures endpoints for decision making and event logging
     * 4. Starts the server
     *
     * @throws IOException if server creation fails
     */
    @BeforeAll
    public static void setup() throws IOException {
        // Find an available port
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        
        // Create and start the server
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/decide", new DecideHandler());
        server.createContext("/log-event", new LogEventHandler());
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }
    
    /**
     * Cleans up the test environment after all tests have run.
     * This method stops the mock HTTP server.
     */
    @AfterAll
    public static void teardown() {
        if (server != null) {
            server.stop(0);
        }
    }
    
    /**
     * Tests that the NetworkPlayer can make a decision via the server.
     * 
     * This test:
     * 1. Creates a NetworkPlayer connected to our mock server
     * 2. Creates a test game state and decision options
     * 3. Calls makeDecision() which should connect to the server
     * 4. Verifies that the received decision matches what we expect
     */
    @Test
    public void testMakeDecision() {
        try {
            // Create a NetworkPlayer that points to our mock server
            String url = "http://localhost:" + port;
            System.out.println("Testing with server at: " + url);
            NetworkPlayer player = new NetworkPlayer("TestPlayer", url);
            
            // Create test game state and options
            GameState state = createTestGameState("TestPlayer");
            ArrayList<Decision> options = new ArrayList<>();
            options.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));
            options.add(new PlayCardDecision(new Card(Card.Type.BITCOIN, 1)));
            
            // Make the decision
            Decision decision = player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());
            
            // Verify the decision type
            assertTrue(decision instanceof EndPhaseDecision, 
                      "Expected EndPhaseDecision but got: " + decision.getClass().getSimpleName());
                      
            System.out.println("Successfully made decision via network player");
        } catch (Exception e) {
            System.err.println("Test failure: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    /**
     * Tests that the NetworkPlayer can log events to the server.
     * 
     * This test:
     * 1. Creates a NetworkPlayer connected to our mock server
     * 2. Calls logEvent() which should asynchronously send an event to the server
     * 3. Passes if no exceptions are thrown (since event logging is "fire and forget")
     */
    @Test
    public void testLogEvent() {
        // Create a NetworkPlayer that points to our mock server
        String url = "http://localhost:" + port;
        NetworkPlayer player = new NetworkPlayer("TestPlayer", url);
        
        // Log an event (should not throw exceptions)
        GameState state = createTestGameState("TestPlayer");
        player.logEvent(null, state);
        
        // No assertions needed - if no exception is thrown, the test passes
    }
    
    /**
     * Tests that the NetworkPlayer properly handles server errors.
     * 
     * This test:
     * 1. Creates a NetworkPlayer with an invalid URL (non-existent port)
     * 2. Attempts to make a decision, which should fail
     * 3. Verifies that a RuntimeException is thrown as expected
     */
    @Test
    public void testErrorHandling() {
        // Create a NetworkPlayer with an invalid URL
        NetworkPlayer player = new NetworkPlayer("ErrorPlayer", "http://localhost:1");
        
        // Create a test game state and options
        GameState state = createTestGameState("ErrorPlayer");
        ArrayList<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));
        
        // Expect an exception when trying to make a decision
        assertThrows(RuntimeException.class, () -> {
            player.makeDecision(state, ImmutableList.copyOf(options), Optional.empty());
        });
    }
    
    /**
     * Helper method to create a test game state.
     * 
     * This creates a minimal GameState suitable for testing, with:
     * - Empty hand of cards
     * - ACTION phase
     * - 1 action, 0 money, 1 buy
     * - Empty game deck
     * 
     * @param playerName The name to use for the player in the game state
     * @return A GameState instance ready for testing
     */
    private GameState createTestGameState(String playerName) {
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of());
        return new GameState(
            playerName,
            hand,
            GameState.TurnPhase.ACTION,
            1, // Actions
            0, // Money
            1, // Buys
            new GameDeck(ImmutableMap.copyOf(new HashMap<Card.Type, Integer>()))
        );
    }
    
    /**
     * Mock handler for the /decide endpoint.
     * 
     * This handler simulates the remote decision server's /decide endpoint, which:
     * 1. Receives a decision request with game state and options
     * 2. Returns a predetermined decision (always EndPhaseDecision in this test)
     */
    private static class DecideHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            
            try {
                // Read request body
                InputStream is = exchange.getRequestBody();
                byte[] bodyBytes = is.readAllBytes();
                is.close();
                
                // Prepare a simple response with EndPhaseDecision
                // We're not actually parsing the request here to avoid deserialization issues
                DecisionResponse response = new DecisionResponse(
                    new EndPhaseDecision(GameState.TurnPhase.ACTION)
                );
                
                // Send response
                String responseJson = objectMapper.writeValueAsString(response);
                sendResponse(exchange, 200, responseJson);
                
            } catch (Exception e) {
                System.err.println("Error in DecideHandler: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }
    
    /**
     * Mock handler for the /log-event endpoint.
     * 
     * This handler simulates the remote decision server's /log-event endpoint, which:
     * 1. Receives event data from the NetworkPlayer
     * 2. Returns a simple acknowledgment (empty JSON object)
     */
    private static class LogEventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            
            try {
                // Just read and discard the body
                InputStream is = exchange.getRequestBody();
                is.readAllBytes();
                is.close();
                
                // Simply acknowledge the request
                sendResponse(exchange, 200, "{}");
            } catch (Exception e) {
                System.err.println("Error in LogEventHandler: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }
    
    /**
     * Helper method to send HTTP responses.
     * 
     * @param exchange The HttpExchange to respond to
     * @param statusCode The HTTP status code to return
     * @param response The response body string
     * @throws IOException If writing the response fails
     */
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
} 