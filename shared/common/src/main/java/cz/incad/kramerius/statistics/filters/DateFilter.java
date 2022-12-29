package cz.incad.kramerius.statistics.filters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.statistics.StatisticReport;

/**
 * Represents simple date filter (date_from  -  date_to)
 * @author pstastny
 */
public class DateFilter implements StatisticsFilter {

    
    public static final Logger LOGGER = Logger.getLogger(DateFilter.class.getName());
    
    private SimpleDateFormat inputFormat = StatisticReport.DATE_FORMAT;
    private SimpleDateFormat outputFormat = StatisticReport.SOLR_DATE_FORMAT;

    private String fromDate;
    private String toDate;
    
    
    /**
     * Returns date from 
     * @return
     */
    public String getFromDate() {
        if(this.fromDate != null) {
            return getFormattedDate(this.fromDate);
        } else return null;
    }
    
    
    public String getRawFromDate() {
        return this.fromDate;
    }
    
    
    private String getFormattedDate(String fDate) {
      if (this.inputFormat != null && this.outputFormat !=null) {
          try {
            Date parsed = this.inputFormat.parse(fDate);
            return this.outputFormat.format(parsed);
          } catch (ParseException e) {
              LOGGER.log(Level.SEVERE,e.getMessage(),e);
              return fDate;
          }
      } else {
          return fDate;
      }
    }

    /**
     * Sets date from
     * @param fromDate
     */
    public void setFromDate(String fromDate) {
        if (fromDate != null && (!fromDate.trim().equals(""))) {
            this.fromDate = fromDate;
        } else {
            //LOGGER.severe("bad value");
        }
    }

    /**
     * Returns date to
     * @return
     */
    public String getToDate() {
        if (this.toDate != null) {
            return getFormattedDate(toDate);
        } else {
            return null;
        }
    }

    public String getRawToDate() {
        return this.toDate;
    }
    
    /**
     * Sets date to
     * @param toDate
     */
    public void setToDate(String toDate) {
        if (toDate != null && (!toDate.trim().equals(""))) {
            this.toDate = toDate;
        } else {
            //LOGGER.severe("bad value");
        }
    }
    
    
    public SimpleDateFormat getInputFormat() {
        return inputFormat;
    }
    
    public void setInputFormat(SimpleDateFormat inputFormat) {
        this.inputFormat = inputFormat;
    }
    
    public SimpleDateFormat getOutputFormat() {
        return outputFormat;
    }
    
    public void setOutputFormat(SimpleDateFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

}
