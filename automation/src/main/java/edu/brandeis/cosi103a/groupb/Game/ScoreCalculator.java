package edu.brandeis.cosi103a.groupb.Game;

import edu.brandeis.cosi103a.groupb.Cards.Card;
import edu.brandeis.cosi103a.groupb.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreCalculator {
    private final GameState gameState;

    public ScoreCalculator(GameState gameState) {
        this.gameState = gameState;
    }

    public List<Player.ScorePair> computeScores(Player player1, Player player2) {
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

        List<Card> playerMainDeck = new ArrayList<>();
        playerMainDeck.addAll(discardDeck);
        playerMainDeck.addAll(drawDeck);
        if (gameState != null && gameState.getCurrentPlayerName().equals(player.getName())) {
            playerMainDeck.addAll(gameState.getCurrentPlayerHand().getPlayedCards());
            playerMainDeck.addAll(gameState.getCurrentPlayerHand().getUnplayedCards());
        }

        int score = 0;
        for (Card card : playerMainDeck) {
            if (card.getType().getCategory() == Card.Type.Category.VICTORY) {
                score += card.getType().getValue();
            }
        }
        return score;
    }
}