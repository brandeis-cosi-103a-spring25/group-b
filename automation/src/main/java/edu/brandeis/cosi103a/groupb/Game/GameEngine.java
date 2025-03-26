package edu.brandeis.cosi103a.groupb.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi.atg.api.Engine;
import edu.brandeis.cosi.atg.api.EngineCreator;
import edu.brandeis.cosi.atg.api.GameDeck;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.GameState;
import edu.brandeis.cosi.atg.api.Hand;
import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi.atg.api.PlayerViolationException;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi.atg.api.event.*;
import edu.brandeis.cosi103a.groupb.Decks.DrawDeck;
import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;

public class GameEngine implements Engine {
    private GameDeck deck;
    private final AtgPlayer player1;
    private final AtgPlayer player2;
    private final GameObserver observer;
    private GameState gameState;
    private final List<Card> physicalDeck = new ArrayList<>();
    private int turnCount = 1; //For logging
    private int cardTotalCount = 0;

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

    @EngineCreator
    public static Engine createEngine(AtgPlayer player1, AtgPlayer player2, GameObserver observer) {
        // Encapsulate the creation of the game deck
        List<Card.Type> actionCards = Arrays.stream(Card.Type.values())
        .filter(type -> type.getCategory() == Card.Type.Category.ACTION)
        .collect(Collectors.toList());
        Map<Card.Type, Integer> cardCounts = new HashMap<>();
        // Set up initial counts for non-action cards.
        cardCounts.put(Card.Type.BITCOIN, 60);
        cardCounts.put(Card.Type.ETHEREUM, 40);
        cardCounts.put(Card.Type.DOGECOIN, 30);
        cardCounts.put(Card.Type.METHOD, 14);
        cardCounts.put(Card.Type.MODULE, 8);
        cardCounts.put(Card.Type.FRAMEWORK, 8);
        // Add 10 copies for each action card.
        for (Card.Type actionType : actionCards) {
            cardCounts.put(actionType, 10);
        }
        GameDeck deck = new GameDeck(ImmutableMap.copyOf(cardCounts));
        return new GameEngine(player1, player2, observer, deck);
    }

    private void initializePhysicalDeck() {
        for (Card.Type type : this.deck.getCardTypes()) {
            int count = this.deck.getNumAvailable(type);
            for (int i = 0; i < count; i++) {
                physicalDeck.add(new Card(type, i)); // Create a new card and add it to the deck
            }
            this.cardTotalCount = count;
        }
        Collections.shuffle(physicalDeck); // Shuffle the deck
        System.out.println("Physical deck initialized with " + physicalDeck.size() + " cards.");
    }

    private Card drawCardFromGameDeck(Card.Type cardType) {
        // Search for a card of the specified type in the physical deck
        for (Card card : physicalDeck) {
            if (card.getType() == cardType) {
                physicalDeck.remove(card); // Remove the card from the physical deck
                System.out.println("DEBUG: Removing card of type " + cardType + " from the physical deck.");
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
    private void initializeGameState(AtgPlayer player) throws PlayerViolationException {
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
        Map<Card.Type, Integer> cardCounts = new HashMap<>(this.deck.getCardCounts());
        cardCounts.put(Card.Type.BITCOIN, cardCounts.get(Card.Type.BITCOIN) - 7);
        cardCounts.put(Card.Type.METHOD, cardCounts.get(Card.Type.METHOD) - 3);
        this.deck = new GameDeck(ImmutableMap.copyOf(cardCounts));
        // Shuffle the player's draw deck
        drawDeck.shuffle();
    
        System.out.println("Initialized " + player.getName() + "'s draw deck.");
        this.gameState = new GameState(
            player.getName(),
            new Hand(ImmutableList.copyOf(new ArrayList<Card>()), ImmutableList.copyOf(new ArrayList<Card>())),
            GameState.TurnPhase.ACTION,
            1, // Starting actions
            0, // Starting money
            1, // Starting buys
            deck
        );
        this.distributeCard(player);
    }

    @Override
    public ImmutableList<Player.ScorePair> play() throws PlayerViolationException {
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
        // NEW: Process Action Phase before existing phases.
        handleActionPhase(player);
        handleMoneyPhase(player);
        handleBuyPhase(player);
        handleCleanupPhase(player);
    }

    /**
     * Handles the Action Phase by allowing the player to play action cards.
     * Assumes that a new GameState.TurnPhase.ACTION constant has been defined.
     */
    private void handleActionPhase(AtgPlayer player) throws PlayerViolationException {
        // Initialize Action Phase with 1 starting action.
        observer.notifyEvent(gameState, new GameEvent(player.getName() + " -- begins ACTION phase."));
        // Reinitialize the state for a new player
        this.gameState = new GameState(
            player.getName(),
            player.getHand(),
            GameState.TurnPhase.ACTION, 
            1,
            0,
            1,
            deck
        );
        // Loop until the player chooses to end the Action Phase.
        while (gameState.getTurnPhase() == GameState.TurnPhase.ACTION && gameState.getAvailableActions() >= 1) {
            List<Decision> options = new ArrayList<>();
            List<Card> unplayedActions = new ArrayList<>();
            // List all unplayed action cards in hand (assumes Card.Type.Category.ACTION is defined)
            for (Card card : gameState.getCurrentPlayerHand().getUnplayedCards()) {
                if (card.getType().getCategory() == Card.Type.Category.ACTION && card.getType() != Card.Type.MONITORING) {
                    unplayedActions.add(card);
                    options.add(new PlayCardDecision(card));
                }
            }
            // Always provide an option to end the Action Phase.
            options.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));
            ImmutableList<Decision> immutableOptions = ImmutableList.copyOf(options);
            Decision decision = player.makeDecision(gameState, immutableOptions, Optional.empty());
            if (decision instanceof PlayCardDecision playDecision) {
                Card actionCard = playDecision.getCard();
                observer.notifyEvent(gameState, new PlayCardEvent(actionCard, player.getName()));
                // Remove played card from unplayed set and add to played set.
                List<Card> updatedUnplayed = new ArrayList<>(gameState.getCurrentPlayerHand().getUnplayedCards());
                updatedUnplayed.remove(actionCard);
                List<Card> playedCards = new ArrayList<>(gameState.getCurrentPlayerHand().getPlayedCards());
                playedCards.add(actionCard);
                Hand newHand = new Hand(ImmutableList.copyOf(playedCards), ImmutableList.copyOf(updatedUnplayed));
                gameState = new GameState(
                    player.getName(),
                    newHand,
                    gameState.getTurnPhase(),
                    gameState.getAvailableActions()-1,
                    gameState.getSpendableMoney(),
                    gameState.getAvailableBuys(),
                    deck
                );
                this.updatePlayerHand(player);
                // Dispatch the action card effect based on card type.
                switch (actionCard.getType()) {
                    case BACKLOG:
                        gameState = processBacklogEffect(player, gameState);
                        break;
                    case HACK:
                        gameState = processHackEffect(player, gameState);
                        break;
                    case DAILY_SCRUM:
                        gameState = processDailyScrumEffect(player, gameState);
                        break;
                    case IPO:
                        gameState = processIpoEffect(player, gameState);
                        break;
                    case CODE_REVIEW:
                        gameState = processCodeReviewEffect(player, gameState);
                        break;
                    case TECH_DEBT:
                        gameState = processTechDebtEffect(player, gameState);
                        break;
                    case REFACTOR:
                        gameState = processRefactorEffect(player, gameState);
                        break;
                    case PARALLELIZATION:
                        gameState = processParallelizationEffect(player, gameState);
                        break;
                    case EVERGREEN_TEST:
                        gameState = processEvergreenTestEffect(player, gameState);
                        break;
                    default:
                        System.out.println("Action effect for " + actionCard.getType() + " is not implemented.");
                        break;
                }
            } else if (decision instanceof EndPhaseDecision) {
                // End the Action Phase and transition to the Money Phase.
                gameState = new GameState(
                    player.getName(),
                    gameState.getCurrentPlayerHand(),
                    GameState.TurnPhase.MONEY,
                    0, // Reset available actions
                    gameState.getSpendableMoney(),
                    gameState.getAvailableBuys(),
                    deck
                );
                observer.notifyEvent(gameState, new GameEvent(player.getName() + " -- ends ACTION phase."));
            } else {
                throw new PlayerViolationException("Invalid decision in ACTION phase");
            }
        }
        gameState = new GameState(
            player.getName(),
            gameState.getCurrentPlayerHand(),
            GameState.TurnPhase.MONEY,
            0, // Reset available actions
            gameState.getSpendableMoney(),
            gameState.getAvailableBuys(),
            deck
        );
    }

    private void handleMoneyPhase(AtgPlayer player) throws PlayerViolationException {    
        // 1). Allow the player to play cards to earn money
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
            ImmutableList<Decision> immutableOptions = ImmutableList.copyOf(options);
    
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
                Hand newHand = new Hand(
                    ImmutableList.copyOf(playedCards),
                    ImmutableList.copyOf(updatedUnplayedCards)
                );
                // ✅ Update the game state with increased money
                gameState = new GameState(
                    player.getName(), 
                    newHand,
                    gameState.getTurnPhase(),
                    gameState.getAvailableActions(),
                    newMoney,
                    gameState.getAvailableBuys(),
                    deck
                );
                this.updatePlayerHand(player);
            } else if (decision instanceof EndPhaseDecision) {
                // End the Money Phase and move to the Buy Phase
                gameState = new GameState(
                    player.getName(),
                    gameState.getCurrentPlayerHand(),
                    GameState.TurnPhase.BUY,
                    gameState.getAvailableActions(),
                    gameState.getSpendableMoney(),
                    gameState.getAvailableBuys(),
                    deck
                );
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
            ImmutableList<Decision> immutableOptions = ImmutableList.copyOf(options);
    
            // Create an empty Optional<Event>
            Optional<Event> optionalEvent = Optional.empty();
    
            // Call makeDecision with all required arguments
            Decision decision = player.makeDecision(gameState, immutableOptions, optionalEvent);
    
            if (decision instanceof BuyDecision buyDecision) {
                Card.Type boughtCard = buyDecision.getCardType();
                observer.notifyEvent(gameState, new GainCardEvent(boughtCard, player.getName()));
    
                // Add the bought card to the player's discard deck & remove the card from the physical deck and gamedeck
                player.getDiscardDeck().addCard(drawCardFromGameDeck(boughtCard));
                Map<Card.Type, Integer> cardCounts = new HashMap<>(this.deck.getCardCounts());
                cardCounts.put(boughtCard, cardCounts.get(boughtCard) - 1);
                this.deck = new GameDeck(ImmutableMap.copyOf(cardCounts));
    
                // ✅ Deduct money after buying
                int newMoney = gameState.getSpendableMoney() - boughtCard.getCost();
    
                System.out.println("DEBUG: Updated spendable money after buying: " + newMoney);
                System.out.println("DEBUG: Updated " + player.getName() + "'s discard deck:");
                System.out.println(player.getDiscardDeck());
    
                // ✅ Update game state after a purchase
                gameState = new GameState(
                    player.getName(),
                    gameState.getCurrentPlayerHand(),
                    gameState.getTurnPhase(),
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
        gameState = new GameState(
            player.getName(),
            gameState.getCurrentPlayerHand(),
            GameState.TurnPhase.CLEANUP,
            gameState.getAvailableActions(),
            gameState.getSpendableMoney(),
            gameState.getAvailableBuys(),
            deck
        );
    }
    
    private void handleCleanupPhase(AtgPlayer player) throws PlayerViolationException {
        // Put hand into discard deck
        List<Card> cardsToMove = new ArrayList<>();
        cardsToMove.addAll(gameState.getCurrentPlayerHand().getPlayedCards());
        cardsToMove.addAll(gameState.getCurrentPlayerHand().getUnplayedCards());
        player.getDiscardDeck().addAllCards(cardsToMove);
        System.out.println("DEBUG: Discarding current hand...");
        System.out.println("DEBUG: Updated " + player.getName() + "'s discard deck:");
        System.out.println(player.getDiscardDeck());
        this.distributeCard(player); // draw 5 new cards at the end of each turn
        observer.notifyEvent(gameState, new GameEvent(player.getName() + "'s turn ends"));
    }

    private void distributeCard(AtgPlayer player) throws PlayerViolationException {
        List<Card> startingHand = new ArrayList<>();
        player.setHand(new Hand(
            ImmutableList.copyOf(new ArrayList<Card>()), // Played cards (empty at the start)
            ImmutableList.copyOf(new ArrayList<Card>()))
        );
        // Draw 5 cards
        Hand newHand = this.playerDrawCard(player, 5);    
        System.out.println("DEBUG: Assigning hand to " + player.getName() + ":\n" + startingHand + "\n");
        // Update the GameState after dealing a hand
        this.gameState = new GameState(
            player.getName(),
            newHand,
            gameState.getTurnPhase(), 
            0,
            0,
            0,
            deck
        );
        this.updatePlayerHand(player);
        observer.notifyEvent(gameState, new GameEvent(player.getName() + " -- " + "Turn " + turnCount + "."));
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


    private ImmutableList<Player.ScorePair> computeScores() {
        List<Player.ScorePair> scores = new ArrayList<>();
        scores.add(new Player.ScorePair(player1, calculateScore(player1)));
        scores.add(new Player.ScorePair(player2, calculateScore(player2)));
    
        // Sort scores from most points to least
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
    
        // Convert to ImmutableList and return
        return ImmutableList.copyOf(scores);
    }

    private int calculateScore(AtgPlayer player) {
        // Retrieve all cards from the player's discard and draw decks
        List<Card> playerMainDeck = new ArrayList<>();
        playerMainDeck.addAll(player.getDiscardDeck().getCards()); // Get all cards from discard deck
        playerMainDeck.addAll(player.getDrawDeck().getCards());       // Get all cards from draw deck
    
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

    // --- Action Card Helper Methods ---

    // Detailed implementations for each action card effect in GameEngine

    // BACKLOG: +1 Action; Discard any number of cards, then draw that many.
    private GameState processBacklogEffect(AtgPlayer player, GameState state) throws PlayerViolationException {
        System.out.println("Processing BACKLOG effect for " + player.getName());
        // Grant bonus action.
        int bonusActions = 1;
        int totalDiscarded = 0;
        
        // Simulate a discard phase.
        List<Card> currentUnplayed = new ArrayList<>(state.getCurrentPlayerHand().getUnplayedCards());
        
        // Repeatedly prompt the player for a discard decision.
        while (!currentUnplayed.isEmpty()) {
            // Build available discard options from current hand.
            List<Decision> discardOptions = new ArrayList<>();
            for (Card card : currentUnplayed) {
                discardOptions.add(new DiscardCardDecision(card));
            }
            // Also include an option to end discarding.
            discardOptions.add(new EndPhaseDecision(GameState.TurnPhase.DISCARD));
            
            ImmutableList<Decision> options = ImmutableList.copyOf(discardOptions);
            // Prompt the player (using their decision method).
            Decision decision = player.makeDecision(state, options, Optional.empty());
            if (decision instanceof DiscardCardDecision discardDecision) {
                Card discarded = discardDecision.getCard();
                currentUnplayed.remove(discarded);
                totalDiscarded++;
                player.getDiscardDeck().addCard(discarded);
                observer.notifyEvent(state, new DiscardCardEvent(discarded.getType(), player.getName()));
                System.out.println(player.getName() + " discards " + discarded + " via BACKLOG.");
            } else if (decision instanceof EndPhaseDecision) {
                break;
            } else {
                throw new PlayerViolationException("Invalid decision during BACKLOG discard phase");
            }
        }
        System.out.println(player.getName() + " discards " + totalDiscarded + " card(s) via BACKLOG.");
        player.setHand(new Hand(player.getHand().getPlayedCards(), ImmutableList.copyOf(currentUnplayed)));
        // Draw as many cards as were discarded.
        Hand newHand = this.playerDrawCard(player, totalDiscarded);
        player.setHand(newHand);
        int newActions = state.getAvailableActions() + bonusActions;
        System.out.println(player.getName() + " no. of actions after BACKLOG: " + newActions);
        // Update the GameState with the new hand and bonus actions.
        return new GameState(
                state.getCurrentPlayerName(),
                newHand,
                state.getTurnPhase(),
                newActions,
                state.getSpendableMoney(),
                state.getAvailableBuys(),
                state.getDeck()
        );
    }

    // HACK: (Attack card) +2 Money; Each other player discards down to 3 cards in hand.
    private GameState processHackEffect(AtgPlayer player, GameState state) throws PlayerViolationException {
        System.out.println("Processing HACK effect for " + player.getName());
        // The active player gains +2 money.
        int newMoney = state.getSpendableMoney() + 2;
        
        // Identify the opponent (assuming a two-player game)
        AtgPlayer opponent = (player == this.player1) ? this.player2 : this.player1;
        
        // Check opponent's unplayed hand for a MONITORING card.
        List<Card> oppUnplayed = new ArrayList<>(opponent.getHand().getUnplayedCards());
        boolean hasMonitoring = false;
        for (Card card : oppUnplayed) {
            if (card.getType() == Card.Type.MONITORING) {
                hasMonitoring = true;
                break;
            }
        }
        
        // If a MONITORING card is available, prompt with reaction options.
        if (hasMonitoring) {
            List<Decision> reactionOptions = new ArrayList<>();
            for (Card card : oppUnplayed) {
                if (card.getType() == Card.Type.MONITORING) {
                    reactionOptions.add(new PlayCardDecision(card)); // Option to reveal
                }
            }
            // Also include an option to not react.
            reactionOptions.add(new EndPhaseDecision(GameState.TurnPhase.REACTION));
            ImmutableList<Decision> reactionDecisions = ImmutableList.copyOf(reactionOptions);
            Decision reaction = opponent.makeDecision(state, reactionDecisions, Optional.empty());
            if (reaction instanceof PlayCardDecision playCardDecision) {
                Card chosenCard = playCardDecision.getCard();
                // Opponent reveals a MONITORING card and avoids the attack.
                System.out.println(opponent.getName() + " reveals a MONITORING card to avoid HACK attack.");
                observer.notifyEvent(state, new PlayCardEvent(chosenCard, opponent.getName()));
                // gain 2 cards
                Hand newHand = this.playerDrawCard(opponent, 2);
                opponent.setHand(newHand);
            } else {
                // Opponent did not reveal; force discard down to 3 cards.
                System.out.println(opponent.getName() + " does not reveal MONITORING; must discard down to 3 cards.");
                forcedDiscardDownTo3(opponent, state);
            }
        } else {
            // No monitoring available; force discard.
            System.out.println(opponent.getName() + " lacks MONITORING and must discard down to 3 cards.");
            forcedDiscardDownTo3(opponent, state);
        }
        System.out.println(player.getName() + " gains 2 money from HACK attack. Money now: " + newMoney);
        // Update active player's state with the bonus money.
        return new GameState(
            state.getCurrentPlayerName(),
            state.getCurrentPlayerHand(),
            state.getTurnPhase(),
            state.getAvailableActions(),
            newMoney,
            state.getAvailableBuys(),
            state.getDeck()
        );
    }
    
    /**
     * Helper to force the opponent to discard until their unplayed hand size is at most 3.
     * Discard decisions (without an EndPhase option) are repeatedly prompted.
     */
    private void forcedDiscardDownTo3(AtgPlayer opponent, GameState state) throws PlayerViolationException {
        while(opponent.getHand().getUnplayedCards().size() > 3) {
            List<Decision> discardOptions = new ArrayList<>();
            // Build a discard option for every card in the opponent's unplayed hand.
            for (Card card : opponent.getHand().getUnplayedCards()) {
                discardOptions.add(new DiscardCardDecision(card));
            }
            ImmutableList<Decision> options = ImmutableList.copyOf(discardOptions);
            Decision decision = opponent.makeDecision(state, options, Optional.empty());
            if (decision instanceof DiscardCardDecision discardDecision) {
                Card discarded = discardDecision.getCard();
                // Update the opponent's hand: remove the discarded card.
                List<Card> played = new ArrayList<>(opponent.getHand().getPlayedCards());
                List<Card> unplayed = new ArrayList<>(opponent.getHand().getUnplayedCards());
                unplayed.remove(discarded);
                opponent.setHand(new Hand(ImmutableList.copyOf(played), ImmutableList.copyOf(unplayed)));
                // Add discarded card to opponent's discard deck.
                opponent.getDiscardDeck().addCard(discarded);
                observer.notifyEvent(state, new DiscardCardEvent(discarded.getType(), opponent.getName()));
                System.out.println(opponent.getName() + " discards " + discarded + " due to HACK attack.");
            } else {
                throw new PlayerViolationException("Invalid decision during forced discard for HACK.");
            }
        }
        System.out.println(opponent.getName() + " now has " + opponent.getHand().getUnplayedCards().size() + " unplayed card(s).");
    }

    // DAILY_SCRUM: +4 Cards, +1 Buy; Each other player draws a card.
    private GameState processDailyScrumEffect(AtgPlayer player, GameState state) throws PlayerViolationException {
        System.out.println("Processing DAILY_SCRUM effect for " + player.getName());
        // Draw 4 cards for the player.
        Hand newHand = this.playerDrawCard(player, 4);
        // Increase available buys by 1.
        int newBuys = state.getAvailableBuys() + 1;
        player.setHand(newHand);
        System.out.println(player.getName() + " draws 4 cards. The available buy is updated to: " + newBuys);
        // Draw a card for the opponent, assuming there are only 2 players for now
        AtgPlayer opponent = null;
        if (player != this.player1) {
            opponent = this.player1;
        } else {
            opponent = this.player2;
        }
        // Draw a card for the opponent.
        Hand opponentNewHand = this.playerDrawCard(opponent, 1);
        opponent.setHand(opponentNewHand);
        return new GameState(
                state.getCurrentPlayerName(),
                newHand,
                state.getTurnPhase(),
                state.getAvailableActions(),
                state.getSpendableMoney(),
                newBuys,
                state.getDeck());
    }

    // IPO: +2 Cards, +1 Action, +2 Money.
    private GameState processIpoEffect(AtgPlayer player, GameState state) throws PlayerViolationException {
        System.out.println("Processing IPO effect for " + player.getName());
        // Draw 2 cards
        Hand newHand = this.playerDrawCard(player, 2);
        int newActions = state.getAvailableActions() + 1;
        int newMoney = state.getSpendableMoney() + 2;
        player.setHand(newHand);
        System.out.println(player.getName() + " draws 2 cards. The available actions now are " + newActions + " and money are updated to: " + newMoney);
        return new GameState(
                state.getCurrentPlayerName(),
                newHand,
                state.getTurnPhase(),
                newActions,
                newMoney,
                state.getAvailableBuys(),
                state.getDeck());
    }

    // CODE_REVIEW: +1 Card, +2 Actions.
    private GameState processCodeReviewEffect(AtgPlayer player, GameState state) throws PlayerViolationException {
        System.out.println("Processing CODE_REVIEW effect for " + player.getName());
        // Draw 1 card
        Hand newHand = this.playerDrawCard(player, 1);
        int newActions = state.getAvailableActions() + 2;
        System.out.println(player.getName() + " draws 1 card. The available actions are updated to: " + newActions);
        player.setHand(newHand);
        return new GameState(
                state.getCurrentPlayerName(),
                newHand,
                state.getTurnPhase(),
                newActions,
                state.getSpendableMoney(),
                state.getAvailableBuys(),
                state.getDeck());
    }

    // TECH_DEBT: +1 Card, +1 Action, +1 Money; Discard one card per empty Supply pile.
    private GameState processTechDebtEffect(AtgPlayer player, GameState state) throws PlayerViolationException {
        System.out.println("Processing TECH_DEBT effect for " + player.getName());
        // Count empty supply piles.
        int emptySupplies = 0;
        for (Map.Entry<Card.Type, Integer> entry : state.getDeck().getCardCounts().entrySet()) {
            if (entry.getValue() == 0)
                emptySupplies++;
        }
        
        // Draw one card.
        Hand newHand = this.playerDrawCard(player, 1);
        List<Card> currentUnplayed = new ArrayList<>(newHand.getUnplayedCards());
        // Grant bonus action and money.
        int newActions = state.getAvailableActions() + 1;
        int newMoney = state.getSpendableMoney() + 1;

        
        // For each empty supply, prompt the player to discard a card.
        for (int i = 0; i < emptySupplies; i++) {
            if (currentUnplayed.isEmpty()) break;
            List<Decision> discardOptions = new ArrayList<>();
            for (Card card : currentUnplayed) {
                discardOptions.add(new DiscardCardDecision(card));
            }
            // No EndPhaseDecision here because discarding is mandatory.
            ImmutableList<Decision> options = ImmutableList.copyOf(discardOptions);
            Decision decision = player.makeDecision(state, options, Optional.empty());
            if (decision instanceof DiscardCardDecision discardDecision) {
                Card discarded = discardDecision.getCard();
                currentUnplayed.remove(discarded);
                observer.notifyEvent(state, new DiscardCardEvent(discarded.getType(), player.getName()));
                player.getDiscardDeck().addCard(discarded);
                System.out.println(player.getName() + " discards " + discarded + " due to TECH_DEBT.");
            } else {
                throw new PlayerViolationException("Invalid decision during TECH_DEBT discard phase");
            }
        }
        Hand updatedHand = new Hand(state.getCurrentPlayerHand().getPlayedCards(), ImmutableList.copyOf(currentUnplayed));
        player.setHand(updatedHand);
        System.out.println(player.getName() + " updated actions are " + newActions + " and buys are " + newMoney);
        return new GameState(
                state.getCurrentPlayerName(),
                newHand,
                state.getTurnPhase(),
                newActions,
                newMoney,
                state.getAvailableBuys(),
                state.getDeck()
        );
    }

    // REFACTOR: Trash a card from hand; Gain a card costing up to 2 more than the trashed card.
    private GameState processRefactorEffect(AtgPlayer player, GameState state) throws PlayerViolationException {
        System.out.println("Processing REFACTOR effect for " + player.getName());
        List<Card> currentUnplayed = new ArrayList<>(state.getCurrentPlayerHand().getUnplayedCards());
        if (currentUnplayed.isEmpty()) {
            System.out.println(player.getName() + " has no card to trash for REFACTOR.");
            return state;
        }
        // Build trash decisions from unplayed cards.
        List<Decision> trashOptions = new ArrayList<>();
        for (Card card : currentUnplayed) {
            trashOptions.add(new TrashCardDecision(card));
        }
        ImmutableList<Decision> options = ImmutableList.copyOf(trashOptions);
        Decision decision = player.makeDecision(state, options, Optional.empty());
        int maxCost = 0;
        if (decision instanceof TrashCardDecision trashDecision) {
            Card trashed = trashDecision.getCard();
            // Determine the maximum cost for the gain: trashed card cost + 2.
            maxCost = trashed.getType().getCost() + 2;
            currentUnplayed.remove(trashed);
            observer.notifyEvent(state, new TrashCardEvent(trashed.getType(), player.getName()));
            System.out.println(player.getName() + " trashes " + trashed + " via REFACTOR.");
        } else {
            throw new PlayerViolationException("Invalid decision in REFACTOR effect");
        }
        List<Decision> gainOptions = new ArrayList<>();
        for (Card.Type type : state.getDeck().getCardTypes()) {
            if (state.getDeck().getNumAvailable(type) > 0 && type.getCost() <= maxCost) {
                gainOptions.add(new GainCardDecision(type));
            }
        }
        Hand newHand = new Hand(state.getCurrentPlayerHand().getPlayedCards(), ImmutableList.copyOf(currentUnplayed));
        ImmutableList<Decision> immutableOptions = ImmutableList.copyOf(gainOptions);
        // Update the state for makeDecision
        GameState newState = new GameState(
            state.getCurrentPlayerName(),
            newHand,
            state.getTurnPhase(),
            state.getAvailableActions(),
            state.getSpendableMoney(),
            state.getAvailableBuys(),
            state.getDeck()
        );
        // No available options
        if (immutableOptions.isEmpty()) {
            System.out.println(player.getName() + " has no valid gain options after trashing for REFACTOR.");
            return newState;
        } else {
            Decision gainDecision = player.makeDecision(newState, immutableOptions, Optional.empty());
            if (gainDecision instanceof GainCardDecision gainCardDecision) {
                Card.Type gainType = gainCardDecision.getCardType();
                if (state.getDeck().getNumAvailable(gainType) > 0 && gainType.getCost() <= maxCost) {
                    player.getDiscardDeck().addCard(drawCardFromGameDeck(gainType));
                    observer.notifyEvent(state, new GainCardEvent(gainType, player.getName()));
                    Map<Card.Type, Integer> cardCounts = new HashMap<>(this.deck.getCardCounts());
                    cardCounts.put(gainType, cardCounts.get(gainType) - 1);
                    this.deck = new GameDeck(ImmutableMap.copyOf(cardCounts));
                    System.out.println(player.getName() + " gains a " + gainType + " card via REFACTOR.");
                } else {
                    System.out.println(player.getName() + " attempted to gain an invalid card type for REFACTOR.");
                }
            } else {
                throw new PlayerViolationException("Invalid decision in REFACTOR gain phase");
            }
        }        
        // update the gamestate with new deck
        return new GameState(
                newState.getCurrentPlayerName(),
                newState.getCurrentPlayerHand(),
                newState.getTurnPhase(),
                newState.getAvailableActions(),
                newState.getSpendableMoney(),
                newState.getAvailableBuys(),
                this.deck
        );
    }

    // PARALLELIZATION: You may play an Action card from your hand twice.
    private GameState processParallelizationEffect(AtgPlayer player, GameState state) throws PlayerViolationException {
        System.out.println("Processing PARALLELIZATION effect for " + player.getName());
        // List the unplayed action cards (excluding PARALLELIZATION itself)
        List<Card> unplayed = new ArrayList<>(state.getCurrentPlayerHand().getUnplayedCards());
        List<Decision> possibleActions = new ArrayList<>();
        for (Card card : unplayed) {
            if (card.getType().getCategory() == Card.Type.Category.ACTION &&
                card.getType() != Card.Type.PARALLELIZATION) {
                possibleActions.add(new PlayCardDecision(card));
            }
        }
        if (possibleActions.isEmpty()) {
            System.out.println("No available action to duplicate.");
            return state;
        }
        ImmutableList<Decision> immutableOptions = ImmutableList.copyOf(possibleActions);
        // For simulation, choose the first action card.
        Decision gainDecision = player.makeDecision(state, immutableOptions, Optional.empty());
        if (gainDecision instanceof PlayCardDecision playCardDecision) {
            Card chosenCard = playCardDecision.getCard();
            System.out.println(player.getName() + " chooses to play " + chosenCard + " twice due to PARALLELIZATION.");
            observer.notifyEvent(state, new PlayCardEvent(chosenCard, player.getName()));
            // Execute the chosen action’s effect twice.
            switch (chosenCard.getType()) {
                case BACKLOG:
                    state = processBacklogEffect(player, state);
                    state = processBacklogEffect(player, state);
                    break;
                case HACK:
                    state = processHackEffect(player, state);
                    state = processHackEffect(player, state);
                    break;
                case DAILY_SCRUM:
                    state = processDailyScrumEffect(player, state);
                    state = processDailyScrumEffect(player, state);
                    break;
                case IPO:
                    state = processIpoEffect(player, state);
                    state = processIpoEffect(player, state);
                    break;
                case CODE_REVIEW:
                    state = processCodeReviewEffect(player, state);
                    state = processCodeReviewEffect(player, state);
                    break;
                case TECH_DEBT:
                    state = processTechDebtEffect(player, state);
                    state = processTechDebtEffect(player, state);
                    break;
                case REFACTOR:
                    state = processRefactorEffect(player, state);
                    state = processRefactorEffect(player, state);
                    break;
                case EVERGREEN_TEST:
                    state = processEvergreenTestEffect(player, state);
                    state = processEvergreenTestEffect(player, state);
                    break;
                default:
                    System.out.println("No handling for " + chosenCard.getType() + " in PARALLELIZATION.");
                    break;
            }
        } else {
            throw new PlayerViolationException("Invalid decision in PARALLELIZATION effect");
        }
        return state;
    }

    // EVERGREEN_TEST: (Attack card) +2 Cards; Each other player gains a BUG.
    // Incorporates monitoring reaction: opponents with a MONITORING card may reveal it to avoid gaining a BUG.
    private GameState processEvergreenTestEffect(AtgPlayer player, GameState state) throws PlayerViolationException {
        System.out.println("Processing EVERGREEN_TEST effect for " + player.getName());
        
        // Active player draws 2 cards as before.
        Hand newHand = this.playerDrawCard(player, 2);
        player.setHand(newHand);
        // Identify the opponent.
        AtgPlayer opponent = (player == this.player1) ? this.player2 : this.player1;
        List<Card> oppUnplayed = new ArrayList<>(opponent.getHand().getUnplayedCards());
        boolean hasMonitoring = false;
        for (Card card : oppUnplayed) {
            if (card.getType() == Card.Type.MONITORING) {
                hasMonitoring = true;
                break;
            }
        }
        // Query the users if they use MONITORING card to avoid the attack.
        if (hasMonitoring) {
            List<Decision> reactionOptions = new ArrayList<>();
            for (Card card : oppUnplayed) {
                if (card.getType() == Card.Type.MONITORING) {
                    reactionOptions.add(new PlayCardDecision(card));
                }
            }
            reactionOptions.add(new EndPhaseDecision(GameState.TurnPhase.REACTION));
            ImmutableList<Decision> reactionDecisions = ImmutableList.copyOf(reactionOptions);
            Decision reaction = opponent.makeDecision(state, reactionDecisions, Optional.empty());
            if (reaction instanceof PlayCardDecision playCardDecision) {
                // Opponent reveals MONITORING and avoids gaining a BUG.
                Card chosenCard = playCardDecision.getCard();
                // Opponent reveals a MONITORING card and avoids the attack.
                System.out.println(opponent.getName() + " reveals a MONITORING card to avoid EVERGREEN attack.");
                observer.notifyEvent(state, new PlayCardEvent(chosenCard, opponent.getName()));
                // gain 2 cards
                Hand oppNewHand = this.playerDrawCard(opponent, 2);
                opponent.setHand(oppNewHand);
            } else {
                // Opponent does not react; add a BUG to their discard deck.
                System.out.println(opponent.getName() + " does not reveal MONITORING and gains a BUG card.");
                addBugToOpponent(opponent, state);
            }
        } else {
            System.out.println(opponent.getName() + " lacks MONITORING and gains a BUG card.");
            addBugToOpponent(opponent, state);
        }
        
        // Return updated GameState for the active player.
        return new GameState(
            state.getCurrentPlayerName(),
            newHand,
            state.getTurnPhase(),
            state.getAvailableActions(),
            state.getSpendableMoney(),
            state.getAvailableBuys(),
            state.getDeck()
        );
    }
    
    /**
     * Helper method to add a BUG card to the opponent's discard deck.
     * Assumes BUG is defined as a Card.Type in your API.
     */
    private void addBugToOpponent(AtgPlayer opponent, GameState state) {
        Card bugCard = new Card(Card.Type.BUG, this.cardTotalCount++);
        opponent.getDiscardDeck().addCard(bugCard);
        observer.notifyEvent(state, new GainCardEvent(Card.Type.BUG, opponent.getName()));
        System.out.println("BUG card added to " + opponent.getName() + "'s discard deck due to EVERGREEN_TEST.");
    }

    /**
     * Updates the active player's hand field with the current hand stored in gameState.
     * Assumes that AtgPlayer now provides a setHand(Hand hand) method.
     */
    private void updatePlayerHand(AtgPlayer player) {
        player.setHand(gameState.getCurrentPlayerHand());
    }

    private Hand playerDrawCard(AtgPlayer player, int numCards) {
        List<Card> unplayed = new ArrayList<>(player.getHand().getUnplayedCards());
        List<Card> played = new ArrayList<>(player.getHand().getPlayedCards());
        for (int i = 0; i < numCards; i++) {
            if (player.getDrawDeck().isEmpty()) {
                player.getDiscardDeck().moveDeck(player.getDrawDeck());
            }
            Card drawn = player.getDrawDeck().drawCard();
            if (drawn != null) {
                unplayed.add(drawn);
            } else { // This will not likely happen
                System.out.println("WARNING: Player " + player.getName() + " does not have enough cards to draw a full hand!");
                break;
            }
            System.out.println(player.getName() + " draws " + drawn + " from draw deck.");
        }
        return new Hand(ImmutableList.copyOf(played), ImmutableList.copyOf(unplayed));
    }

    public GameState getGameState() {
        return this.gameState;
    }
}