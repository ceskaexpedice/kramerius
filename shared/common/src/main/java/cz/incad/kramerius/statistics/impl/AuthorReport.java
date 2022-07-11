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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.statistics.accesslogs.database.DatabaseStatisticsAccessLogImpl;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.IOUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

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
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.Offset;

/**
 * @author pavels
 *
 */
public class AuthorReport extends AbstractStatisticsReport implements StatisticReport{
    
    public static final Logger LOGGER = Logger.getLogger(AuthorReport.class.getName());
    
    public static final String REPORT_ID = "author";
    
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    
    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction repAction,StatisticsFiltersContainer filters, Offset rOffset) {
        try {
            String selectEndpoint = super.logsEndpoint();
            
            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);
            IdentifiersFilter idFilter = filters.getFilter(IdentifiersFilter.class);

            StringBuilder builder = new StringBuilder("q=*");

            ReportUtils.enhanceLicense(builder, licFilter);
            ReportUtils.enhanceDateFilter(builder, dateFilter);
            ReportUtils.enhanceIdentifiers(builder, idFilter);
            
            String facetField = "authors";
            builder.append(String.format("&rows=0&facet=true&facet.mincount=1&facet.field=%s", facetField));
            InputStream iStream = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(selectEndpoint, builder.toString(), "json");
            String string = IOUtils.toString(iStream, "UTF-8");

            List<Map<String,Object>> authors = new ArrayList<>();
            ReportUtils.facetIterate(facetField, string, p-> {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(COUNT_KEY, p.getValue());
                map.put(AUTHOR_NAME_KEY, p.getKey());
                authors.add(map);
            });
            
            return authors;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<Map<String,Object>>();
        }
    }

    @Override
    public List<String> getOptionalValues() {
        return new ArrayList<String>();
    }

    @Override
    public String getReportId() {
        return REPORT_ID;
    }


    @Override
    public void prepareViews(ReportedAction action, StatisticsFiltersContainer container) {
    }

    @Override
    public void processAccessLog(final ReportedAction repAction, final StatisticsReportSupport sup,
            StatisticsFiltersContainer filters) throws StatisticsReportException {
        try {
            String selectEndpoint = super.logsEndpoint();
            
            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);
            IdentifiersFilter idFilter = filters.getFilter(IdentifiersFilter.class);

            StringBuilder builder = new StringBuilder("q=*");

            ReportUtils.enhanceLicense(builder, licFilter);
            ReportUtils.enhanceDateFilter(builder, dateFilter);
            ReportUtils.enhanceIdentifiers(builder, idFilter);
            
            String facetField = "authors";
            builder.append(String.format("&rows=0&facet=true&facet.mincount=1&facet.field=%s", facetField));
            InputStream iStream = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(selectEndpoint, builder.toString(), "json");
            String string = IOUtils.toString(iStream, "UTF-8");

            ReportUtils.facetIterate(facetField, string, p-> {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(COUNT_KEY, p.getValue());
                map.put(AUTHOR_NAME_KEY, p.getKey());
                sup.processReportRecord(map);
            });

        } catch (IOException e) {
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
