package cz.incad.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ISODateUtils {

    public static Date parseISODate(String val) {
        Instant inst = DateTimeFormatter.ISO_DATE_TIME.parse(val, Instant::from);
        Date date = Date.from(inst);
        return date;
    }
}
