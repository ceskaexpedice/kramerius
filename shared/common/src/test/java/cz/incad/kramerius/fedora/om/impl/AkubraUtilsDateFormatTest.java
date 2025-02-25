package cz.incad.kramerius.fedora.om.impl;

import cz.incad.kramerius.utils.SafeSimpleDateFormat;
import org.junit.Test;

import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AkubraUtilsDateFormatTest {
    //private static final SafeSimpleDateFormat DATE_FORMAT = new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
    // TODO AK_NEW private static final SafeSimpleDateFormat DATE_FORMAT = AkubraUtils.DATE_FORMAT;

//    @Test
//    public void testParsingMultipleMillisTokens() {
//        assertInvalidDateTime("2022-06-24T19:11:55.666.66.6Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.666.6.66Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.66.666.6Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.66.6.666Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.6.66.666Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.6.666.66Z");
//
//        assertInvalidDateTime("2022-06-24T19:11:55.666.666Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.666.66Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.666.6Z");
//
//        assertInvalidDateTime("2022-06-24T19:11:55.66.666Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.66.66Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.66.6Z");
//
//        assertInvalidDateTime("2022-06-24T19:11:55.6.666Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.6.66Z");
//        assertInvalidDateTime("2022-06-24T19:11:55.6.61Z");
//    }
//
//
//    @Test
//    public void testParsingMillisIn4Digits() {
//
//    }
//
//    @Test
//    public void testParsingMillisIn4plusDigits() {
//        //FIXME: incorrectly accepts 4+ digits in ms
//        //assertInvalidDateTime("2022-06-24T19:11:55.6666Z");
//        //assertInvalidDateTime("2022-06-24T19:11:55.66666Z");
//        //assertInvalidDateTime("2022-06-24T19:11:55.666666Z");
//        //assertInvalidDateTime("2022-06-24T19:11:55.6666666Z");
//
//        //at least it doesn't format in more digits, but it does change number of seconds, minutes, days etc
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.6666Z", "2022-06-24T19:12:01.666Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.66666Z", "2022-06-24T19:13:01.666Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.666666Z", "2022-06-24T19:23:01.666Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.6666666Z", "2022-06-24T21:03:01.666Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.66666666Z", "2022-06-25T13:43:01.666Z");
//    }
//
//    @Test
//    public void testParsingMillisIn3Digits() {
//        assertValidDateTime("2022-06-24T19:11:55.666Z");
//    }
//
//    @Test
//    public void testParsingMillisIn2Digits() {
//        assertValidDateTime("2022-06-24T19:11:55.66Z");
//        assertValidDateTime("2022-06-24T19:11:55.066Z");
//    }
//
//    @Test
//    public void testParsingMillisIn1Digit() {
//        assertValidDateTime("2022-06-24T19:11:55.6Z");
//        assertValidDateTime("2022-06-24T19:11:55.006Z");
//    }
//
//    @Test
//    public void testParsingMillisIn0Digits() {
//        assertInvalidDateTime("2022-06-24T19:11:55.Z");
//        assertValidDateTime("2022-06-24T19:11:55.0Z");
//        assertValidDateTime("2022-06-24T19:11:55.00Z");
//        assertValidDateTime("2022-06-24T19:11:55.000Z");
//    }
//
//    @Test
//    public void testParsingNoMillis() {
//        assertInvalidDateTime("2022-06-24T19:11:55Z");
//    }
//
//    @Test
//    public void testFormattingMillisIn3Digits() {
//        assertSameAfterParsingAndFormatting("2022-06-24T19:11:55.666Z");
//    }
//
//    @Test
//    public void testFormattingMillisIn2Digits() {
//        //assertSameAfterParsingAndFormatting("2022-06-24T19:11:55.66Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.66Z", "2022-06-24T19:11:55.066Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.066Z", "2022-06-24T19:11:55.066Z");
//    }
//
//    @Test
//    public void testFormattingMillisIn1Digit() {
//        //assertSameAfterParsingAndFormatting("2022-06-24T19:11:55.6Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.6Z", "2022-06-24T19:11:55.006Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.06Z", "2022-06-24T19:11:55.006Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.006Z", "2022-06-24T19:11:55.006Z");
//    }
//
//    @Test
//    public void testFormattingMillisIn0Digit() {
//        //cannot and should not be parsed, so no reason for this test
//        //assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.Z", "2022-06-24T19:11:55.0Z");
//    }
//
//    @Test
//    public void testFormattingNoMillis() {
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.0Z", "2022-06-24T19:11:55.000Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.00Z", "2022-06-24T19:11:55.000Z");
//        assertMatchesAfterParsingAndFormatting("2022-06-24T19:11:55.000Z", "2022-06-24T19:11:55.000Z");
//        //assertSameAfterParsingAndFormatting("2022-06-24T19:11:55.0Z");
//    }

//    private void assertMatchesAfterParsingAndFormatting(String original, String expected) {
//        try {
//            Date parsed = DATE_FORMAT.parse(original);
//            String formatted = DATE_FORMAT.format(parsed);
//            assertEquals(expected, formatted);
//        } catch (ParseException e) {
//            fail("should be valid: " + original);
//        }
//    }
//
//    private void assertSameAfterParsingAndFormatting(String original) {
//        try {
//            Date parsed = DATE_FORMAT.parse(original);
//            String formatted = DATE_FORMAT.format(parsed);
//            assertEquals(original, formatted);
//        } catch (DateTimeParseException e) {
//            fail("should be valid: " + original);
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void assertValidDateTime(String dt) {
//        try {
//            DATE_FORMAT.parse(dt);
//        } catch (ParseException e) {
//            fail("should be valid: " + dt);
//        }
//    }
//
//    private void assertInvalidDateTime(String dt) {
//        try {
//            Date parsed = DATE_FORMAT.parse(dt);
//            fail("should not be valid: " + dt + ", result: " + DATE_FORMAT.format(parsed));
//        } catch (ParseException e) {
//            //ok, failed to parse as expected
//        }
//    }
}
