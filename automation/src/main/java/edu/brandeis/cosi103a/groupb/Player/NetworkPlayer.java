package edu.brandeis.cosi103a.groupb.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Server.model.DecisionRequest;
import edu.brandeis.cosi103a.groupb.Server.model.DecisionResponse;
import edu.brandeis.cosi103a.groupb.Server.model.LogEventRequest;


/**
 * Represents an automated player, following the basic strategy.
 */
public class NetworkPlayer implements AtgPlayer {
    private final String name;
    private final String serverUrl;
    private final String playerUuid;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private DiscardDeck discardDeck = new DiscardDeck();
    private DrawDeck drawDeck = new DrawDeck();
    private Hand hand = new Hand(
                ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty)
                ImmutableList.copyOf(new ArrayList<Card>())  // Unplayed cards (empty)
            );

    private final Optional<GameObserver> observer;

    public NetworkPlayer(String name, String serverUrl) {
        this.name = name;
        this.serverUrl = serverUrl;
        // Generate a unique ID for this player instance
        this.playerUuid = UUID.randomUUID().toString();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.observer = Optional.empty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        try {

            // Create the request body
            DecisionRequest requestBody = new DecisionRequest(state, options, reason);
            requestBody.setPlayer_uuid(playerUuid);

            // Convert to JSON
            String requestJson = objectMapper.writeValueAsString(requestBody);

            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/decide"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

            // Send the request and get the response
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            // Check if the request was successful
            if (response.statusCode() != 200) {
                throw new RuntimeException("Server returned status code: " + response.statusCode());
            };

            // Parse the response
            DecisionResponse decisionResponse = objectMapper.readValue(
                response.body(),
                DecisionResponse.class
            );

            return decisionResponse.getDecision();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to make decision", e);
        }
    }

    @Override
    public Optional<GameObserver> getObserver() {
        return observer;
    }
    
    @Override
    public DiscardDeck getDiscardDeck() {
        return this.discardDeck;
    }

    @Override
    public DrawDeck getDrawDeck() {
        return this.drawDeck;
    }

    @Override
    public Hand getHand() {
        return this.hand;
    }

    @Override
    public void setHand(Hand hand) {
        this.hand = hand;
    }

    /**
     * Logs an event to the server
     */
    public void logEvent(Event event, GameState state) {
        try {
            // Create the request body
            LogEventRequest requestBody = new LogEventRequest();
            requestBody.setEvent(event);
            requestBody.setPlayer_uuid(playerUuid);
            requestBody.setState(state);

            // Convert to JSON
            String requestJson = objectMapper.writeValueAsString(requestBody);

            // Build the HTTP Request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/log-event"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

            httpClient.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            
        } catch (Exception e) {
            System.err.println("Failed to log event: " + e.getMessage());
        }
    }
}
