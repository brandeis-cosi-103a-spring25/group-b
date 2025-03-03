package edu.brandeis.cosi103a.groupb;

import org.junit.Test;

import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Game.GameObserver;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;
import edu.brandeis.cosi103a.groupb.Player.Player;
import edu.brandeis.cosi103a.groupb.Player.PlayerViolationException;

public class testEngine {
    private Player player1 = new BigMoneyPlayer("Nancy");
    private Player player2 = new HumanPlayer("Abby");
    private GameObserver observer = new ConsoleGameObserver();
    private GameEngine gameEngine = new GameEngine(player1, player2, observer);

    @Test
    void testInitialization() throws PlayerViolationException {
        gameEngine.play();
    }

    
}
