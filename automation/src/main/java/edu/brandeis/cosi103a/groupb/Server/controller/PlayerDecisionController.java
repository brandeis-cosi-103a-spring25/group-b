package edu.brandeis.cosi103a.groupb.Server.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.RequestEntity;

import edu.brandeis.cosi103a.groupb.Server.model.DecisionRequest;
import edu.brandeis.cosi103a.groupb.Server.model.DecisionResponse;
import edu.brandeis.cosi103a.groupb.Server.model.LogEventRequest;

@RestController
@RequestMapping("/api/player-server")
public class PlayerDecisionController {

    @PostMapping("/decide")
    public DecisionResponse handleDecision(@RequestBody DecisionRequest request) {
        
    }

    @PostMapping("/log")
    public RequestEntity<Void> handleLog(@RequestBody LogEventRequest request) {

    } 

    
}
