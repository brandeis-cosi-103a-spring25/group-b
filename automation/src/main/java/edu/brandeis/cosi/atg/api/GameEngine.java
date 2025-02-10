package edu.brandeis.cosi.atg.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.api.cards.Card;
import edu.brandeis.cosi.atg.api.decisions.*;
import edu.brandeis.cosi.atg.api.event.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngine implements Engine {
    private final List<Player> players;
    private final GameObserver observer;
    private final GameDeck gameDeck;

    public GameEngine(List<Player> players, GameObserver observer) {
        this.players = players;
        this.observer = observer;
        this.gameDeck = initializeGameDeck();
        dealStartingHands();
    }

    private GameDeck initializeGameDeck() {
        ImmutableMap<Card.Type, Integer> cardCounts = ImmutableMap.<Card.Type, Integer>builder()
                .put(Card.Type.BITCOIN, 60)
                .put(Card.Type.ETHEREUM, 40)
                .put(Card.Type.DOGECOIN, 30)
                .put(Card.Type.METHOD, 14)
                .put(Card.Type.MODULE, 8)
                .put(Card.Type.FRAMEWORK, 8)
                .build();
        return new GameDeck(cardCounts);
    }

    private void dealStartingHands() {
        for (Player player : players) {
            for (int i = 0; i < 7; i++) {
                player.addCardToDeck(new Card("Bitcoin", 0, Card.Category.CRYPTOCURRENCY));
            }
            for (int i = 0; i < 3; i++) {
                player.addCardToDeck(new Card("Method", 0, Card.Category.AUTOMATION));
            }
            player.drawHand();
        }
    }

    @Override
    public ImmutableList<Player.ScorePair> play() throws PlayerViolationException {
        while (!isGameOver()) {
            for (Player player : players) {
                takeTurn(player);
            }
        }
        return determineWinner();
    }

    private void takeTurn(Player player) throws PlayerViolationException {
        // MONEY phase
        GameState gameState = new GameState(player.getName(), new Hand(player.getHand(), ImmutableList.of()), GameState.TurnPhase.MONEY, player.getAvailableCoins(), 1, gameDeck);
        while (true) {
            Decision decision = player.makeDecision(gameState, getMoneyPhaseDecisions(player));
            if (decision instanceof EndPhaseDecision) {
                break;
            } else if (decision instanceof PlayCardDecision) {
                Card card = ((PlayCardDecision) decision).getCard();
                player.getHand().remove(card);
                logEvent(new PlayCardEvent(card, player.getName()));
            } else {
                throw new PlayerViolationException("Invalid decision during MONEY phase");
            }
        }

        // BUY phase
        gameState = new GameState(player.getName(), new Hand(player.getHand(), ImmutableList.of()), GameState.TurnPhase.BUY, player.getAvailableCoins(), 1, gameDeck);
        while (true) {
            Decision decision = player.makeDecision(gameState, getBuyPhaseDecisions(player));
            if (decision instanceof EndPhaseDecision) {
                break;
            } else if (decision instanceof BuyDecision) {
                Card.Type cardType = ((BuyDecision) decision).getCardType();
                if (gameDeck.getNumAvailable(cardType) > 0) {
                    player.getDeck().add(new Card(cardType, 0)); // Simplified card creation
                    logEvent(new GainCardEvent(cardType, player.getName()));
                } else {
                    throw new PlayerViolationException("Card not available for purchase");
                }
            } else {
                throw new PlayerViolationException("Invalid decision during BUY phase");
            }
        }

        // CLEANUP phase
        player.discardHand();
        player.drawHand();
        logEvent(new EndTurnEvent(player));
    }

    private ImmutableList<Decision> getMoneyPhaseDecisions(Player player) {
        List<Decision> decisions = new ArrayList<>();
        for (Card card : player.getHand()) {
            decisions.add(new PlayCardDecision(card));
        }
        decisions.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        return ImmutableList.copyOf(decisions);
    }

    private ImmutableList<Decision> getBuyPhaseDecisions(Player player) {
        List<Decision> decisions = new ArrayList<>();
        for (Card.Type cardType : gameDeck.getCardTypes()) {
            if (gameDeck.getNumAvailable(cardType) > 0) {
                decisions.add(new BuyDecision(cardType));
            }
        }
        decisions.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
        return ImmutableList.copyOf(decisions);
    }

    private boolean isGameOver() {
        return gameDeck.getNumAvailable(Card.Type.FRAMEWORK) == 0;
    }

    private ImmutableList<Player.ScorePair> determineWinner() {
        List<Player.ScorePair> scores = new ArrayList<>();
        for (Player player : players) {
            int score = player.getDeck().stream()
                    .filter(card -> card.getCategory() == Card.Type.Category.VICTORY)
                    .mapToInt(Card::getCost)
                    .sum();
            scores.add(new Player.ScorePair(player, score));
        }
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return ImmutableList.copyOf(scores);
    }

    private void logEvent(Event event) {
        if (observer != null) {
            observer.notifyEvent(null, event); // Assuming null for GameState, replace with actual state if available
        }
        for (Player player : players) {
            player.getObserver().ifPresent(o -> o.notifyEvent(null, event)); // Assuming null for GameState, replace with actual state if available
        }
    }
}