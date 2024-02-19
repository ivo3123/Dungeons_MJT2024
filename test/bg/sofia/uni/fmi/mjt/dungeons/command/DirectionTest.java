package bg.sofia.uni.fmi.mjt.dungeons.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DirectionTest {
    @Test
    void testIsValid() {
        assertTrue(Direction.isValid("down"));
        assertTrue(Direction.isValid("up"));
        assertTrue(Direction.isValid("left"));
        assertTrue(Direction.isValid("right"));
        assertFalse(Direction.isValid("any"));
    }
}
