package edu.brandeis.cosi103a.groupb.Player;

import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi.atg.api.cards.Card;

import java.util.*;

import com.google.common.collect.ImmutableList;

/**
 * Represents a human-controlled player using console input.
 */
public class HumanPlayer implements AtgPlayer {
    private final String name;
    private DiscardDeck discardDeck = new DiscardDeck();
    private DrawDeck drawDeck = new DrawDeck();
    private Hand hand = new Hand(
            ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty)
            ImmutableList.copyOf(new ArrayList<Card>())  // Unplayed cards (empty)
        );

    // Remove "static" so that the scanner instance can be set from the constructor.
    private final Scanner scanner;
    private final Optional<GameObserver> observer;

    public HumanPlayer(String name) {
        this(name, new Scanner(System.in));
    }
    
    // Constructor to inject a custom scanner.
    public HumanPlayer(String name, Scanner scanner) {
        this.name = name;
        this.scanner = scanner;
        this.observer = Optional.of((GameObserver) new ConsoleGameObserver());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        if (reason.isPresent()) {
            System.out.println("Decision prompted by event: " + reason);
        } 
        
        for (int i = 0; i < options.size(); i++) {
            System.out.println("[" + i + "] " + options.get(i).getDescription());
        }
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
}
