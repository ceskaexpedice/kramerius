package cz.inovatika.dochub.utils;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class FileNameDateUtilsTest {


    @Test
    public void testFileName() {
        String fileName = "2026-02-13T11-55-30.284427100Z.log";
        Instant instant = FileNameDateUtils.instantFromFileName(Path.of(fileName));
        Assert.assertNotNull(instant);
        ZonedDateTime dateTime = instant.atZone(ZoneId.of("UTC"));

        Assert.assertEquals("Year", 2026, dateTime.getYear());
        Assert.assertEquals("Month", 2, dateTime.getMonthValue());
        Assert.assertEquals("Day", 13, dateTime.getDayOfMonth());
    }
}
