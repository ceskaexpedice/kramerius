package cz.incad.kramerius.statistics.filters;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents simple date filter (date_from  -  date_to)
 * @author pstastny
 */
public class DateFilter implements StatisticsFilter {

    public static final Logger LOGGER = Logger.getLogger(DateFilter.class.getName());
    
    private String fromDate;
    private String toDate;
    
    /**
     * Returns date from 
     * @return
     */
    public String getFromDate() {
        return fromDate;
    }

    /**
     * Sets date from
     * @param fromDate
     */
    public void setFromDate(String fromDate) {
        if (fromDate != null && (!fromDate.trim().equals(""))) {
            this.fromDate = fromDate;
        } else {
            LOGGER.severe("bad value");
        }
    }

    /**
     * Returns date to
     * @return
     */
    public String getToDate() {
        return toDate;
    }

    /**
     * Sets date to
     * @param toDate
     */
    public void setToDate(String toDate) {
        if (toDate != null && (!toDate.trim().equals(""))) {
            this.toDate = toDate;
        } else {
            LOGGER.severe("bad value");
        }
    }

}
