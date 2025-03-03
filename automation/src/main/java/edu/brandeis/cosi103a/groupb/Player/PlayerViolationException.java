package edu.brandeis.cosi103a.groupb.Player;

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