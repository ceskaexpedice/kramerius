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
import cz.incad.kramerius.statistics.filters.PidsFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.filters.UniqueIPAddressesFilter;
import cz.incad.kramerius.statistics.filters.VisibilityFilter;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import cz.incad.kramerius.utils.database.Offset;
import java.util.logging.Logger;

public class PidsReport implements StatisticReport {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PidsReport.class.getName());

    public static final String REPORT_ID = "pids";

    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    
    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction repAction, StatisticsFiltersContainer filters,
            Offset rOffset) throws StatisticsReportException {
            try {
                DateFilter dateFilter = filters.getFilter(DateFilter.class);
                VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
                UniqueIPAddressesFilter uniqueIPFilter = filters.getFilter(UniqueIPAddressesFilter.class);
                IPAddressFilter ipFilter = filters.getFilter(IPAddressFilter.class);             

                Boolean isUniqueSelected = uniqueIPFilter.getUniqueIPAddresses();
                final StringTemplate statRecord;
            
                if (isUniqueSelected == false) {
                    statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                            .getInstanceOf("selectPidsReport");
                }
                else {
                    statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                            .getInstanceOf("selectPidsReportUnique"); 
                }
                
                statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
                statRecord.setAttribute("paging", true);
                statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
                statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
                statRecord.setAttribute("visibility", visFilter.asMap());
                statRecord.setAttribute("ipaddr", ipFilter.getIpAddress());
                
                @SuppressWarnings("rawtypes")
                List params = StatisticUtils.jdbcParams(dateFilter, rOffset);
                String sql = statRecord.toString();
                Connection conn = connectionProvider.get();

                List<Map<String, Object>> pids = new JDBCQueryTemplate<Map<String, Object>>(conn) {
                    @Override
                    public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                        Map<String, Object> map = new HashMap<>();
                        map.put(COUNT_KEY, rs.getInt("count"));
                        map.put(PID_KEY, rs.getString("pid"));
                        returnsList.add(map);
                        return super.handleRow(rs, returnsList);
                    }
                }.executeQuery(sql, params.toArray());
                conn.close();
                return pids;
            } catch (ParseException e) {
               LOGGER.log(Level.SEVERE, e.getMessage(), e);
               return new ArrayList<Map<String, Object>>();
            } catch (SQLException ex) {
                Logger.getLogger(PidsReport.class.getName()).log(Level.SEVERE, null, ex);
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
    public void prepareViews(ReportedAction action, StatisticsFiltersContainer filters) throws StatisticsReportException {
        try {
            IPAddressFilter ipFilter = filters.getFilter(IPAddressFilter.class);
            PidsFilter pidsFilter = filters.getFilter(PidsFilter.class);
            
            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("preparePidsView");
            statRecord.setAttribute("action", action != null ? action.name() : null);
            statRecord.setAttribute("paging", false);
            
            statRecord.setAttribute("pids", pidsFilter.getPids().split(","));
            statRecord.setAttribute("ipaddr", ipFilter.getIpAddress());
            
            String sql = statRecord.toString();
            Connection conn = connectionProvider.get();
            
            String viewName =  "statistics_grouped_by_sessionandpid_pid";
            boolean tableExists = DatabaseUtils.viewExists(conn, viewName.toUpperCase());
            if (!tableExists) {
                JDBCUpdateTemplate updateTemplate = new JDBCUpdateTemplate(conn, true);
                updateTemplate.setUseReturningKeys(false);
                updateTemplate
                    .executeUpdate(sql);
            }
            // if table exists; we have to close connection manually
            if (!conn.isClosed()) {
                conn.close();
            }
            LOGGER.fine(String.format("Test statistics connection.isClosed() : %b", conn.isClosed()));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new StatisticsReportException(e);
        }
    }


    @Override
    public void processAccessLog(ReportedAction repAction, StatisticsReportSupport sup,
            StatisticsFiltersContainer filters) throws StatisticsReportException {
        try {
            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
            UniqueIPAddressesFilter uniqueIPFilter = filters.getFilter(UniqueIPAddressesFilter.class);
            IPAddressFilter ipFilter = filters.getFilter(IPAddressFilter.class);
            
            Boolean isUniqueSelected = uniqueIPFilter.getUniqueIPAddresses();
            final StringTemplate statRecord;
            
            if (isUniqueSelected == false) {
                statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                .getInstanceOf("selectPidsReport");
            }
            else {
                statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                .getInstanceOf("selectPidsReportUnique"); 
            }
            
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging", false);
            statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
            statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
            statRecord.setAttribute("visibility", visFilter.asMap());
            statRecord.setAttribute("ipaddr", ipFilter.getIpAddress());
            
            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(dateFilter);
            String sql = statRecord.toString();
            Connection conn = connectionProvider.get();
            
            new JDBCQueryTemplate<Map<String,Object>>(conn) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                    Map<String, Object> map = new HashMap<>();
                    map.put(COUNT_KEY, rs.getInt("count"));
                    map.put(PID_KEY, rs.getString("pid"));
                    map.put(TITLE_KEY, rs.getString("title"));
                    sup.processReportRecord(map);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql, params.toArray());
            //conn.close();
            LOGGER.fine(String.format("Test statistics connection.isClosed() : %b", conn.isClosed()));
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (SQLException ex) {
            Logger.getLogger(PidsReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
