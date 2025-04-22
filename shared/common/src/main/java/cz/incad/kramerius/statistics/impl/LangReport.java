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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.statistics.accesslogs.database.DatabaseStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.accesslogs.solr.SolrStatisticsAccessLogImpl;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.IOUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.IPAddressFilter;
import cz.incad.kramerius.statistics.filters.IdentifiersFilter;
import cz.incad.kramerius.statistics.filters.LicenseFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.filters.UniqueIPAddressesFilter;
import cz.incad.kramerius.statistics.filters.VisibilityFilter;
import cz.incad.kramerius.statistics.utils.ReportUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.Offset;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

/**
 * Report poskytnutych jazyku
 * 
 * @author pavels
 */
public class LangReport extends AbstractStatisticsReport implements StatisticReport{

    public static final Logger LOGGER = Logger.getLogger(LangReport.class.getName());
    
    public static final String REPORT_ID = "lang";

    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;


    @javax.inject.Inject
    @javax.inject.Named("solr-client")
    javax.inject.Provider<CloseableHttpClient> provider;


    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction repAction,  StatisticsFiltersContainer filters,Offset rOffset) {
        try {
            String selectEndpint = super.logsEndpoint();
            
            List<Map<String,Object>> langs = new ArrayList<>();
            

            StringBuilder builder = new StringBuilder("q=*");
//            ReportUtils.enhanceLicense(builder, licFilter);
//            ReportUtils.enhanceDateFilter(builder, dateFilter);
//            ReportUtils.enhanceIdentifiers(builder, idFilter);
            super.applyFilters(filters,builder);

            String facetField = "langs";
            builder.append(String.format("&rows=0&facet=true&facet.mincount=1&facet.field=%s", facetField));
            InputStream iStream = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(this.provider.get(), selectEndpint, builder.toString(), "json", null);
            String string = IOUtils.toString(iStream, "UTF-8");

            ReportUtils.facetIterate(facetField, string, p-> {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(COUNT_KEY, p.getValue());
                map.put(LANG_KEY, p.getKey());
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
        return new ArrayList<String>();
    }

    @Override
    public String getReportId() {
        return REPORT_ID;
    }


    @Override
    public void processAccessLog(final ReportedAction repAction, final StatisticsReportSupport sup,
            final StatisticsFiltersContainer filters) throws StatisticsReportException {
        try {
            String selectEndpint = super.logsEndpoint();
            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);
            IdentifiersFilter idFilter = filters.getFilter(IdentifiersFilter.class);

            StringBuilder builder = new StringBuilder("q=*");
            ReportUtils.enhanceLicense(builder, licFilter);
            ReportUtils.enhanceDateFilter(builder, dateFilter);
            ReportUtils.enhanceIdentifiers(builder, idFilter);

            String facetField = "langs";
            builder.append(String.format("&rows=0&facet=true&facet.mincount=1&facet.field=%s", facetField));
            InputStream iStream = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(this.provider.get(), selectEndpint, builder.toString(), "json", null);
            String string = IOUtils.toString(iStream, "UTF-8");

            ReportUtils.facetIterate(facetField, string, p-> {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(COUNT_KEY, p.getValue());
                map.put(LANG_KEY, p.getKey());
                sup.processReportRecord(map);
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new StatisticsReportException(e);
        }
    }

	@Override
	public List<String> verifyFilters(ReportedAction action, StatisticsFiltersContainer container) {
    	List<String> list = new ArrayList<>();
		DateFilter dateFilter = container.getFilter(DateFilter.class);
		VerificationUtils.dateVerification(list, dateFilter.getRawFromDate());
		VerificationUtils.dateVerification(list, dateFilter.getRawToDate());
		return list;
	}
}
