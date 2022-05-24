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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.IPAddressFilter;
import cz.incad.kramerius.statistics.filters.LicenseFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.filters.UniqueIPAddressesFilter;
import cz.incad.kramerius.statistics.filters.VisibilityFilter;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.Offset;

/**
 * @author pavels
 *
 */
public class AuthorReport implements StatisticReport{
    
    public static final Logger LOGGER = Logger.getLogger(AuthorReport.class.getName());
    
    public static final String REPORT_ID = "author";
    
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    
    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction repAction,StatisticsFiltersContainer filters, Offset rOffset) {
        try {
            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
            LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);

            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectAuthorReport");
            
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging", true);
            statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
            statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
            statRecord.setAttribute("visibility", visFilter.asMap());

            statRecord.setAttribute("licenseDefined", licFilter.getLicence() != null);
            //statRecord.setAttribute("license", licFilter.getLicence());

            
            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(dateFilter, licFilter,rOffset);
            String sql = statRecord.toString();  
            Connection conn = connectionProvider.get();
            List<Map<String,Object>> auths = new JDBCQueryTemplate<Map<String,Object>>(conn) {

                @Override
                public boolean handleRow(ResultSet rs, List<Map<String,Object>> returnsList) throws SQLException {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(COUNT_KEY, rs.getInt("count"));
                    map.put(AUTHOR_NAME_KEY, rs.getString("author_name"));
                    returnsList.add(map);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql.toString(), params.toArray());

            LOGGER.fine(String.format("Test statistics connection.isClosed() : %b", conn.isClosed()));
            return auths;
        } catch (ParseException | SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return new ArrayList<Map<String, Object>>();
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
            final DateFilter dateFilter = filters.getFilter(DateFilter.class);
            VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
            
            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                        .getInstanceOf("selectAuthorReport");
            
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging", false);
            statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
            statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
            statRecord.setAttribute("visibility", visFilter.asMap());

            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(dateFilter);

            String sql = statRecord.toString();
            Connection conn = connectionProvider.get();
            new JDBCQueryTemplate<Map<String,Object>>(conn) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String,Object>> returnsList) throws SQLException {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(COUNT_KEY, rs.getInt("count"));
                    map.put(AUTHOR_NAME_KEY, rs.getString("author_name"));
                    returnsList.add(map);
                    sup.processReportRecord(map);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql.toString(),params.toArray());
            LOGGER.fine(String.format("Test statistics connection.isClosed() : %b", conn.isClosed()));
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new StatisticsReportException(e);
       } catch (SQLException ex) {
            Logger.getLogger(AuthorReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	@Override
	public List<String> verifyFilters(ReportedAction action, StatisticsFiltersContainer container) {
    	List<String> list = new ArrayList<>();
		DateFilter dateFilter = container.getFilter(DateFilter.class);
		VerificationUtils.dateVerification(list, dateFilter.getFromDate());
		VerificationUtils.dateVerification(list, dateFilter.getToDate());
		return list;
	}
}
