package edu.brandeis.cosi103a.groupb.Server.handler;

import com.sun.net.httpserver.*;
import java.io.*;

import edu.brandeis.cosi103a.groupb.Server.model.PlayerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerHandler extends BaseHandler {
    private static final List<String> PLAYER_TYPES = List.of("bigmoney", "reyeye", "finalboss");
    
    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        // Return available player types
        List<PlayerResponse> playerTypes = new ArrayList<>();
        for (String type : PLAYER_TYPES) {
            playerTypes.add(new PlayerResponse(type));
        }
        sendResponse(exchange, 200, gson.toJson(playerTypes));
    }
}