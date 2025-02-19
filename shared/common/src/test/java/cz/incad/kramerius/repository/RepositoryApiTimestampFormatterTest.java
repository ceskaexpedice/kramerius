package cz.incad.kramerius.repository;

import org.ceskaexpedice.akubra.ObjectProperties;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RepositoryApiTimestampFormatterTest {

    //public static final DateTimeFormatter TIMESTAMP_FORMATTER_1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    //public static final DateTimeFormatter TIMESTAMP_FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.[SSS][SS][S]'Z'");
    //public static final DateTimeFormatter TIMESTAMP_FORMATTER_3 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]'Z'");

    //best solution, still not perfect
    public static final DateTimeFormatter TIMESTAMP_FORMATTER_4 = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, false)
            .appendPattern("'Z'")
            .toFormatter();

    public static final DateTimeFormatter TIMESTAMP_FORMATTER = ObjectProperties.TIMESTAMP_FORMATTER;

    @Test
    public void testParsingMultipleMillisTokens() {
        assertInvalidDateTime("2022-06-24T19:11:55.666.66.6Z");
        assertInvalidDateTime("2022-06-24T19:11:55.666.6.66Z");
        assertInvalidDateTime("2022-06-24T19:11:55.66.666.6Z");
        assertInvalidDateTime("2022-06-24T19:11:55.66.6.666Z");
        assertInvalidDateTime("2022-06-24T19:11:55.6.66.666Z");
        assertInvalidDateTime("2022-06-24T19:11:55.6.666.66Z");

        assertInvalidDateTime("2022-06-24T19:11:55.666.666Z");
        assertInvalidDateTime("2022-06-24T19:11:55.666.66Z");
        assertInvalidDateTime("2022-06-24T19:11:55.666.6Z");

        assertInvalidDateTime("2022-06-24T19:11:55.66.666Z");
        assertInvalidDateTime("2022-06-24T19:11:55.66.66Z");
        assertInvalidDateTime("2022-06-24T19:11:55.66.6Z");

        assertInvalidDateTime("2022-06-24T19:11:55.6.666Z");
        assertInvalidDateTime("2022-06-24T19:11:55.6.66Z");
        assertInvalidDateTime("2022-06-24T19:11:55.6.61Z");
    }

    @Test
    public void testParsingMillisIn4plusDigits() {
        assertInvalidDateTime("2022-06-24T19:11:55.6666Z");
        assertInvalidDateTime("2022-06-24T19:11:55.66666Z");
        assertInvalidDateTime("2022-06-24T19:11:55.666666Z");
        assertInvalidDateTime("2022-06-24T19:11:55.6666666Z");
    }

    @Test
    public void testParsingMillisIn3Digits() {
        assertValidDateTime("2022-06-24T19:11:55.666Z");
    }

    @Test
    public void testParsingMillisIn2Digits() {
        assertValidDateTime("2022-06-24T19:11:55.66Z");
        assertValidDateTime("2022-06-24T19:11:55.066Z");
    }

    @Test
    public void testParsingMillisIn1Digit() {
        assertValidDateTime("2022-06-24T19:11:55.6Z");
        assertValidDateTime("2022-06-24T19:11:55.006Z");
    }

    @Test
    public void testParsingMillisIn0Digits() {
        //FIXME: this shouldn't be really accepted. But it's the best solution still.
        //assertInvalidDateTime("2022-06-24T19:11:55.Z");
        assertValidDateTime("2022-06-24T19:11:55.0Z");
    }

    @Test
    public void testParsingNoMillis() {
        assertInvalidDateTime("2022-06-24T19:11:55Z");
    }

    @Test
    public void testFormattingMillisIn3Digits() {
        assertSameAfterParsingAndFormatting("2022-06-24T19:11:55.666Z");
    }

    @Test
    public void testFormattingMillisIn2Digits() {
        assertSameAfterParsingAndFormatting("2022-06-24T19:11:55.66Z");
    }

    @Test
    public void testFormattingMillisIn1Digit() {
        assertSameAfterParsingAndFormatting("2022-06-24T19:11:55.6Z");
    }

    @Test
    public void testFormattingMillisIn0Digit() {
        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.0Z", "2022-06-24T19:11:55.0Z");
    }

    @Test
    public void testFormattingNoMillis() {
        assertSameAfterParsingAndFormatting("2022-06-24T19:11:55.0Z");
    }

    private void assertMatchesAfterParsingAndFormatting(String original, String expected) {
        //System.getProperties();
        
        
        try {
            LocalDateTime parsed = LocalDateTime.parse(original, TIMESTAMP_FORMATTER);
            String formatted = parsed.format(TIMESTAMP_FORMATTER);
            assertEquals(formatted, expected);
        } catch (DateTimeParseException e) {
            fail("original: " + original+", expected:"+expected);
        }
    }

    private void assertSameAfterParsingAndFormatting(String original) {
        try {
            LocalDateTime parsed = LocalDateTime.parse(original, TIMESTAMP_FORMATTER);
            String formatted = parsed.format(TIMESTAMP_FORMATTER);
            assertEquals(original, formatted);
        } catch (DateTimeParseException e) {
            fail("should be valid: " + original);
        }
    }

    private void assertValidDateTime(String dt) {
        try {
            LocalDateTime.parse(dt, TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException e) {
            fail("should be valid: " + dt);
        }
    }

    private void assertInvalidDateTime(String dt) {
        try {
            LocalDateTime.parse(dt, TIMESTAMP_FORMATTER);
            fail("should not be valid: " + dt);
        } catch (DateTimeParseException e) {
            //ok, failed to parse as expected
        }
    }
}
