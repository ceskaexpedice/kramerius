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
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            final StringTemplate langs;
            
            if (isUniqueSelected == false) {
                langs = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectLangReport");
            }
            else {
               langs = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectLangReportUnique"); 
            }
          
            langs.setAttribute("action", repAction != null ? repAction.name() : null);
            langs.setAttribute("fromDefined", dateFilter.getFromDate() != null);
            langs.setAttribute("toDefined", dateFilter.getToDate() != null);
            langs.setAttribute("ipaddr", ipFilter.getIpAddress());

            
            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(dateFilter);
            //authors.setAttribute("paging", true);
            String sql = langs.toString();
            
            List<Map<String,Object>> auths = new JDBCQueryTemplate<Map<String,Object>>(connectionProvider.get()) {

                @Override
                public boolean handleRow(ResultSet rs, List<Map<String,Object>> returnsList) throws SQLException {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("count", rs.getInt("count"));
                    map.put("lang", rs.getString("lang"));
                    returnsList.add(map);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql.toString(),params.toArray());
            
            return auths;
        } catch (ParseException e) {
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
            final StringTemplate langs;
            
            if (isUniqueSelected == false) {
                langs = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectLangReport");
            }
            else {
               langs = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectLangReportUnique"); 
            }
            langs.setAttribute("action", repAction != null ? repAction.name() : null);
            langs.setAttribute("fromDefined", dateFilter.getFromDate() != null);
            langs.setAttribute("toDefined", dateFilter.getToDate() != null);
            langs.setAttribute("ipaddr", ipFilter.getIpAddress());


            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(dateFilter);

            //authors.setAttribute("paging", true);
            String sql = langs.toString();
            new JDBCQueryTemplate<Map<String,Object>>(connectionProvider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String,Object>> returnsList) throws SQLException {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("count", rs.getInt("count"));
                    map.put("lang", rs.getString("lang"));
                    sup.processReportRecord(map);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql.toString(), params.toArray());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new StatisticsReportException(e);
        }
    }

}
