package edu.brandeis.cosi103a.groupb;
import java.util.*;

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GameTest {

    @Test
    public void testPlayer() {
        Player player = new Player();
        Card card = new CryptocurrencyCard("Bitcoin", 0, 1);
        player.addCardToDeck(card);

        assertEquals(card, player.getDeck().get(0));
    }

    @Test
    public void testCard() {
        Player player = new Player();
        Card card = new CryptocurrencyCard("Bitcoin", 0, 1);
        Card card2 = new CryptocurrencyCard("Ethereum", 1, 2);
        player.addCardToDeck(card);
        player.addCardToDeck(card2);

        assertEquals(card2, player.getDeck().get(1));
    } 

}
