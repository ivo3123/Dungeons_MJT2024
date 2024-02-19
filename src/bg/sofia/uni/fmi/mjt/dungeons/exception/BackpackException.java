package bg.sofia.uni.fmi.mjt.dungeons.exception;

public class BackpackException extends InventoryException {
    public BackpackException(String message) {
        super(message);
    }

    public BackpackException(String message, Throwable cause) {
        super(message, cause);
    }
}
