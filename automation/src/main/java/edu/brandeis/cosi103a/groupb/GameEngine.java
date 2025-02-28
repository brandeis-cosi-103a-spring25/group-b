package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.brandeis.cosi103a.groupb.Cards.Card;
import edu.brandeis.cosi103a.groupb.Decisions.BuyDecision;
import edu.brandeis.cosi103a.groupb.Decisions.Decision;
import edu.brandeis.cosi103a.groupb.Decisions.EndPhaseDecision;
import edu.brandeis.cosi103a.groupb.Decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupb.Events.GainCardEvent;
import edu.brandeis.cosi103a.groupb.Events.GameEvent;
import edu.brandeis.cosi103a.groupb.Events.PlayCardEvent;

public class GameEngine implements Engine {
    private final GameDeck deck;
    private final Player player1;
    private final Player player2;
    private final GameObserver observer;
    private GameState gameState;

    public GameEngine(Player player1, Player player2, GameObserver observer) {
        this.player1 = player1;
        this.player2 = player2;
        this.observer = observer;
        this.deck = initializeDeck();
        this.gameState = initializeGameState();
    }

    @Override
    public List<Player.ScorePair> play() throws PlayerViolationException {
        while (!isGameOver()) {
            processTurn(player1);
            if (isGameOver()) break;
            processTurn(player2);
        }
        return computeScores();
    }

    private void processTurn(Player player) throws PlayerViolationException {
        gameState = new GameState(player.getName(), gameState.getCurrentPlayerHand(),
                                  GameState.TurnPhase.MONEY, gameState.getSpendableMoney(),
                                  gameState.getAvailableBuys(), deck);
        observer.notifyEvent(gameState, new GameEvent(player.getName() + "'s turn begins"));

        handleMoneyPhase(player);
        handleBuyPhase(player);
        handleCleanupPhase(player);
    }

    private void handleMoneyPhase(Player player) throws PlayerViolationException {
        System.out.println("DEBUG: Checking " + player.getName() + "'s hand...");
        System.out.println("DEBUG: Unplayed cards -> " + gameState.getCurrentPlayerHand().getUnplayedCards());
    
        while (gameState.getTurnPhase() == GameState.TurnPhase.MONEY) {
            List<Decision> options = new ArrayList<>();
            List<Card> updatedUnplayedCards = new ArrayList<>(gameState.getCurrentPlayerHand().getUnplayedCards());
            List<Card> playedCards = new ArrayList<>(gameState.getCurrentPlayerHand().getPlayedCards());
    
            for (Card card : updatedUnplayedCards) {
                options.add(new PlayCardDecision(card));
            }
            options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
    
            System.out.println("DEBUG: Available decisions -> " + options.size());
    
            Decision decision = player.makeDecision(gameState, options);
            if (decision instanceof PlayCardDecision) {
                Card playedCard = ((PlayCardDecision) decision).getCard();
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
    
        while (gameState.getTurnPhase() == GameState.TurnPhase.BUY) {
            List<Decision> options = new ArrayList<>();
    
            // ✅ Add buyable cards based on money
            for (Card.Type type : deck.getCardTypes()) {
                if (deck.getNumAvailable(type) > 0 && gameState.getSpendableMoney() >= type.getValue()) {
                    System.out.println("DEBUG: Adding buy option for " + type + " (Cost: " + type.getValue() + ")");
                    options.add(new BuyDecision(type));
                }
            }
    
            options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
    
            System.out.println("DEBUG: Available decisions in BUY phase -> " + options.size());
    
            Decision decision = player.makeDecision(gameState, options);
            if (decision instanceof BuyDecision) {
                Card.Type boughtCard = ((BuyDecision) decision).getCardType();
                observer.notifyEvent(gameState, new GainCardEvent(boughtCard, player.getName()));
    
                // ✅ Deduct money after buying
                int newMoney = gameState.getSpendableMoney() - boughtCard.getValue();
                System.out.println("DEBUG: Updated spendable money after buying: " + newMoney);
    
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
        observer.notifyEvent(gameState, new GameEvent(player.getName() + "'s turn ends"));
    
        // ✅ Draw 5 new cards from the deck for the next turn
        List<Card> newHand = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Card drawnCard = deck.drawCard(Card.Type.BITCOIN); // Currently using BITCOIN for simplicity
            if (drawnCard != null) {
                newHand.add(drawnCard);
            }
        }
    
        System.out.println("DEBUG: Drawing new hand for " + player.getName() + ": " + newHand.size() + " cards");
    
        // ✅ Reset the game state with new cards
        gameState = new GameState(player.getName(), new Hand(new ArrayList<>(), newHand),
                                  GameState.TurnPhase.MONEY, 0, 1, deck);
    }

    private boolean isGameOver() {
        return deck.getNumAvailable(Card.Type.FRAMEWORK) == 0;
    }

    private List<Player.ScorePair> computeScores() {
        List<Player.ScorePair> scores = new ArrayList<>();
        scores.add(new Player.ScorePair(player1, calculateScore(player1)));
        scores.add(new Player.ScorePair(player2, calculateScore(player2)));
        return scores;
    }

    private int calculateScore(Player player) {
        int score = 0;
        for (Card.Type type : deck.getCardTypes()) {
            if (type.getCategory() == Card.Type.Category.VICTORY) {
                score += deck.getNumAvailable(type) * type.getValue();
            }
        }
        return score;
    }

    private GameDeck initializeDeck() {
        Map<Card.Type, Integer> deckMap = new HashMap<>();
        deckMap.put(Card.Type.BITCOIN, 60);
        deckMap.put(Card.Type.ETHEREUM, 40);
        deckMap.put(Card.Type.DOGECOIN, 30);
        deckMap.put(Card.Type.METHOD, 14);
        deckMap.put(Card.Type.MODULE, 8);
        deckMap.put(Card.Type.FRAMEWORK, 8);
        return new GameDeck(deckMap);
    }

    private GameState initializeGameState() {
        List<Card> startingHand = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            startingHand.add(new Card(Card.Type.BITCOIN, i));
        }
    
        System.out.println("DEBUG: Assigning starting hand to player: " + startingHand);
    
        return new GameState(player1.getName(), new Hand(new ArrayList<>(), startingHand), 
                             GameState.TurnPhase.MONEY, 0, 1, deck);
    }
    
    
}