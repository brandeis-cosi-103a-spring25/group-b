package edu.brandeis.cosi103a.groupb.Server.controller;

import java.util.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.*;

import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi103a.groupb.Player.*;
import edu.brandeis.cosi103a.groupb.Server.model.*;

@RestController
public class PlayerDecisionController {

    private final Map<String, AtgPlayer> players = new HashMap<>();

    @PostMapping("/decide")
    public DecisionResponse handleDecision(@RequestBody DecisionRequest request) {
        String playerUuid = request.getPlayer_uuid();

        if (playerUuid == null || playerUuid.isEmpty()) {
            playerUuid = "dxefault-uuid";
        }

        AtgPlayer player = getOrCreatePlayer(playerUuid);

        ImmutableList<Decision> immutableOptions = ImmutableList.copyOf(request.getOptions());

        var decision = player.makeDecision(request.getState(), immutableOptions, request.getReason());

        return new DecisionResponse(decision); 
    }

    @PostMapping("/log-event")
    public ResponseEntity<Void> handleLog(@RequestBody LogEventRequest request) {
        String playerUuid = request.getPlayer_uuid();

        AtgPlayer player = getOrCreatePlayer(playerUuid);

        if (request.getEvent() != null) {
            player.getObserver().ifPresent(observer -> observer.notifyEvent(request.getState(), request.getEvent()));
        }
        
        return ResponseEntity.ok().build();
    } 

    /**
     * Helper method to get or create a player instance by UUID
     */
    private AtgPlayer getOrCreatePlayer(String uuid) {
        if (players.containsKey(uuid)) {
            return players.get(uuid);
        }

        AtgPlayer player = new BigMoneyPlayer("Player-" + uuid.substring(0, Math.min(8, uuid.length())));
        players.put(uuid, player);
        return player;
    }

    
}
