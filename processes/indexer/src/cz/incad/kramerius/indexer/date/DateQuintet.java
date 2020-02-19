package cz.incad.kramerius.indexer.date;

import java.util.Date;

/**
 * This class is used only by BiblioModsDateParser and ExtendedFields class as auxiliary structure.
 *
 * @author Aleksei Ermak
 * @see    BiblioModsDateParser
 * @see    cz.incad.kramerius.indexer.ExtendedFields
 */
public class DateQuintet {

    private Date date;
    private String dateStr;
    private String yearBegin;
    private String yearEnd;
    private String year;

    public DateQuintet(Date d, String ds, String db, String de, String y) {
        date = d;
        dateStr = ds;
        yearBegin = db;
        yearEnd = de;
        year = y;
    }

    public Date getDate() {
        return date;
    }

    public String getDateStr() {
        return dateStr;
    }

    public String getYear() {
        return year;
    }

    public String getYearBegin() {
        return yearBegin;
    }

    public String getYearEnd() {
        return yearEnd;
    }
}
