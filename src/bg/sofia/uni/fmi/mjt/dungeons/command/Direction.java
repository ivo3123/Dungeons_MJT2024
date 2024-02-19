package bg.sofia.uni.fmi.mjt.dungeons.command;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    private static Set<String> directions;

    static {
        directions = Arrays.stream(Direction.values())
            .map(direction -> direction.name())
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    }

    public static boolean isValid(String input) {
        return directions.contains(input);
    }

    public static Direction getDirection(String string) {
        try {
            return Direction.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid direction: " + string);
        }
    }
}
