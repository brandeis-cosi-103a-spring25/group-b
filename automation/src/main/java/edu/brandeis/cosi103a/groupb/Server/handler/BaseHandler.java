package edu.brandeis.cosi103a.groupb.Server.handler;

import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.*;
import com.google.gson.*;

public abstract class BaseHandler implements HttpHandler {
    protected static final Gson gson = new Gson();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    protected String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
    
    protected void handleGet(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "{\"error\": \"Method not implemented\"}");
    }
    
    protected void handlePost(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "{\"error\": \"Method not implemented\"}");
    }
    
    protected void handlePut(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "{\"error\": \"Method not implemented\"}");
    }
    
    protected void handleDelete(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "{\"error\": \"Method not implemented\"}");
    }
}