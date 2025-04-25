/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package cz.incad.kramerius.statistics.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.statistics.accesslogs.database.DatabaseStatisticsAccessLogImpl;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import antlr.StringUtils;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.IPAddressFilter;
import cz.incad.kramerius.statistics.filters.IdentifiersFilter;
import cz.incad.kramerius.statistics.filters.LicenseFilter;
import cz.incad.kramerius.statistics.filters.ModelFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.filters.UniqueIPAddressesFilter;
import cz.incad.kramerius.statistics.filters.VisibilityFilter;
import cz.incad.kramerius.statistics.utils.ReportUtils;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import cz.incad.kramerius.utils.database.Offset;

/**
 * Report pro konkretni model
 * 
 * @author pavels
 */
public class ModelStatisticReport extends AbstractStatisticsReport implements StatisticReport {


    public static final Logger LOGGER = Logger.getLogger(ModelStatisticReport.class.getName());

    public static final String REPORT_ID = "model";


    @javax.inject.Inject
    @javax.inject.Named("solr-client")
    private javax.inject.Provider<CloseableHttpClient> provider;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;


    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction repAction,StatisticsFiltersContainer filters, Offset rOffset) {
        try {
            ModelFilter modelFilter = filters.getFilter(ModelFilter.class);

            String selectEndpoint = super.logsEndpoint();
            List<Map<String,Object>> models = new ArrayList<>();
            
            StringBuilder builder = new StringBuilder("q=*");
            super.applyFilters(filters, builder);

            String facetValue = "pids_"+modelFilter.getModel();
            builder.append("&rows=0&facet=true&facet.mincount=1&facet.field="+facetValue);
            
            InputStream iStream = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(this.provider.get(), selectEndpoint, builder.toString(), "json", null);
            String string = IOUtils.toString(iStream, "UTF-8");
            
            ReportUtils.facetIterate(facetValue, string, p-> {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(COUNT_KEY, p.getValue());
                map.put(PID_KEY, p.getKey().toString());
                map.put(MODEL_KEY, modelFilter.getModel());
                try {
                    map.put(TITLE_KEY, titleFromSolr(p.getKey().toString()));
                } catch(IOException ex) {
                    LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
                }
                models.add(map);
            });
            

            JSONObject response = new JSONObject(string).getJSONObject("response");
            Map<String, Object> val = new HashMap<>();
            val.put("sum", response.optInt("numFound", 0));
            models.add(val);
            
            return models;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<Map<String,Object>>();
        }

    }

    private String titleFromSolr(Object key) throws IOException {
        JSONObject result = solrAccess.requestWithSelectReturningJson(String.format("q=%s", URLEncoder.encode("pid:\""+key.toString()+"\"", "UTF-8")), null );
        if (result.has("response")) {
            JSONObject response = result.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");
            if (docs.length() > 0) {
                JSONObject foundDoc = docs.getJSONObject(0);
                return foundDoc.optString("title.search", "");
            }
        }
        return "";
    }

    @Override
    public List<String> getOptionalValues(StatisticsFiltersContainer filters) {
        return new ArrayList<>();
    }

    @Override
    public String getReportId() {
        return REPORT_ID;
    }


    @Override
    public void processAccessLog(final ReportedAction repAction, final StatisticsReportSupport sup,
            final StatisticsFiltersContainer filters) throws StatisticsReportException {
        DateFilter dateFilter = filters.getFilter(DateFilter.class);
        ModelFilter modelFilter = filters.getFilter(ModelFilter.class);
        LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);
        IdentifiersFilter idFilter = filters.getFilter(IdentifiersFilter.class);

        try {
            String selectEndpoint = super.logsEndpoint();
            
            StringBuilder builder = new StringBuilder("q=*");
            ReportUtils.enhanceLicense(builder, licFilter);
            ReportUtils.enhanceDateFilter(builder, dateFilter);
            ReportUtils.enhanceModelFilter(builder, modelFilter);
            ReportUtils.enhanceIdentifiers(builder, idFilter);

            String facetValue = "pids_"+modelFilter.getModel();
            builder.append("&rows=0&facet=true&facet.mincount=1&facet.field="+facetValue);
            
            InputStream iStream = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(this.provider.get(), selectEndpoint, builder.toString(), "json", null);
            String string = IOUtils.toString(iStream, "UTF-8");
            
            ReportUtils.facetIterate(facetValue, string, p-> {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(COUNT_KEY, p.getValue());
                map.put(PID_KEY, p.getKey().toString());
                map.put(MODEL_KEY, modelFilter.getModel());
                try {
                    map.put(TITLE_KEY, titleFromSolr(p.getKey().toString()));
                } catch(IOException ex) {
                    LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
                }
                sup.processReportRecord(map);
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public List<String> verifyFilters(ReportedAction action, StatisticsFiltersContainer container) {
    	List<String> list = new ArrayList<>();
    	ModelFilter modelFilter = container.getFilter(ModelFilter.class);
        if (modelFilter.getModel() == null)  {
        	list.add("model is mandatory");
        }
		DateFilter dateFilter = container.getFilter(DateFilter.class);
		VerificationUtils.dateVerification(list, dateFilter.getRawFromDate());
		VerificationUtils.dateVerification(list, dateFilter.getRawToDate());
        
        return list;

	}
}
