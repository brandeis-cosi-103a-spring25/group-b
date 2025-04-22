package edu.brandeis.cosi103a.groupb.Player;

import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi103a.groupb.Decks.*;
import edu.brandeis.cosi103a.groupb.Server.model.*;

/**
 * A player implementation that delegates decision-making to a remote server.
 * This allows for AI players to be hosted on a separate server, enabling
 * more complex strategies without requiring the game engine to run them locally.
 */
public class NetworkPlayer implements AtgPlayer {
    // Network configuration
    private final String name;
    private final String serverUrl;
    private final String playerUuid;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Player components
    private DiscardDeck discardDeck = new DiscardDeck();
    private DrawDeck drawDeck = new DrawDeck();
    private Hand hand = new Hand(
                ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty)
                ImmutableList.copyOf(new ArrayList<Card>())  // Unplayed cards (empty)
            );

    private final Optional<GameObserver> observer;

    /**
     * Creates a new NetworkPlayer that communicates with a remote server.
     * 
     * @param name The player's name
     * @param serverUrl The URL of the remote decision server
     */
    public NetworkPlayer(String name, String serverUrl) {
        this.name = name;
        this.serverUrl = serverUrl;
        // Generate a unique ID for this player instance
        this.playerUuid = UUID.randomUUID().toString();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.observer = Optional.empty();
    }

    /**
     * Returns the player's name.
     * 
     * @return The player's name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Makes a decision by sending the current game state to the remote server
     * and receiving the decision back.
     * 
     * @param state The current game state
     * @param options Available decision options
     * @param reason Optional event that prompted this decision
     * @return The decision returned by the remote server
     * @throws RuntimeException if communication with the server fails
     */
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        try {
            // Step 1: Create the request body
            DecisionRequest requestBody = new DecisionRequest(state, options, reason);
            requestBody.setPlayer_uuid(playerUuid);

            // Step 2: Convert to JSON
            String requestJson = objectMapper.writeValueAsString(requestBody);

            // Step 3: Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/decide"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

            // Step 4: Send the request and get the response
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            // Step 5: Check if the request was successful
            if (response.statusCode() != 200) {
                throw new RuntimeException("Server returned status code: " + response.statusCode());
            };

            // Step 6: Parse the response
            DecisionResponse decisionResponse = objectMapper.readValue(
                response.body(),
                DecisionResponse.class
            );

            return decisionResponse.getDecision();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to make decision", e);
        }
    }

    /**
     * Returns the game observer for this player.
     * NetworkPlayer doesn't use an observer by default.
     * 
     * @return An empty Optional as no observer is used
     */
    @Override
    public Optional<GameObserver> getObserver() {
        return observer;
    }
    
    /**
     * Gets the player's discard deck.
     * 
     * @return The player's discard deck
     */
    @Override
    public DiscardDeck getDiscardDeck() {
        return this.discardDeck;
    }

    /**
     * Gets the player's draw deck.
     * 
     * @return The player's draw deck
     */
    @Override
    public DrawDeck getDrawDeck() {
        return this.drawDeck;
    }

    /**
     * Gets the player's current hand.
     * 
     * @return The player's hand
     */
    @Override
    public Hand getHand() {
        return this.hand;
    }

    /**
     * Sets the player's hand.
     * 
     * @param hand The new hand to set
     */
    @Override
    public void setHand(Hand hand) {
        this.hand = hand;
    }

    /**
     * Logs an event to the remote server.
     * This is done asynchronously to avoid blocking the game.
     * 
     * @param event The event to log
     * @param state The current game state
     */
    public void logEvent(Event event, GameState state) {
        try {
            // Step 1: Create the request body
            LogEventRequest requestBody = new LogEventRequest();
            requestBody.setEvent(event);
            requestBody.setPlayer_uuid(playerUuid);
            requestBody.setState(state);

            // Step 2: Convert to JSON
            String requestJson = objectMapper.writeValueAsString(requestBody);

            // Step 3: Build the HTTP Request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/log-event"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

            // Step 4: Send the request asynchronously (non-blocking)
            httpClient.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            
        } catch (Exception e) {
            System.err.println("Failed to log event: " + e.getMessage());
        }
    }
}
