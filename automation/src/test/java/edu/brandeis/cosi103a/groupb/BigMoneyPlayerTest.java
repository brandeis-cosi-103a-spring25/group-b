package edu.brandeis.cosi103a.groupb;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Decisions.Decision;
import edu.brandeis.cosi103a.groupb.Decisions.EndPhaseDecision;
import edu.brandeis.cosi103a.groupb.Decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupb.Decisions.BuyDecision;
import edu.brandeis.cosi103a.groupb.Game.GameState;
import edu.brandeis.cosi103a.groupb.Decks.GameDeck;
import edu.brandeis.cosi103a.groupb.Decks.Hand;
import edu.brandeis.cosi103a.groupb.Cards.Card;

public class BigMoneyPlayerTest {

    @Test
    public void testMakeDecisionWithMoneyPhase() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        // Create dummy GameState for MONEY phase.
        Hand dummyHand = new Hand(new ArrayList<>(), new ArrayList<>());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 0, 1, new GameDeck(new HashMap<>()));

        // Provide a PlayCardDecision option and an EndPhaseDecision.
        Card dummyCard = new Card(Card.Type.BITCOIN, 1);
        List<Decision> options = new ArrayList<>();
        options.add(new PlayCardDecision(dummyCard));
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        Decision decision = player.makeDecision(state, options);

        assertTrue(decision instanceof PlayCardDecision, "Expected decision to be PlayCardDecision in MONEY phase.");
    }
    
    @Test
    public void testMakeDecisionWithBuyPhase() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        // Create dummy GameState for BUY phase with enough money.
        Hand dummyHand = new Hand(new ArrayList<>(), new ArrayList<>());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.BUY, 5, 1, new GameDeck(new HashMap<>()));

        // Provide BuyDecision options (one cheap and one expensive) and an EndPhaseDecision.
        List<Decision> options = new ArrayList<>();
        options.add(new BuyDecision(Card.Type.BITCOIN));
        options.add(new BuyDecision(Card.Type.FRAMEWORK)); // Assume FRAMEWORK has a higher cost.
        options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));

        Decision decision = player.makeDecision(state, options);

        assertTrue(decision instanceof BuyDecision, "Expected decision to be BuyDecision.");
        BuyDecision buyDecision = (BuyDecision) decision;
        assertEquals(Card.Type.FRAMEWORK, buyDecision.getCardType(), "Expected to choose FRAMEWORK card.");
    }
    
    // Additional test: Only EndPhaseDecision exists in MONEY phase
    @Test
    public void testMakeDecisionWithMoneyPhaseOnlyEndPhase() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        Hand dummyHand = new Hand(new ArrayList<>(), new ArrayList<>());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 0, 1, new GameDeck(new HashMap<>()));
        
        // Only provide an EndPhaseDecision.
        List<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        Decision decision = player.makeDecision(state, options);
        
        assertTrue(decision instanceof EndPhaseDecision, "Expected decision to be EndPhaseDecision when no PlayCardDecision exists in MONEY phase.");
    }
    
    // Additional test: Only EndPhaseDecision exists in BUY phase (simulate insufficient options).
    @Test
    public void testMakeDecisionWithBuyPhaseOnlyEndPhase() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        Hand dummyHand = new Hand(new ArrayList<>(), new ArrayList<>());
        // Create dummy GameState for BUY phase.
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.BUY, 5, 1, new GameDeck(new HashMap<>()));
        
        // Only provide an EndPhaseDecision.
        List<Decision> options = new ArrayList<>();
        options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
        
        Decision decision = player.makeDecision(state, options);
        
        assertTrue(decision instanceof EndPhaseDecision, "Expected decision to be EndPhaseDecision when no BuyDecision exists in BUY phase.");
    }
    
    // Additional test: In MONEY phase, when multiple PlayCardDecision options exist, the first one is chosen.
    @Test
    public void testMakeDecisionWithMultiplePlayCardDecisions() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        Hand dummyHand = new Hand(new ArrayList<>(), new ArrayList<>());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.MONEY, 0, 1, new GameDeck(new HashMap<>()));

        // Create two PlayCardDecision options.
        Card firstCard = new Card(Card.Type.BITCOIN, 1);
        Card secondCard = new Card(Card.Type.ETHEREUM, 2);
        List<Decision> options = new ArrayList<>();
        options.add(new PlayCardDecision(firstCard));
        options.add(new PlayCardDecision(secondCard));
        options.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        
        Decision decision = player.makeDecision(state, options);
        
        // Expect the first PlayCardDecision to be returned.
        assertTrue(decision instanceof PlayCardDecision, "Expected decision to be PlayCardDecision when multiple are provided.");
        PlayCardDecision playDecision = (PlayCardDecision) decision;
        assertEquals(firstCard, playDecision.getCard(), "Expected the first PlayCardDecision option to be chosen.");
    }

    @Test
    public void testAdvancedStrategySkipsFramework() {
        BigMoneyPlayer player = new BigMoneyPlayer("BigMoney");
        
        // Create a deck where only 1 Framework is available and several ETHEREUM cards are available.
        Map<Card.Type, Integer> deckMap = new HashMap<>();
        deckMap.put(Card.Type.FRAMEWORK, 1);  // Last framework card
        deckMap.put(Card.Type.ETHEREUM, 5);   // Alternative purchase option
        GameDeck deck = new GameDeck(deckMap);
        
        // Create dummy hand and game state for BUY phase with enough money.
        Hand dummyHand = new Hand(new ArrayList<>(), new ArrayList<>());
        GameState state = new GameState(player.getName(), dummyHand, GameState.TurnPhase.BUY, 5, 1, deck);
        
        // Provide BuyDecision options: one for FRAMEWORK and one for ETHEREUM, plus an EndPhaseDecision.
        List<Decision> options = new ArrayList<>();
        options.add(new BuyDecision(Card.Type.FRAMEWORK));
        options.add(new BuyDecision(Card.Type.ETHEREUM));
        options.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
        
        // Since isWinning(state) is stubbed to return false (player not winning) and only one FRAMEWORK remains,
        // the advanced strategy should skip the framework option and choose the ETHEREUM card.
        Decision decision = player.makeDecision(state, options);
        
        assertTrue(decision instanceof BuyDecision, "Expected decision to be BuyDecision.");
        BuyDecision buyDecision = (BuyDecision) decision;
        assertEquals(Card.Type.ETHEREUM, buyDecision.getCardType(), "Expected to choose ETHEREUM over FRAMEWORK due to advanced strategy.");
    }
}