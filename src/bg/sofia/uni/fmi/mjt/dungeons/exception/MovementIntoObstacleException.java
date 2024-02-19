package bg.sofia.uni.fmi.mjt.dungeons.exception;

public class MovementIntoObstacleException extends Exception {
    public MovementIntoObstacleException(String message) {
        super(message);
    }

    public MovementIntoObstacleException(String message, Throwable cause) {
        super(message, cause);
    }
}
