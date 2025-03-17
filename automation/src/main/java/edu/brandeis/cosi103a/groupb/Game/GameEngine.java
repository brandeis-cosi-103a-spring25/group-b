package edu.brandeis.cosi103a.groupb.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.cards.Card.Type;
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
    private int turnCount = 1;

    private static GameEngine currentEngine;

    public GameEngine(AtgPlayer player1, AtgPlayer player2, GameObserver observer) {
        this.player1 = player1;
        this.player2 = player2;
        this.observer = observer;
        this.deck = initializeDeck();
        this.gameState = null;
        currentEngine = this;
    }
    

    /**
     * Initialize the main deck.
     */
    private GameDeck initializeDeck() {
        Map<Card.Type, Integer> deckMap = new HashMap<>();
        deckMap.put(Card.Type.BITCOIN, 60);
        deckMap.put(Card.Type.ETHEREUM, 40);
        deckMap.put(Card.Type.DOGECOIN, 30);
        deckMap.put(Card.Type.METHOD, 14);
        deckMap.put(Card.Type.MODULE, 8);
        deckMap.put(Card.Type.FRAMEWORK, 8);
        return new GameDeck(ImmutableMap.copyOf(deckMap));
    }

    /**
     * Initialize player decks with starting hands.
     */
    private void initializeGameState(AtgPlayer player) {
        for (int i = 0; i < 7; i++) {
            player.getDrawDeck().addCard(new Card(Card.Type.BITCOIN, i));
        }
        for (int i = 0; i < 3; i++) {
            player.getDrawDeck().addCard(new Card(Card.Type.METHOD, i));
        }
        player.getDrawDeck().shuffle();
    }

    @Override
    public ImmutableList<Player.ScorePair> play() throws PlayerViolationException {
        Random rand = new Random();
        int seed = rand.nextInt(2) + 1;

        if (seed == 1) {
            initializeGameState((AtgPlayer) player1);
            initializeGameState((AtgPlayer) player2);
        } else {
            initializeGameState((AtgPlayer) player2);
            initializeGameState((AtgPlayer) player1);
        }

        while (!isGameOver()) {
            processTurn((AtgPlayer)player1);
            if (isGameOver()) break;
            processTurn((AtgPlayer)player2);
            turnCount++;
        }
        return computeScores();
    }

    private void processTurn(AtgPlayer player) throws PlayerViolationException {
        handleMoneyPhase( player);
        handleBuyPhase(player);
        handleCleanupPhase(player);
    }

    private void handleMoneyPhase(AtgPlayer player) throws PlayerViolationException {
        List<Card> startingHand = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (player.getDrawDeck().isEmpty()) {
                player.getDiscardDeck().moveDeck(player.getDrawDeck());
            }

            Card drawnCard = player.getDrawDeck().drawCard();
            if (drawnCard != null) {
                startingHand.add(drawnCard);
            }
        }

        gameState = new GameState(player.getName(), new Hand(ImmutableList.of(), ImmutableList.copyOf(startingHand)), 
                                  GameState.TurnPhase.MONEY, 1, 0, 1, deck);
        
        observer.notifyEvent(gameState, new GameEvent(player.getName() + " -- Turn " + turnCount));

        while (gameState.getTurnPhase() == GameState.TurnPhase.MONEY) {
            List<Decision> options = new ArrayList<>();
            for (Card card : startingHand) {
                if (card.getType().getCategory() == Card.Type.Category.MONEY) {
                    options.add(new PlayCardDecision(card));
                }
            }
            options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));

            Decision decision = player.makeDecision(gameState, ImmutableList.copyOf(options), null);
            if (decision instanceof PlayCardDecision playCardDecision) {
                startingHand.remove(playCardDecision.getCard());
                observer.notifyEvent(gameState, new PlayCardEvent(playCardDecision.getCard(), player.getName()));

                int newMoney = gameState.getSpendableMoney() + playCardDecision.getCard().getType().getValue();
                gameState = new GameState(player.getName(), new Hand(ImmutableList.copyOf(startingHand), ImmutableList.of()),
                                          GameState.TurnPhase.MONEY, 1, newMoney, 1, deck);
            } else if (decision instanceof EndPhaseDecision) {
                gameState = new GameState(player.getName(), gameState.getCurrentPlayerHand(),
                                          GameState.TurnPhase.BUY, 1, gameState.getSpendableMoney(), 1, deck);
            }
        }
    }

    private void handleBuyPhase(AtgPlayer player) throws PlayerViolationException {
        while (gameState.getTurnPhase() == GameState.TurnPhase.BUY) {
            List<Decision> options = new ArrayList<>();
            for (Card.Type type : deck.getCardTypes()) {
                if (deck.getNumAvailable(type) > 0 && gameState.getSpendableMoney() >= type.getCost()) {
                    options.add(new BuyDecision(type));
                }
            }
            options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));

            Decision decision = player.makeDecision(gameState, ImmutableList.copyOf(options), null);
            if (decision instanceof BuyDecision buyDecision) {
                observer.notifyEvent(gameState, new GainCardEvent(buyDecision.getCardType(), player.getName()));
                player.getDrawDeck().addCard(new Card(buyDecision.getCardType(), 0)); // Buy goes to DrawDeck
            } else if (decision instanceof EndPhaseDecision) {
                gameState = new GameState(player.getName(), gameState.getCurrentPlayerHand(),
                                          GameState.TurnPhase.CLEANUP, 1, gameState.getSpendableMoney(), 1, deck);
            }
        }
    }

    private void handleCleanupPhase(AtgPlayer player) {
        List<Card> cardsToMove = new ArrayList<>();
        cardsToMove.addAll(gameState.getCurrentPlayerHand().getPlayedCards());
        cardsToMove.addAll(gameState.getCurrentPlayerHand().getUnplayedCards());
        player.getDiscardDeck().addAllCards(cardsToMove);

        gameState = new GameState(player.getName(), new Hand(ImmutableList.of(), ImmutableList.of()),
                                   GameState.TurnPhase.CLEANUP, 1, 0,0, deck);
        observer.notifyEvent(gameState, new GameEvent(player.getName() + "'s turn ends"));
    }

    private boolean isGameOver() {
        return deck.getNumAvailable(Card.Type.FRAMEWORK) == 0;
    }

    private ImmutableList<Player.ScorePair> computeScores() {
        return ImmutableList.of(new Player.ScorePair(player1, calculateScore(player1)),
                                new Player.ScorePair(player2, calculateScore(player2)));
    }

    private int calculateScore(AtgPlayer player) {
        return player.getDiscardDeck().size() + player.getDrawDeck().size();
    }
}
