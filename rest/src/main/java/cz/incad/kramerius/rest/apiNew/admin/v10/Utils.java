package cz.incad.kramerius.rest.apiNew.admin.v10;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static String toFormattedStringOrNull(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
        }
    }

    public static String toFormattedStringOrNull(long timeInSeconds) {
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(timeInSeconds, 0, ZoneOffset.UTC);
        return toFormattedStringOrNull(localDateTime);
    }
}
