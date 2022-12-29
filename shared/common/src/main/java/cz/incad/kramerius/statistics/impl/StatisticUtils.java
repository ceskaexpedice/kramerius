package cz.incad.kramerius.statistics.impl;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.LicenseFilter;
import cz.incad.kramerius.utils.database.Offset;

public class StatisticUtils {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List jdbcParams(DateFilter filter, LicenseFilter licenseFilter, Offset rOffset) throws ParseException {
        List params = new ArrayList();
        if (filter.getFromDate() != null && (!filter.getFromDate().trim().equals(""))) {
            try {
                params.add(new Timestamp(StatisticReport.TIMESTAMP_FORMAT.parse(filter.getFromDate()).getTime()));
            } catch (ParseException e) {
                params.add(new Timestamp(StatisticReport.DATE_FORMAT.parse(filter.getFromDate()).getTime()));
            }

        }
        if (filter.getToDate() != null && (!filter.getToDate().trim().equals(""))) {
            try {
                params.add(new Timestamp(StatisticReport.TIMESTAMP_FORMAT.parse(filter.getToDate()).getTime()));
            } catch (ParseException e) {
                params.add(new Timestamp(StatisticReport.DATE_FORMAT.parse(filter.getToDate()).getTime()));
            }
        }
        if (licenseFilter != null && licenseFilter.getLicence() != null) {
        	params.add(licenseFilter.getLicence());
        }
        
        if (rOffset != null) {
            params.add(Integer.parseInt(rOffset.getOffset()));
            params.add(Integer.parseInt(rOffset.getSize()));
        }

        return params;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List jdbcParams(DateFilter filter) throws ParseException {
        List params = new ArrayList();
        if (filter.getFromDate() != null && (!filter.getFromDate().trim().equals(""))) {
            try {
                params.add(new Timestamp(StatisticReport.TIMESTAMP_FORMAT.parse(filter.getFromDate()).getTime()));
            } catch (ParseException e) {
                params.add(new Timestamp(StatisticReport.DATE_FORMAT.parse(filter.getFromDate()).getTime()));
            }
        }
        if (filter.getToDate() != null && (!filter.getToDate().trim().equals(""))) {
            try {
                params.add(new Timestamp(StatisticReport.TIMESTAMP_FORMAT.parse(filter.getToDate()).getTime()));
            }catch(ParseException e) {
                params.add(new Timestamp(StatisticReport.DATE_FORMAT.parse(filter.getToDate()).getTime()));

            }
        }
        return params;
    }
}
