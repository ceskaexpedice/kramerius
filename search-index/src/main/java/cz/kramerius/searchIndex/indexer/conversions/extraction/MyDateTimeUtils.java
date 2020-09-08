package cz.kramerius.searchIndex.indexer.conversions.extraction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class MyDateTimeUtils {

    private static final String TIMEZONE = "UTC";
    //private static final String TIMEZONE = "GMT";
    //private static final String TIMEZONE="CET";

    /**
     * @param day
     * @param month
     * @param year
     * @return timestamp of the first millisecond of the day (excluding midnight)
     */
    public static Date toDayStart(int day, int month, int year) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month - 1, day, 0, 0, 0);
        gregorianCalendar.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        gregorianCalendar.set(GregorianCalendar.MILLISECOND, 1);
        return new Date(gregorianCalendar.getTimeInMillis());
    }

    /**
     * @param day
     * @param month
     * @param year
     * @return timestamp of the last millisecond of the day (excluding midnight)
     */
    public static Date toDayEnd(int day, int month, int year) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month - 1, day, 23, 59, 59);
        gregorianCalendar.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        gregorianCalendar.set(GregorianCalendar.MILLISECOND, 999);
        return new Date(gregorianCalendar.getTimeInMillis());
    }

    /**
     * @param month
     * @param year
     * @return timestamp of the first millisecond of the first day of the month (excluding midnight)
     */
    public static Date toMonthStart(int month, int year) {
        return toDayStart(1, month, year);
    }

    /**
     * @param month
     * @param year
     * @return timestamp of the last millisecond of the last day of the month (excluding midnight)
     */
    public static Date toMonthEnd(int month, int year) {
        return toDayEnd(getLastDayOfTheMonth(month, year), month, year);
    }

    /**
     * @param year
     * @return timestamp of the first millisecond of the year (excluding midnight)
     */
    public static Date toYearStart(int year) {
        return toDayStart(1, 1, year);
    }

    /**
     * @param year
     * @return timestamp of the last millisecond of the year (excluding midnight)
     */
    public static Date toYearEnd(int year) {
        return toDayEnd(31, 12, year);
    }

    /**
     * @param input year with unsure numbers, for example 19uu for 20th century, 182- for eighteen-twenties
     * @return timestamp of the first millisecond of the first year, that matches the input (excluding midnight)
     */
    public static Date toYearStartFromPartialYear(String input) {
        StringBuilder builder = new StringBuilder();
        boolean stillNumbers = true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (stillNumbers) {
                if (Character.isDigit(c)) {
                    builder.append(c);
                } else {
                    stillNumbers = false;
                    builder.append('0');
                }
            } else {
                builder.append('0');
            }
        }
        Integer year = Integer.valueOf(builder.toString());
        return toYearStart(year);
    }

    /**
     * @param input year with unsure numbers, for example 19uu for 20th century, 182- for eighteen-twenties
     * @return timestamp of the last millisecond of the last year, that matches the input (excluding midnight)
     */
    public static Date toYearEndFromPartialYear(String input) {
        StringBuilder builder = new StringBuilder();
        boolean stillNumbers = true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (stillNumbers) {
                if (Character.isDigit(c)) {
                    builder.append(c);
                } else {
                    stillNumbers = false;
                    builder.append('9');
                }
            } else {
                builder.append('9');
            }
        }
        Integer year = Integer.valueOf(builder.toString());
        return toYearEnd(year);
    }

    private static int getLastDayOfTheMonth(int month, int year) {
        Calendar calendar = new GregorianCalendar(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * @param date
     * @return Date formatted for Solr field of type date
     */
    public static String formatForSolr(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        return sdf.format(date) + "Z";
    }


}
