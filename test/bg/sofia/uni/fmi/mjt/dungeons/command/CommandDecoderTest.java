package bg.sofia.uni.fmi.mjt.dungeons.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandDecoderTest {
    @Test
    void testIsEncodedMapWhenIs() {
        String encoded = "COMMANDabc";

        assertTrue(CommandDecoder.isEncodedMap(encoded), "Should identidy encoded maps correctly");
    }

    @Test
    void testIsEncodedMapWhenIsNot() {
        String notEncoded = "not_encodedabc";

        assertFalse(CommandDecoder.isEncodedMap(notEncoded), "Should identify non-encoded maps correctly");
    }

    @Test
    void testDecodeEncodedMap() {
        String encoded = "COMMAND3SPLIT4SPLITTM__32_TM__M";

        String expected = """
                T M _ _ 
                3 2 _ T 
                M _ _ M 
                """.trim();

        assertEquals(expected, CommandDecoder.decodeEncodedMap(encoded), "Should decode encoded maps correctly");
    }

    @Test
    void testIsRequestToLeaveWhenIs() {
        String encoded = "CAN_LEAVEabc";

        assertTrue(CommandDecoder.isRequestToLeave(encoded), "Should identidy request to leave correctly");
    }

    @Test
    void testIsRequestToLeaveWhenIsNot() {
        String noteEncoded = "abc";

        assertFalse(CommandDecoder.isRequestToLeave(noteEncoded), "Should identidy request to leave correctly");
    }

    @Test
    void testDecodeRequestToLeave() {
        String encoded = "CAN_LEAVEabv";

        assertEquals("abv", CommandDecoder.decodeRequestToLeave(encoded), "Should decode the request to leave correctly");
    }

    @Test
    void testIsMultiLineStringWhenIs() {
        String encoded = "MULTI_LINE_STRINGabcNEW_LINE_SEPARATORlll";

        assertTrue(CommandDecoder.isMultiLineResponse(encoded), "Should identify multiline strings correctly");
    }

    @Test
    void testIsMultiLineStringWhenIsNot() {
        String notEncoded = "afhuienwfhiohnncwkho8ihy1";

        assertFalse(CommandDecoder.isMultiLineResponse(notEncoded), "Should identify multiline strings correctly");
    }

    @Test
    void testDecodeMultiLineString() {
        String encoded = "MULTI_LINE_STRINGabcNEW_LINE_SEPARATORlll";

        String expected = """
                abc
                lll
                """.trim();
        
        assertEquals(expected, CommandDecoder.decodeMultiLineResponse(encoded), "Should decode multiline strings correctly");
    }
}
