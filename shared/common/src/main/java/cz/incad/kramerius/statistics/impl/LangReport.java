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
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.filters.UniqueIPAddressesFilter;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.Offset;

/**
 * 
 * @author pavels
 */
public class LangReport implements StatisticReport{

    public static final Logger LOGGER = Logger.getLogger(LangReport.class.getName());
    
    public static final String REPORT_ID = "lang";

    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction repAction,  StatisticsFiltersContainer filters,Offset rOffset) {
        try {
            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            
            IPAddressFilter ipFilter = filters.getFilter(IPAddressFilter.class);
          
            UniqueIPAddressesFilter uniqueIPFilter = filters.getFilter(UniqueIPAddressesFilter.class);
            
            Boolean isUniqueSelected = uniqueIPFilter.getUniqueIPAddresses();
            final StringTemplate statRecord;
            
            if (isUniqueSelected == false) {
                statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectLangReport");
            }
            else {
               statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectLangReportUnique"); 
            }
          
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
            statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
            statRecord.setAttribute("ipaddr", ipFilter.getIpAddress());

            
            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(dateFilter);
            //statRecord.setAttribute("paging", true);
            String sql = statRecord.toString();
            Connection conn = connectionProvider.get();
            List<Map<String,Object>> langs = new JDBCQueryTemplate<Map<String,Object>>(conn) {

                @Override
                public boolean handleRow(ResultSet rs, List<Map<String,Object>> returnsList) throws SQLException {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("count", rs.getInt("count"));
                    map.put("lang", rs.getString("lang"));
                    returnsList.add(map);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql.toString(),params.toArray());
            LOGGER.fine(String.format("Test statistics connection.isClosed() : %b", conn.isClosed()));
            return langs;
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<Map<String,Object>>();
        } catch (SQLException ex) {
            Logger.getLogger(LangReport.class.getName()).log(Level.SEVERE, null, ex);
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void processAccessLog(final ReportedAction repAction, final StatisticsReportSupport sup,
            final StatisticsFiltersContainer container) throws StatisticsReportException {
        try {
            DateFilter dateFilter = container.getFilter(DateFilter.class);
            IPAddressFilter ipFilter = container.getFilter(IPAddressFilter.class);
            UniqueIPAddressesFilter uniqueIPFilter = container.getFilter(UniqueIPAddressesFilter.class);
            
            Boolean isUniqueSelected = uniqueIPFilter.getUniqueIPAddresses();
            final StringTemplate statRecord;
            
            if (isUniqueSelected == false) {
                statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectLangReport");
            }
            else {
               statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectLangReportUnique"); 
            }
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
            statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
            statRecord.setAttribute("ipaddr", ipFilter.getIpAddress());
            statRecord.setAttribute("paging", false);


            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(dateFilter);

            String sql = statRecord.toString();
            Connection conn = connectionProvider.get();
            new JDBCQueryTemplate<Map<String,Object>>(conn) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String,Object>> returnsList) throws SQLException {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(COUNT_KEY, rs.getInt("count"));
                    map.put(LANG_KEY, rs.getString("lang"));
                    sup.processReportRecord(map);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql.toString(), params.toArray());
            conn.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new StatisticsReportException(e);
        }
    }

    @Override
    public boolean verifyFilters(ReportedAction action, StatisticsFiltersContainer container) {
        return true;
    }
}
