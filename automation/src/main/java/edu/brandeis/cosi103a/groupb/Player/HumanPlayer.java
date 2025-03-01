package edu.brandeis.cosi103a.groupb.Player;

import edu.brandeis.cosi103a.groupb.Decisions.*;
import edu.brandeis.cosi103a.groupb.Decks.DiscardDeck;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameState;

import java.util.*;

/**
 * Represents a human-controlled player using console input.
 */
public class HumanPlayer implements Player {
    private final String name;
    private DiscardDeck discardDeck;
    private DrawDeck drawDeck;

    private static final Scanner scanner = new Scanner(System.in);
    private final Optional<GameObserver> observer;

    public HumanPlayer(String name) {
        this.name = name;
        this.observer = Optional.of((GameObserver) new ConsoleGameObserver());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Decision makeDecision(GameState state, List<Decision> options) {
        System.out.println("\n" + name + "'s turn (" + state.getTurnPhase() + " phase)");
        System.out.println("Available options:");

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
}
