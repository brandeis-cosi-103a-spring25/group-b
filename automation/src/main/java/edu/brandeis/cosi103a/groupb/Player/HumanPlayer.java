package edu.brandeis.cosi103a.groupb.Player;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.*;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi103a.groupb.Decks.*;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;

/**
 * Represents a human-controlled player using console input.
 * This class allows a human to make decisions during gameplay by
 * presenting options and collecting input through the console.
 */
public class HumanPlayer implements AtgPlayer {
    // Player identification
    private final String name;
    
    // Player's game components
    private DiscardDeck discardDeck = new DiscardDeck();
    private DrawDeck drawDeck = new DrawDeck();
    private Hand hand = new Hand(
            ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty)
            ImmutableList.copyOf(new ArrayList<Card>())  // Unplayed cards (empty)
        );

    // Input and output handling
    private final Scanner scanner;
    private final Optional<GameObserver> observer;

    /**
     * Creates a HumanPlayer with the specified name and a default System.in scanner.
     * 
     * @param name The player's name
     */
    public HumanPlayer(String name) {
        this(name, new Scanner(System.in));
    }
    
    /**
     * Creates a HumanPlayer with the specified name and custom scanner.
     * Useful for testing or alternate input sources.
     * 
     * @param name The player's name
     * @param scanner The scanner to use for input
     */
    public HumanPlayer(String name, Scanner scanner) {
        this.name = name;
        this.scanner = scanner;
        this.observer = Optional.of(new ConsoleGameObserver());
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
     * Presents decision options to the human player and collects their choice.
     * 
     * @param state The current game state
     * @param options Available decision options
     * @param reason Optional event that prompted this decision
     * @return The chosen decision
     */
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        // Display reason for decision if available
        if (reason.isPresent()) {
            System.out.println("Decision prompted by event: " + reason);
        } 
        
        // Display available options
        for (int i = 0; i < options.size(); i++) {
            System.out.println("[" + i + "] " + options.get(i).getDescription());
        }
        
        // Get valid choice from user
        System.out.print("Choose an option (enter number): ");
        int choice = -1;
        while (choice < 0 || choice >= options.size()) {
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
            } else {
                scanner.next(); // Consume invalid input
            }
            if (choice < 0 || choice >= options.size()) {
                System.out.print("Invalid choice. Try again: ");
            }
        }
        return options.get(choice);
    }

    /**
     * Returns the game observer for this player.
     * 
     * @return The game observer
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
}
