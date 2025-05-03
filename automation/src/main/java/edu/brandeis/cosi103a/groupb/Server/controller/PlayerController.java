package edu.brandeis.cosi103a.groupb.Server.controller;

import org.springframework.web.bind.annotation.RestController;

import edu.brandeis.cosi103a.groupb.Server.model.PlayerResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private static final List<String> PLAYER_TYPES = List.of("bigmoney", "redeye","finalboss");
    
    @GetMapping
    public List<PlayerResponse> getPlayerTypes() {
        return PLAYER_TYPES.stream()
            .map(PlayerResponse::new)
            .collect(Collectors.toList());
    }
}