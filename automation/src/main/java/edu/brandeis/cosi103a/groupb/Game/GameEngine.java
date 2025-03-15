package edu.brandeis.cosi103a.groupb.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.cards.Card.Type;
import edu.brandeis.cosi.atg.api.event.Event;
import edu.brandeis.cosi.atg.api.event.GainCardEvent;
import edu.brandeis.cosi.atg.api.event.GameEvent;
import edu.brandeis.cosi.atg.api.event.PlayCardEvent;
import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;
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
    private final Player player1;
    private final Player player2;
    private final GameObserver observer;
    private GameState gameState;
    private int turnCount = 1; //For logging

    private static GameEngine currentEngine;

    public GameEngine(Player player1, Player player2, GameObserver observer) {
        this.player1 = player1;
        this.player2 = player2;
        this.observer = observer;
        this.deck = initializeDeck();
        this.gameState = null;
        currentEngine = this;
    }

    /**
     * Initialize the main deck.
     * @return
     */
    private GameDeck initializeDeck() {
        Map<Card.Type, Integer> deckMap = new HashMap<>();
        deckMap.put(Card.Type.BITCOIN, 60);
        deckMap.put(Card.Type.ETHEREUM, 40);
        deckMap.put(Card.Type.DOGECOIN, 30);
        deckMap.put(Card.Type.METHOD, 14);
        deckMap.put(Card.Type.MODULE, 8);
        deckMap.put(Card.Type.FRAMEWORK, 8);
        return new GameDeck((ImmutableMap<Type, Integer>) deckMap);
    }

    /**
     * Players' starting hand: 
     * -- 7x Bitcoin cards
     * -- 3x Method cards
     */
    private void initializeGameState(AtgPlayer player) {
        // List<Card> startingHand = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            player.getDrawDeck().addCard(new Card(Card.Type.BITCOIN, i));
            this.deck.drawCard(Card.Type.BITCOIN); //Deduct corresponding card types from main deck
        }
        for (int i = 0; i < 3; i++) {
            player.getDrawDeck().addCard(new Card(Card.Type.METHOD, i));
            this.deck.drawCard(Card.Type.METHOD); //Deduct corresponding card types from main deck
        }
        player.getDrawDeck().shuffle();

        System.out.println("Initializaing " + player.getName() + " 's draw drck...\n");
    }

    @Override
    public List<Player.ScorePair> play() throws PlayerViolationException {
        Random rand = new Random();
        int seed = rand.nextInt(2) + 1;

        if (seed == 1) {
            System.out.println(player1.getName() + ", you got lucky this time. You get to start first!");
            System.out.println(player2.getName() + ", don't be upset. Maybe your luck will come later!\n");

            //Initialize the game 
            initializeGameState(player1);
            initializeGameState(player2);
            
            while (!isGameOver()) {
                processTurn(player1);
                if (isGameOver()) break;
                processTurn(player2);
                turnCount++;
            }
            return computeScores();
        } else {
            System.out.println(player2.getName() + ", you got lucky this time. You get to start first!");
            System.out.println(player1.getName() + ", don't be upset. Maybe your luck will come later!\n");

            //Initialize the game 
            initializeGameState(player2);
            initializeGameState(player1);
            
            while (!isGameOver()) {
                processTurn(player2);
                if (isGameOver()) break;
                processTurn(player1);
                turnCount++;
            }
            return computeScores();
        }
    }


    private void processTurn(Player player) throws PlayerViolationException {
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
            if (player.getDrawDeck().isEmpty()) { //If the draw deck is empty, move the discard deck into the draw deck
                player.getDiscardDeck().moveDeck(player.getDrawDeck());
            }

            Card drawnCard = player.getDrawDeck().drawCard();
            if (drawnCard != null) {
                startingHand.add(drawnCard);
            } else { //This will not likely happen
                System.out.println("WARNING: Player " + player.getName() + " does not have enough cards to draw a full hand!");
                break;
            }
        }

        System.out.println("DEBUG: Assigning hand to " 
        + player.getName() + ":\n" + startingHand + "\n");
 
        this.gameState = new GameState(player.getName(), new Hand(new ArrayList<>(), startingHand), 
        GameState.TurnPhase.MONEY, 0, 1, turnCount, deck); //Spendable money updates as player plays the card, not with the changes in hand in one turn & available buys is 1 currently. Will update in the future.
        
        observer.notifyEvent(gameState, new GameEvent(player.getName() + " -- " + "Turn " + turnCount + "."));

        // 2). Show players' hand: card type + card value
        System.out.println("DEBUG: Checking " + player.getName() + "'s hand...");
        System.out.println("DEBUG: Unplayed cards -> " + gameState.getCurrentPlayerHand().getUnplayedCards() + "\n");

        // 3). Show players' spendable money in this single turn
        int money = 0;
        for (int i = 0; i < 5; i++) {
            if (gameState.getCurrentPlayerHand().getUnplayedCards().get(i).getType().getCategory() == Card.Type.Category.MONEY) {
                money += gameState.getCurrentPlayerHand().getUnplayedCards().get(i).getType().getValue();
            }
        }
        System.out.println("In this turn, the maximum money value " + gameState.getCurrentPlayerName() + " can spend is: " + money);

        // 4). Show player that they can only buy up to [1] card (so far)
        System.out.println("In this turn, " + gameState.getCurrentPlayerName() + " can buy " + gameState.getAvailableBuys() + " card(s).");

        // 5). Show player the buyable cards
        System.out.println("These are the card in the main deck left.\n");
        System.out.println(this.deck);
        ArrayList<Card.Type> buyableCards = new ArrayList<>();
        for (Map.Entry<Card.Type, Integer> Entry: this.deck.getCardCounts().entrySet()) {
            if (Entry.getKey().getCost() <= money) {
                buyableCards.add(Entry.getKey());
            }
        }
        System.out.println("In this turn, " + gameState.getCurrentPlayerName() + " can AFFORD:\n" + buyableCards);

        // 6) Ask the player how much money he wants to spend on this turn
        while (gameState.getTurnPhase() == GameState.TurnPhase.MONEY) {
            List<Decision> options = new ArrayList<>();
            List<Card> updatedUnplayedCards = new ArrayList<>(gameState.getCurrentPlayerHand().getUnplayedCards());
            List<Card> playedCards = new ArrayList<>(gameState.getCurrentPlayerHand().getPlayedCards());
            
            //The player can choose to either play the available cards, or end the phase without playing any cards.
            for (Card card : updatedUnplayedCards) {
                if (card.getType().getCategory() == Card.Type.Category.MONEY) { //Player can only play money card (and action cards in the future)
                    options.add(new PlayCardDecision(card)); 
                }
            }
            options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
    
            System.out.println("DEBUG: Available decisions -> " + options.size());
            
            Decision decision = player.makeDecision(gameState, options);
            if (decision instanceof PlayCardDecision playCardDecision) {
                Card playedCard = playCardDecision.getCard();
                updatedUnplayedCards.remove(playedCard);
                playedCards.add(playedCard);
                observer.notifyEvent(gameState, new PlayCardEvent(playedCard, player.getName()));
    
                // ✅ Increase spendable money when playing a money card
                int newMoney = gameState.getSpendableMoney() + playedCard.getType().getValue();
                System.out.println("DEBUG: Updated spendable money: " + newMoney);
    
                // ✅ Update the game state with increased money
                gameState = new GameState(player.getName(), new Hand(playedCards, updatedUnplayedCards),
                                          GameState.TurnPhase.MONEY, newMoney,
                                          gameState.getAvailableBuys(), deck);
            } else if (decision instanceof EndPhaseDecision) {
                gameState = new GameState(player.getName(), gameState.getCurrentPlayerHand(),
                                          GameState.TurnPhase.BUY, gameState.getSpendableMoney(),
                                          gameState.getAvailableBuys(), deck);
            } else {
                throw new PlayerViolationException("Invalid decision in MONEY phase");
            }
        }
    }
    
    private void handleBuyPhase(Player player) throws PlayerViolationException {
        System.out.println("DEBUG: Entering BUY phase. Available money: " + gameState.getSpendableMoney());
    
        while (gameState.getTurnPhase() == GameState.TurnPhase.BUY && gameState.getAvailableBuys() >= 1) { //In this stage, without action cards, each turn only consists of 1 avaible buys. The 1 is subject to future changes.
            List<Decision> options = new ArrayList<>();
    
            // ✅ Add buyable cards based on money
            for (Card.Type type : deck.getCardTypes()) {
                if (deck.getNumAvailable(type) > 0 && gameState.getSpendableMoney() >= type.getCost()) {
                    System.out.println("DEBUG: Adding buy option for " + type + " (Cost: " + type.getCost() + ", Value: " + type.getValue() + ")");
                    options.add(new BuyDecision(type));
                }
            }
    
            options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
    
            System.out.println("DEBUG: Available decisions in BUY phase -> " + options.size());
    
            Decision decision = player.makeDecision(gameState, options);
            if (decision instanceof BuyDecision buyDecision) {
                Card.Type boughtCard = buyDecision.getCardType();
                observer.notifyEvent(gameState, new GainCardEvent(boughtCard, player.getName()));

                //Add the bought card to player's discard deck & Remove the card from the game deck
                player.getDiscardDeck().addCard(deck.drawCard(boughtCard));

                // ✅ Deduct money after buying
                int newMoney = gameState.getSpendableMoney() - boughtCard.getCost();

                System.out.println("DEBUG: Updated spendable money after buying: " + newMoney);
                System.out.println("DEBUG: Updated " + player.getName() 
                + "'s discard deck:");
                player.getDiscardDeck().printDeck();
                System.out.println("DEBUG: Updated " + player.getName() 
                + "'s hand: \n" + gameState.getCurrentPlayerHand());

                // ✅ Update game state after a purchase
                gameState = new GameState(player.getName(), gameState.getCurrentPlayerHand(),
                                          GameState.TurnPhase.BUY, newMoney,
                                          gameState.getAvailableBuys() - 1, deck);
            } else if (decision instanceof EndPhaseDecision) {
                gameState = new GameState(player.getName(), gameState.getCurrentPlayerHand(),
                                          GameState.TurnPhase.CLEANUP, gameState.getSpendableMoney(),
                                          gameState.getAvailableBuys(), deck);
            } else {
                throw new PlayerViolationException("Invalid decision in BUY phase");
            }
        }
    }
    

    private void handleCleanupPhase(Player player) {
        //Put hand into discard deck.
        List<Card> cardsToMove = new ArrayList<>();
        cardsToMove.addAll(gameState.getCurrentPlayerHand().getPlayedCards());
        cardsToMove.addAll(gameState.getCurrentPlayerHand().getUnplayedCards());
        player.getDiscardDeck().addAllCards(cardsToMove);

        gameState = new GameState(player.getName(), new Hand(new ArrayList<>(), new ArrayList<>()),
                                   GameState.TurnPhase.CLEANUP, 0, 0, deck);
        System.out.println("DEBUG: Discarding current hand...");
        System.out.println("DEBUG: Updated " + player.getName() + "'s discard deck:");
        player.getDiscardDeck().printDeck();
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

    private List<Player.ScorePair> computeScores() {
        List<Player.ScorePair> scores = new ArrayList<>();
        scores.add(new Player.ScorePair(player1, calculateScore(player1)));
        scores.add(new Player.ScorePair(player2, calculateScore(player2)));
        return scores;
    }

    private int calculateScore(Player player) {
        List<Card> discardDeck = new ArrayList<>();
        discardDeck.addAll(player.getDiscardDeck().getCards());

        List<Card> drawDeck = new ArrayList<>();
        drawDeck.addAll(player.getDrawDeck().getCards());
        
        // Caculate AP points for cards in players' hand, if any        
        List<Card> playerMainDeck = new ArrayList<>();
        playerMainDeck.addAll(discardDeck);
        playerMainDeck.addAll(drawDeck);
        if (gameState != null && gameState.getCurrentPlayerName().equals(player.getName())) {
            playerMainDeck.addAll(gameState.getCurrentPlayerHand().getPlayedCards());
            playerMainDeck.addAll(gameState.getCurrentPlayerHand().getUnplayedCards());
        }

        int score = 0;
        for (Card card: playerMainDeck) {
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