package edu.brandeis.cosi.atg.api;

public class PlayerViolationException extends Exception {
    public PlayerViolationException(String message) {
        super(message);
    }

    public PlayerViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlayerViolationException(Throwable cause) {
        super(cause);
    }
}