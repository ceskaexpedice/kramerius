package cz.incad.utils;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SafeSimpleDateFormat extends SimpleDateFormat {

    public SafeSimpleDateFormat(String pattern) {
        super(pattern);
    }

    @Override
    public synchronized Date parse(String source) throws ParseException {
        return super.parse(source);
    }

    @Override
    public synchronized StringBuffer format(Date date, StringBuffer toAppendTo,
            FieldPosition fieldPosition) {
        return super.format(date, toAppendTo, fieldPosition);
    }
}
