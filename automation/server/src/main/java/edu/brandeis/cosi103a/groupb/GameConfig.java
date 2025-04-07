package edu.brandeis.cosi103a.groupb;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.databind.Module;

import edu.brandeis.cosi.atg.api.GameDeck;
import edu.brandeis.cosi.atg.api.GameObserver;
import edu.brandeis.cosi.atg.api.Player;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;
import edu.brandeis.cosi103a.groupb.Player.*;


/* Defines Player + GameEngine Beans */
@Configuration
public class GameConfig {
    /**
    * This class lets Spring know that it can create a new instance of the game engine
    * and inject it into any class that needs it.
    */
    @Bean
    public Player humanPlayer() {
        return new HumanPlayer("Network Player");
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
