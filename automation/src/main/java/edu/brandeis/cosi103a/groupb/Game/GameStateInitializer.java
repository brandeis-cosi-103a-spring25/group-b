package edu.brandeis.cosi103a.groupb.Game;

import edu.brandeis.cosi103a.groupb.Cards.Card;
import edu.brandeis.cosi103a.groupb.Player.Player;
import edu.brandeis.cosi103a.groupb.Decks.GameDeck;

public class GameStateInitializer {
    public void initializeGameState(Player player, GameDeck deck) {
        for (int i = 0; i < 7; i++) {
            player.getDrawDeck().addCard(new Card(Card.Type.BITCOIN, i));
            deck.drawCard(Card.Type.BITCOIN); // Deduct corresponding card types from main deck
        }
        for (int i = 0; i < 3; i++) {
            player.getDrawDeck().addCard(new Card(Card.Type.METHOD, i));
            deck.drawCard(Card.Type.METHOD); // Deduct corresponding card types from main deck
        }
        player.getDrawDeck().shuffle();
    }
}