package edu.brandeis.cosi103a.groupb.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;

import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.cards.Card.Type;
import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi.atg.api.event.GainCardEvent;
import edu.brandeis.cosi.atg.api.event.GameEvent;
import edu.brandeis.cosi.atg.api.event.PlayCardEvent;
import edu.brandeis.cosi.atg.api.decisions.BuyDecision;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi.atg.api.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.api.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.api.Engine;
import edu.brandeis.cosi.atg.api.GameDeck;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi.atg.api.PlayerViolationException;

public class GameEngine implements Engine {
    private final GameDeck deck;
    private final AtgPlayer player1;
    private final AtgPlayer player2;
    private final GameObserver observer;
    private GameState gameState;
    private final List<Card> physicalDeck = new ArrayList<>();
    private int turnCount = 1; //For logging

    private static GameEngine currentEngine;

    public GameEngine(AtgPlayer player1, AtgPlayer player2, GameObserver observer, GameDeck deck) {
        this.player1 = player1;
        this.player2 = player2;
        this.observer = observer;
        this.deck = deck;
        initializePhysicalDeck();
        this.gameState = null;
        currentEngine = this;
    }


    private void initializePhysicalDeck() {
        for (Card.Type type : this.deck.getCardTypes()) {
            int count = this.deck.getNumAvailable(type);
            for (int i = 0; i < count; i++) {
                physicalDeck.add(new Card(type, i)); // Create a new card and add it to the deck
            }
        }
        Collections.shuffle(physicalDeck); // Shuffle the deck
        System.out.println("Physical deck initialized with " + physicalDeck.size() + " cards.");
    }
    private Card drawCard() {
        if (physicalDeck.isEmpty()) {
            throw new IllegalStateException("The physical deck is empty!");
        }
        return physicalDeck.remove(0); // Remove and return the top card
    }
    private Card drawCardFromGameDeck(Card.Type cardType) {
        // Search for a card of the specified type in the physical deck
        for (Card card : physicalDeck) {
            if (card.getType() == cardType) {
                physicalDeck.remove(card); // Remove the card from the physical deck
                System.out.println("DEBUG: Drawing card of type " + cardType + " from the physical deck.");
                return card; // Return the drawn card
            }
        }

        // If no card of the specified type is found, throw an exception
        throw new IllegalStateException("No cards of type " + cardType + " are available in the physical deck.");
    }
    /**
     * Players' starting hand: 
     * -- 7x Bitcoin cards
     * -- 3x Method cards
     */
    private void initializeGameState(AtgPlayer player) {
        DrawDeck drawDeck = player.getDrawDeck();
    
        // Add 7 Bitcoin cards to the player's draw deck
        for (int i = 0; i < 7; i++) {
            Card card = drawCardFromGameDeck(Card.Type.BITCOIN); // Explicitly draw a Bitcoin card
            drawDeck.addCard(card); // Add to player's DrawDeck
        }
    
        // Add 3 Method cards to the player's draw deck
        for (int i = 0; i < 3; i++) {
            Card card = drawCardFromGameDeck(Card.Type.METHOD); // Explicitly draw a Method card
            drawDeck.addCard(card); // Add to player's DrawDeck
        }
    
        // Shuffle the player's draw deck
        drawDeck.shuffle();
    
        System.out.println("Initialized " + player.getName() + "'s draw deck.");
    }

    @Override
    public com.google.common.collect.ImmutableList<Player.ScorePair> play() throws PlayerViolationException {
        Random rand = new Random();
        int seed = rand.nextInt(2) + 1;

        if (seed == 1) {
            System.out.println(player1.getName() + ", you got lucky this time. You get to start first!");
            System.out.println(player2.getName() + ", don't be upset. Maybe your luck will come later!\n");

            // Initialize the game
            initializeGameState(player1);
            initializeGameState(player2);

            while (!isGameOver()) {
                processTurn(player1);
                if (isGameOver()) break;
                processTurn(player2);
                turnCount++;
            }
        } else {
            System.out.println(player2.getName() + ", you got lucky this time. You get to start first!");
            System.out.println(player1.getName() + ", don't be upset. Maybe your luck will come later!\n");

            // Initialize the game
            initializeGameState(player2);
            initializeGameState(player1);

            while (!isGameOver()) {
                processTurn(player2);
                if (isGameOver()) break;
                processTurn(player1);
                turnCount++;
            }
        }

        // Return the final scores as an ImmutableList
        return computeScores();
    }


    private void processTurn(AtgPlayer player) throws PlayerViolationException {
        handleMoneyPhase(player);
        handleBuyPhase(player);
        handleCleanupPhase(player);
    }

    /**
     * 1) Assign hand for this turn
     * 2) Show players' hand: card type + card value
     * 3) Show players' spendable money in this single turn
     * 4) Show player that they can only buy up to [1?] card (so far)
     * 5) Show player the buyable cards by listing out their types
     * 6) Ask the player how much money he wants to spend on this turn
     * @param player Player in action
     * @throws PlayerViolationException
     */
    private void handleMoneyPhase(AtgPlayer player) throws PlayerViolationException {
        List<Card> startingHand = new ArrayList<>();
    
        // 1). Assign hand for this turn
        for (int i = 0; i < 5; i++) {
            if (player.getDrawDeck().isEmpty()) { // If the draw deck is empty, move the discard deck into the draw deck
                player.getDiscardDeck().moveDeck(player.getDrawDeck());
            }
    
            Card drawnCard = player.getDrawDeck().drawCard();
            if (drawnCard != null) {
                startingHand.add(drawnCard);
            } else { // This will not likely happen
                System.out.println("WARNING: Player " + player.getName() + " does not have enough cards to draw a full hand!");
                break;
            }
        }
    
        System.out.println("DEBUG: Assigning hand to " + player.getName() + ":\n" + startingHand + "\n");
    
        // Initialize the GameState for the Money Phase
        this.gameState = new GameState(
            player.getName(),
            new Hand(
                com.google.common.collect.ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty at the start)
                com.google.common.collect.ImmutableList.copyOf(startingHand)          // Unplayed cards
            ),
            GameState.TurnPhase.MONEY,
            1, // Available actions (default to 1 for now)
            0, // Spendable money (starts at 0)
            1, // Available buys (default to 1 for now)
            deck
        );
    
        observer.notifyEvent(gameState, new GameEvent(player.getName() + " -- " + "Turn " + turnCount + "."));
    
        // 2). Allow the player to play cards to earn money
        while (gameState.getTurnPhase() == GameState.TurnPhase.MONEY) {
            List<Decision> options = new ArrayList<>();
            List<Card> updatedUnplayedCards = new ArrayList<>(gameState.getCurrentPlayerHand().getUnplayedCards());
            List<Card> playedCards = new ArrayList<>(gameState.getCurrentPlayerHand().getPlayedCards());
    
            // Add options to play money cards
            for (Card card : updatedUnplayedCards) {
                if (card.getType().getCategory() == Card.Type.Category.MONEY) { // Player can only play money cards
                    options.add(new PlayCardDecision(card));
                }
            }
    
            // Add an option to end the Money Phase
            options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
    
            System.out.println("DEBUG: Available decisions -> " + options.size());
    
            // Convert options to an ImmutableList
            ImmutableList<Decision> immutableOptions = com.google.common.collect.ImmutableList.copyOf(options);
    
            // Create an empty Optional<Event>
            Optional<Event> optionalEvent = Optional.empty();
    
            // Call makeDecision with all required arguments
            Decision decision = player.makeDecision(gameState, immutableOptions, optionalEvent);
    
            if (decision instanceof PlayCardDecision playCardDecision) {
                Card playedCard = playCardDecision.getCard();
                updatedUnplayedCards.remove(playedCard);
                playedCards.add(playedCard);
                observer.notifyEvent(gameState, new PlayCardEvent(playedCard, player.getName()));
    
                // ✅ Increase spendable money when playing a money card
                int newMoney = gameState.getSpendableMoney() + playedCard.getType().getValue();
                System.out.println("DEBUG: Updated spendable money: " + newMoney);
    
                // ✅ Update the game state with increased money
                gameState = new GameState(player.getName(), new Hand(
                        com.google.common.collect.ImmutableList.copyOf(playedCards),
                        com.google.common.collect.ImmutableList.copyOf(updatedUnplayedCards)
                    ),
                    GameState.TurnPhase.MONEY,
                    gameState.getAvailableActions(),
                    newMoney,
                    gameState.getAvailableBuys(),
                    deck
                );
            } else if (decision instanceof EndPhaseDecision) {
                // End the Money Phase and move to the Buy Phase
                gameState = new GameState(player.getName(), gameState.getCurrentPlayerHand(),
                                          GameState.TurnPhase.BUY, gameState.getAvailableActions(),
                                          gameState.getSpendableMoney(), gameState.getAvailableBuys(), deck);
            } else {
                throw new PlayerViolationException("Invalid decision in MONEY phase");
            }
        }
    }
    
    private void handleBuyPhase(AtgPlayer player) throws PlayerViolationException {
        System.out.println("DEBUG: Entering BUY phase. Available money: " + gameState.getSpendableMoney());
    
        while (gameState.getTurnPhase() == GameState.TurnPhase.BUY && gameState.getAvailableBuys() >= 1) {
            List<Decision> options = new ArrayList<>();
    
            // ✅ Add buyable cards based on money
            for (Card.Type type : deck.getCardTypes()) {
                if (deck.getNumAvailable(type) > 0 && gameState.getSpendableMoney() >= type.getCost()) {
                    System.out.println("DEBUG: Adding buy option for " + type + " (Cost: " + type.getCost() + ", Value: " + type.getValue() + ")");
                    options.add(new BuyDecision(type));
                }
            }
    
            // Add an option to end the Buy Phase
            options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
    
            System.out.println("DEBUG: Available decisions in BUY phase -> " + options.size());
    
            // Convert options to an ImmutableList
            ImmutableList<Decision> immutableOptions = com.google.common.collect.ImmutableList.copyOf(options);
    
            // Create an empty Optional<Event>
            Optional<Event> optionalEvent = Optional.empty();
    
            // Call makeDecision with all required arguments
            Decision decision = player.makeDecision(gameState, immutableOptions, optionalEvent);
    
            if (decision instanceof BuyDecision buyDecision) {
                Card.Type boughtCard = buyDecision.getCardType();
                observer.notifyEvent(gameState, new GainCardEvent(boughtCard, player.getName()));
    
                // Add the bought card to the player's discard deck & remove the card from the physical deck
                player.getDiscardDeck().addCard(drawCardFromGameDeck(boughtCard));
    
                // ✅ Deduct money after buying
                int newMoney = gameState.getSpendableMoney() - boughtCard.getCost();
    
                System.out.println("DEBUG: Updated spendable money after buying: " + newMoney);
                System.out.println("DEBUG: Updated " + player.getName() + "'s discard deck:");
                player.getDiscardDeck().toString();
    
                // ✅ Update game state after a purchase
                gameState = new GameState(
                    player.getName(),
                    gameState.getCurrentPlayerHand(),
                    GameState.TurnPhase.BUY,
                    gameState.getAvailableActions(),
                    newMoney,
                    gameState.getAvailableBuys() - 1,
                    deck
                );
            } else if (decision instanceof EndPhaseDecision) {
                // End the Buy Phase and move to the Cleanup Phase
                gameState = new GameState(
                    player.getName(),
                    gameState.getCurrentPlayerHand(),
                    GameState.TurnPhase.CLEANUP,
                    gameState.getAvailableActions(),
                    gameState.getSpendableMoney(),
                    gameState.getAvailableBuys(),
                    deck
                );
            } else {
                throw new PlayerViolationException("Invalid decision in BUY phase");
            }
        }
    }
    

    private void handleCleanupPhase(AtgPlayer player) {
        // Put hand into discard deck
        List<Card> cardsToMove = new ArrayList<>();
        cardsToMove.addAll(gameState.getCurrentPlayerHand().getPlayedCards());
        cardsToMove.addAll(gameState.getCurrentPlayerHand().getUnplayedCards());
        player.getDiscardDeck().addAllCards(cardsToMove);
    
        // Create a new GameState for the Cleanup Phase
        gameState = new GameState(
            player.getName(),
            new Hand(
                com.google.common.collect.ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty)
                com.google.common.collect.ImmutableList.copyOf(new ArrayList<Card>())  // Unplayed cards (empty)
            ),
            GameState.TurnPhase.CLEANUP,
            0, // Available actions
            0, // Spendable money
            0, // Available buys
            deck
        );
    
        System.out.println("DEBUG: Discarding current hand...");
        System.out.println("DEBUG: Updated " + player.getName() + "'s discard deck:");
        player.getDiscardDeck().toString();
        observer.notifyEvent(gameState, new GameEvent(player.getName() + "'s turn ends"));
    }

    private boolean isGameOver() {
        return deck.getNumAvailable(Card.Type.FRAMEWORK) == 0;
    }

    public static List<Player.ScorePair> getCurrentScores() {
        if (currentEngine == null) {
            throw new IllegalStateException("GameEngine has not been initialized yet");
        }
        return currentEngine.computeScores();
    }

    private com.google.common.collect.ImmutableList<Player.ScorePair> computeScores() {
        List<Player.ScorePair> scores = new ArrayList<>();
        scores.add(new Player.ScorePair(player1, calculateScore(player1)));
        scores.add(new Player.ScorePair(player2, calculateScore(player2)));
    
        // Sort scores from most points to least
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
    
        // Convert to ImmutableList and return
        return com.google.common.collect.ImmutableList.copyOf(scores);
    }

    private int calculateScore(AtgPlayer player) {
        // Retrieve all cards from the player's discard and draw decks
        List<Card> playerMainDeck = new ArrayList<>();
        playerMainDeck.addAll(player.getDiscardDeck().getCards(player.getDiscardDeck().size())); // Get all cards from discard deck
        playerMainDeck.addAll(player.getDrawDeck().getCards(player.getDrawDeck().size()));       // Get all cards from draw deck

        // Add cards from the player's current hand if it's their turn
        if (gameState != null && gameState.getCurrentPlayerName().equals(player.getName())) {
            playerMainDeck.addAll(gameState.getCurrentPlayerHand().getPlayedCards());
            playerMainDeck.addAll(gameState.getCurrentPlayerHand().getUnplayedCards());
        }

        // Calculate the score based on VICTORY cards
        int score = 0;
        for (Card card : playerMainDeck) {
            if (card.getType().getCategory() == Card.Type.Category.VICTORY) {
                score += card.getType().getValue();
            }
        }
        return score;
    }   

    public GameState getGameState() {
        return this.gameState;
    }
}