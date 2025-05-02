package edu.brandeis.cosi103a.groupb.Server.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.api.decisions.Decision;
import edu.brandeis.cosi103a.groupb.Player.AtgPlayer;
import edu.brandeis.cosi103a.groupb.Player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.Server.model.DecisionRequest;
import edu.brandeis.cosi103a.groupb.Server.model.DecisionResponse;
import edu.brandeis.cosi103a.groupb.Server.model.LogEventRequest;

@RestController
public class PlayerDecisionController {

    private final Map<String, AtgPlayer> players = new HashMap<>();

    @PostMapping("/decide")
    public DecisionResponse handleDecision(@RequestBody DecisionRequest request) {
        String playerUuid = request.getPlayer_uuid();

        if (playerUuid == null || playerUuid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing player UUID");
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
