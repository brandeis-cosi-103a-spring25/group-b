package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi103a.groupb.Decisions.*;
import edu.brandeis.cosi103a.groupb.Events.*;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Represents a human-controlled player using console input.
 */
public class HumanPlayer implements Player {
    private final String name;
    private final Scanner scanner;
    private final Optional<GameObserver> observer;

    public HumanPlayer(String name) {
        this(name, new Scanner(System.in));
    }
    
    // Added constructor to inject a custom scanner
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
}
