package edu.brandeis.cosi103a.groupb;

import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.brandeis.cosi103a.groupb.Player.Player;
import edu.brandeis.cosi103a.groupb.Player.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.Player.HumanPlayer;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Game.ConsoleGameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameState;
import edu.brandeis.cosi103a.groupb.Game.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Decisions.*;
import edu.brandeis.cosi103a.groupb.Cards.*;
import edu.brandeis.cosi103a.groupb.Events.*;

public class BigMoneyPlayerTest {

    private Player bigMoneyPlayer;
    private GameObserver observer;
    private GameState gameState;
    private List<Decision> options;
    private GameEngine gameEngine;

    @Test
    void TestBigMoneyPlayerBuy() {
        BuyDecision mockDecision = new BuyDecision(Card.Type.BITCOIN);
        Card boughtCard = new Card(Card.Type.FRAMEWORK, 10086);

        GameEngine mockEngine = Mockito.mock(GameEngine.class);
        

    }
    
}
