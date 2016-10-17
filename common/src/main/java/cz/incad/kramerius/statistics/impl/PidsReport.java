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

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.Offset;

public class PidsReport implements StatisticReport {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PidsReport.class.getName());

    public static final String REPORT_ID = "pids";

    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    
    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction repAction, StatisticsFiltersContainer filters,
            Offset rOffset) throws StatisticsReportException {
//        try {
//            String[] pids = filteringValue.toString().split(",");
//
//            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectPidReport");
//            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
//            statRecord.setAttribute("paging", true);
//            statRecord.setAttribute("pids", pids);
//            statRecord.setAttribute("fromDefined", filter.getFromDate() != null);
//            statRecord.setAttribute("toDefined", filter.getToDate() != null);
//
//            @SuppressWarnings("rawtypes")
//            List params = StatisticUtils.jdbcParams(filter, rOffset);
//            String sql = statRecord.toString();
//            List<Map<String, Object>> vals = new JDBCQueryTemplate<Map<String, Object>>(connectionProvider.get()) {
//                @Override
//                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
//                    Map<String, Object> map = createMap(rs);
//                    returnsList.add(map);
//                    return super.handleRow(rs, returnsList);
//                }
//            }.executeQuery(sql, params.toArray());
//
//            return vals;
//        } catch (ParseException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//            return new ArrayList<Map<String, Object>>();
//        }
      return new ArrayList<Map<String, Object>>();

    }






    




    @Override
    public List<String> getOptionalValues() {
        return new ArrayList<String>();
    }

    @Override
    public String getReportId() {
        return REPORT_ID;
    }

    private Map<String, Object> createMap(ResultSet rs) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pid", rs.getString("pid"));
        map.put("count", new Integer(rs.getInt("count")));
        map.put("title", rs.getString("title"));
        map.put("model", rs.getString("model"));
        map.put("rights", rs.getString("rights"));
        return map;
    }

    
    @Override
    public void prepareViews(ReportedAction action, StatisticsFiltersContainer container)
            throws StatisticsReportException {
        // TODO Auto-generated method stub
        
    }






    @Override
    public void processAccessLog(ReportedAction repAction, StatisticsReportSupport sup,
            StatisticsFiltersContainer filters) throws StatisticsReportException {
//        try {
//            String[] pids = filteringValue.toString().split(",");
//
//            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectPidReport");
//            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
//            statRecord.setAttribute("paging", false);
//            statRecord.setAttribute("pids", pids);
//            statRecord.setAttribute("fromDefined", filter.getFromDate() != null);
//            statRecord.setAttribute("toDefined", filter.getToDate() != null);
//
//            @SuppressWarnings({ "rawtypes"})
//            List params = StatisticUtils.jdbcParams(filter);
//            String sql = statRecord.toString();
//
//            new JDBCQueryTemplate<Map<String, Object>>(connectionProvider.get()) {
//                @Override
//                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
//                    Map<String, Object> map = createMap(rs);
//                    returnsList.add(map);
//                    sup.processReportRecord(map);
//
//                    return super.handleRow(rs, returnsList);
//                }
//
//            }.executeQuery(sql,params);
//        } catch (ParseException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//        }
        
    }





}
