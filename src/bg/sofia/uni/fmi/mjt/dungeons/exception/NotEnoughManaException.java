package bg.sofia.uni.fmi.mjt.dungeons.exception;

public class NotEnoughManaException extends Exception {
    public NotEnoughManaException(String message) {
        super(message);
    }

    public NotEnoughManaException(String message, Throwable cause) {
        super(message, cause);
    }
}
