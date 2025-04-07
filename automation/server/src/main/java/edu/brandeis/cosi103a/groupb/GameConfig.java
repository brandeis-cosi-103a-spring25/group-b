package edu.brandeis.cosi103a.groupb;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.databind.Module;

import edu.brandeis.cosi.atg.api.GameDeck;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;

@Configuration
public class GameConfig {
    /**
    * This class lets Spring know that it can create a new instance of the game engine
    * and inject it into any class that needs it.
    */
    @Bean
    public GameEngine gameEngine(AtgPlayer player1, AtgPlayer player2, GameObserver observer, GameDeck deck) {
        return new GameEngine(player1, player2, observer, deck);
    }

    /**
    * This registers the Guava module with the Jackson JSON mapper.
    * This allows Jackson to serialize and deserialize Guava types,
    * such as ImmutableList.
    */
    @Bean
    public Module guavaModule() {
        return new GuavaModule();
    }
    
}
