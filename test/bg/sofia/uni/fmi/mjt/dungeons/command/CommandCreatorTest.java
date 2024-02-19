package bg.sofia.uni.fmi.mjt.dungeons.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class CommandCreatorTest {
    @Test
    void testNewCommand() {
        String s = "3attack 36 aa";

        assertEquals(
            new Command("attack", '3', List.of("36", "aa")),
            CommandCreator.newCommand(s)
        );
    }

    @Test
    void testNewCommandOneLetterString() {
        String s = "0";

        assertThrows(
            IllegalArgumentException.class,
            () -> CommandCreator.newCommand(s),
            "Should thorw with invalid index"
        );
    }
}
