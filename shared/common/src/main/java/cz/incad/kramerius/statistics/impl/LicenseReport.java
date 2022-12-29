package cz.incad.kramerius.statistics.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.utils.ReportUtils;
import cz.incad.kramerius.utils.database.Offset;

/**
 * Poskytnute licence 
 * @author happy
 */
public class LicenseReport extends AbstractStatisticsReport implements StatisticReport{
    
    public static final Logger LOGGER = Logger.getLogger(LicenseReport.class.getName());

    
    public static final String REPORT_ID = "license";

    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction reportedAction, StatisticsFiltersContainer filters,
            Offset rOffset) throws StatisticsReportException {
        try {
            String selectEndpint = super.logsEndpoint();
            
            List<Map<String,Object>> langs = new ArrayList<>();
            StringBuilder builder = new StringBuilder("q=*");
            applyFilters(filters, builder);
            
            String facetField = "provided_by_license";
            builder.append(String.format("&rows=0&facet=true&facet.mincount=1&facet.field=%s", facetField));
            InputStream iStream = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(selectEndpint, builder.toString(), "json");
            String string = IOUtils.toString(iStream, "UTF-8");
    
            ReportUtils.facetIterate(facetField, string, p-> {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(COUNT_KEY, p.getValue());
                map.put(PROVIDED_LICENSE_KEY, p.getKey());
                langs.add(map);
            });
            return langs;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<Map<String,Object>>();
        }
    }

    @Override
    public List<String> getOptionalValues(StatisticsFiltersContainer filters) {
        List<String> retvals = new ArrayList<>();
        try {
            String selectEndpint = super.logsEndpoint();

            StringBuilder builder = new StringBuilder("q=*");
            applyFilters(filters,builder);
            String facetField = "provided_by_license";
            //
            builder.append(String.format("&rows=0&facet=true&facet.mincount=1&facet.field=%s", facetField));
            InputStream iStream = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(selectEndpint, builder.toString(), "json");
            String string = IOUtils.toString(iStream, "UTF-8");
    
            ReportUtils.facetIterate(facetField, string, p-> {
//                Map<String, Object> map = new HashMap<String, Object>();
//                map.put(COUNT_KEY, p.getValue());
//                map.put(PROVIDED_LICENSE_KEY, p.getKey());
                retvals.add(p.getKey().toString());
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return retvals;
    }

    @Override
    public String getReportId() {
        return REPORT_ID;
    }


    @Override
    public void processAccessLog(ReportedAction action, StatisticsReportSupport sup,
            StatisticsFiltersContainer filters) throws StatisticsReportException {
        try {
            String selectEndpint = super.logsEndpoint();
            StringBuilder builder = new StringBuilder("q=*");
            applyFilters(filters, builder);
            String facetField = "provided_by_license";
            //
            builder.append(String.format("&rows=0&facet=true&facet.mincount=1&facet.field=%s", facetField));
            InputStream iStream = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(selectEndpint, builder.toString(), "json");
            String string = IOUtils.toString(iStream, "UTF-8");
    
            ReportUtils.facetIterate(facetField, string, p-> {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(COUNT_KEY, p.getValue());
                map.put(PROVIDED_LICENSE_KEY, p.getKey());
                sup.processReportRecord(map);
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public List<String> verifyFilters(ReportedAction action, StatisticsFiltersContainer container) {
        return new ArrayList<>();
    }

}
