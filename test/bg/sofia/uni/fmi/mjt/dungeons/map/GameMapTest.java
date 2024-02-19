package bg.sofia.uni.fmi.mjt.dungeons.map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class GameMapTest {
    private static Reader READER = new StringReader(
        """
        _M_
        T_T
        MT_
        __T
        """
    );
    private static GameMap MAP = new GameMap(READER, 4, 3);

    @Test
    void testGetRandomPosition() {
        Position pos = MAP.getRandomFreePosition();

        assertNotNull(pos, "Random position shouldn't be null");
        assertTrue(MAP.isFree(pos), "Random free position should be free");
        assertEquals('_', MAP.getCell(pos), "Free position should be '_'");
    }

    @Test
    void testIsFreeWhenFree() {
        assertTrue(MAP.isFree(new Position(0, 0)), "'_' should be considered free");
        assertTrue(MAP.isFree(new Position(3, 1)), "'_' should be considered free");
    }

    @Test
    void testIsFreeWhenNotFree() {
        assertFalse(MAP.isFree(new Position(0, 1)), "'M' should not be considered free");
        assertFalse(MAP.isFree(new Position(2, 1)), "'T' should not be considered free");
    }

    @Test
    void testGetRandomPositionWhenNoPositionsAreFree() {
        Reader reader = new StringReader(
            """
            M2
            TT
            """
        );
        GameMap map = new GameMap(reader, 2, 2);

        assertThrows(
            NoSuchElementException.class,
            () -> map.getRandomFreePosition(),
            "Should throw when there are no free spots on the map"
        );
    }

    @Test
    void testGetCellAfterMovementWithWrongDirection() {
        assertThrows(
            IllegalArgumentException.class,
            () -> MAP.getCellAfterMovement(null, "invalid"),
            "Should throw when invalid direction is provided"
        );
    }

    @Test
    void testGetCellAfterMovementWithDirectionUp() {
        assertEquals(
            'M',
            MAP.getCellAfterMovement(new Position(1, 1), "up"),
            "Moving up should move correctly"
        );
    }

    @Test
    void testGetCellAfterMovementWithDirectionDown() {
        assertEquals(
            'T',
            MAP.getCellAfterMovement(new Position(1, 1), "down"),
            "Moving down should move correctly"
        );
    }

    @Test
    void testGetCellAfterMovementWithDirectionLeft() {
        assertEquals(
            'T',
            MAP.getCellAfterMovement(new Position(1, 1), "left"),
            "Moving left should move correctly"
        );
    }

    @Test
    void testGetCellAfterMovementWithDirectionRight() {
        assertEquals(
            'T',
            MAP.getCellAfterMovement(new Position(1, 1), "right"),
            "Moving right should move correctly"
        );
    }

    @Test
    void testAddPlayer() {
        Reader reader = new StringReader(
            """
            M_
            TT
            """
        );
        GameMap map = new GameMap(reader, 2, 2);
        Position position = map.addPlayer('2');

        assertNotNull(position, "Position of new player added should not be null");
        assertEquals(new Position(0, 1), position, "Should spawn player on a free position");
        assertEquals('2', map.getCell(position), "Position of new player should be the right index");
    }

    @Test
    void testRemovePlayer() {
        Reader reader = new StringReader(
            """
            M2
            TT
            """
        );
        GameMap map = new GameMap(reader, 2, 2);

        assertDoesNotThrow(
            () -> map.removePlayer('2'),
            "Should not throw when removing player that is there"
        );

        assertEquals('_', map.getCell(new Position(0, 1)), "Should be empty where there was a player");
    }

    @Test
    void testRemovePlayerThatIsNotOnMap() {
        Reader reader = new StringReader(
            """
            M2
            TT
            """
        );
        GameMap map = new GameMap(reader, 2, 2);

        assertDoesNotThrow(
            () -> map.removePlayer('3'),
            "Should not throw when removing player that is there"
        );
    }

    @Test
    void testToString() {
        String encodedMap = MAP.toString();

        assertEquals("COMMAND4SPLIT3SPLIT_M_T_TMT___T", encodedMap, "Encoding should work correctly");
    }
}
