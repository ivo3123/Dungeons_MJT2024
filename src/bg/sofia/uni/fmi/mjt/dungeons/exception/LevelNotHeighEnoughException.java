package bg.sofia.uni.fmi.mjt.dungeons.exception;

public class LevelNotHeighEnoughException extends Exception {
    public LevelNotHeighEnoughException(String message) {
        super(message);
    }

    public LevelNotHeighEnoughException(String message, Throwable cause) {
        super(message, cause);
    }
}
