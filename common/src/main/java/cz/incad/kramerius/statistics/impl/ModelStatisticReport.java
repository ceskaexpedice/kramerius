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
import java.text.SimpleDateFormat;
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
import cz.incad.kramerius.statistics.filters.ModelFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.filters.UniqueIPAddressesFilter;
import cz.incad.kramerius.statistics.filters.VisibilityFilter;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import cz.incad.kramerius.utils.database.Offset;
import javax.swing.JOptionPane;

/**
 * @author pavels
 */
public class ModelStatisticReport implements StatisticReport {


    public static final Logger LOGGER = Logger.getLogger(ModelStatisticReport.class.getName());

    public static final String REPORT_ID = "model";



    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction repAction,StatisticsFiltersContainer filters, Offset rOffset) {
        try {
            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            ModelFilter modelFilter = filters.getFilter(ModelFilter.class);
            VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
            UniqueIPAddressesFilter uniqueIPFilter = filters.getFilter(UniqueIPAddressesFilter.class);
            
            Boolean isUniqueSelected = uniqueIPFilter.getUniqueIPAddresses();
            final StringTemplate statRecord;
            
            if (isUniqueSelected == false) {
                statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectModelReport");
            }
            else {
               statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectModelReportUnique"); 
            }
            statRecord.setAttribute("model", modelFilter.getModel());
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging", true);
            statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
            statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
            statRecord.setAttribute("visibility", visFilter.asMap());


            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(dateFilter, rOffset);
            String sql = statRecord.toString();
            List<Map<String, Object>> returns = new JDBCQueryTemplate<Map<String, Object>>(connectionProvider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                    Map<String, Object> val = new HashMap<>();
                    val.put(COUNT_KEY, rs.getInt("count"));
                    val.put(PID_KEY, rs.getString("pid"));
                    val.put(TITLE_KEY, rs.getString("title"));
                    val.put(MODEL_KEY, rs.getString("model"));
                    returnsList.add(val);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql, params.toArray());

            return returns;
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return new ArrayList<Map<String, Object>>();
        }
    }

    @Override
    public List<String> getOptionalValues() {
        final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectModels");
        String sql = statRecord.toString();
        List<String> returns = new JDBCQueryTemplate<String>(connectionProvider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                String model = rs.getString("model");
                returnsList.add(model);
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(sql);

        return returns;
    }

    @Override
    public String getReportId() {
        return REPORT_ID;
    }

    
    
    @Override
    public void prepareViews(ReportedAction action, StatisticsFiltersContainer filters) throws StatisticsReportException {
        try {
            ModelFilter modelFilter = filters.getFilter(ModelFilter.class);
            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            IPAddressFilter ipFilter = filters.getFilter(IPAddressFilter.class);
            
            
            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("prepareModelView");
            statRecord.setAttribute("model", modelFilter.getModel());
            statRecord.setAttribute("action", action != null ? action.name() : null);
            statRecord.setAttribute("paging", false);
            
            statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
            statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
            statRecord.setAttribute("ipaddr", ipFilter.getIpAddress());
            
            String sql = statRecord.toString();

            String viewName =  "statistics_grouped_by_sessionandpid_"+modelFilter.getModel();
            boolean tableExists = DatabaseUtils.viewExists(connectionProvider.get(),viewName.toUpperCase());
            if (!tableExists) {
                JDBCUpdateTemplate updateTemplate = new JDBCUpdateTemplate(connectionProvider.get(), true);
                updateTemplate.setUseReturningKeys(false);
                updateTemplate
                    .executeUpdate(sql);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new StatisticsReportException(e);
        }
    }

    @Override
    public void processAccessLog(final ReportedAction repAction, final StatisticsReportSupport sup,
            final StatisticsFiltersContainer filters) throws StatisticsReportException {
        try {
            ModelFilter modelFilter = filters.getFilter(ModelFilter.class);
            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
            //IPAddressFilter ipAddrFilter = filters.getFilter(IPAddressFilter.class);
            UniqueIPAddressesFilter uniqueIPFilter = filters.getFilter(UniqueIPAddressesFilter.class);
            
            Boolean isUniqueSelected = uniqueIPFilter.getUniqueIPAddresses();
            final StringTemplate statRecord;
            
            if (isUniqueSelected == false) {
                statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectModelReport");
            }
            else {
               statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectModelReportUnique"); 
            }
            
            statRecord.setAttribute("model", modelFilter.getModel());
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging", false);
            
            statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
            statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
            statRecord.setAttribute("visibility", visFilter.asMap());
            
            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(dateFilter);
            String sql = statRecord.toString();
            new JDBCQueryTemplate<Map<String, Object>>(connectionProvider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                    Map<String, Object> val = new HashMap<String, Object>();
                    val.put(COUNT_KEY, rs.getInt("count"));
                    val.put(PID_KEY, rs.getString("pid"));
                    val.put(TITLE_KEY, rs.getString("title"));
                    val.put(MODEL_KEY, rs.getString("model"));


                    sup.processReportRecord(val);
                    returnsList.add(val);

                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql,params.toArray());
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new StatisticsReportException(e);
        }
    }

    

}
