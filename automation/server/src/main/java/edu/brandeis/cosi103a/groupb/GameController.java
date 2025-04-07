package edu.brandeis.cosi103a.groupb;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi103a.groupb.Player.*;
import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi103a.groupb.Game.GameEngine;

@RestController
public class GameController {
    
    private final AtgPlayer player;

    public GameController(AtgPlayer player) {
        this.player = player;
    }

    // Handles decisions
    @PostMapping("/decide") 
    public Decision handleDecision(@RequestBody Decision request) {
        Decision decision = player.makeDecision(request.getGameState(), request.options(), request.reason());
    }


}
