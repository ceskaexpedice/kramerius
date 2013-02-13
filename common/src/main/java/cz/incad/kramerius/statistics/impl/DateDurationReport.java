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
import java.util.Set;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticReportOffset;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

/**
 * @author pavels
 *
 */
public class DateDurationReport implements StatisticReport{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DateDurationReport.class.getName());
    
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    
    public static final String REPORT_ID="dates";
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction repAction, StatisticReportOffset reportOffset, Object filteringValue) {
        try {
            
            String[] splitted = filteringValue.toString().split("-");
            
            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectDateDurationReport_1");
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging",true);
            String sql = statRecord.toString();
            
            List<Map<String,Object>> vals = new JDBCQueryTemplate<Map<String,Object>>(connectionProvider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("pid",rs.getString("pid"));
                    map.put("count",new Integer(rs.getInt("count")));
                    returnsList.add(map);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql, new Timestamp(FORMAT.parse(splitted[0]).getTime()), new Timestamp(FORMAT.parse(splitted[1]).getTime()) , reportOffset.getOffset(), reportOffset.getSize());
            
            List<String> pids = new ArrayList<String>(); {
                for (Map<String, Object> map : vals) {
                    pids.add((String)map.get("pid"));
                }
            }
            
            final StringTemplate detailRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectDateDurationReport_2");
            detailRecord.setAttribute("pids", pids);
            
            List<Map<String,Object>> details = new JDBCQueryTemplate<Map<String, Object>>(connectionProvider.get()) {

                @Override
                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                    Map<String, Object> val = new HashMap<String, Object>();
                    val.put(PID_KEY, rs.getString("pid"));
                    val.put(TITLE_KEY, rs.getString("title"));
                    val.put(MODEL_KEY, rs.getString("model"));
                    returnsList.add(val);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(detailRecord.toString());

            
            return merge(vals, details);
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<Map<String,Object>>();
        }
    }

    /**
     * @param vals
     * @param details
     * @return
     */
    private List<Map<String, Object>> merge(List<Map<String, Object>> vals, List<Map<String, Object>> details) {
        List<Map<String, Object>> l = new ArrayList<Map<String,Object>>();
        for (Map<String, Object> map : vals) {
            Object pid = map.get("pid");
            Map<String, Object> detail = find(pid.toString(), details);
            if (detail != null) {
                Set<String> keySet = detail.keySet();
                for (String key : keySet) {
                    map.put(key, detail.get(key));
                }
            }
            l.add(map);
        }
        return l;
    }
    
    public Map<String, Object> find(String pid, List<Map<String, Object>> in) {
        for (Map<String, Object> map : in) {
            if (map.get("pid").equals(pid)) return map;
        }
        return null;
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
    public void processAccessLog(ReportedAction repAction, StatisticsReportSupport sup, Object filteringValue, Object... args) throws StatisticsReportException {
        try {
            String[] splitted = filteringValue.toString().split("-");
            
            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectDateDurationReport_1");
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging", false);
            String sql = statRecord.toString();
            
            List<Map<String,Object>> vals = new JDBCQueryTemplate<Map<String,Object>>(connectionProvider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("pid",rs.getString("pid"));
                    map.put("count",new Integer(rs.getInt("count")));
                    returnsList.add(map);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql, new Timestamp(FORMAT.parse(splitted[0]).getTime()), new Timestamp(FORMAT.parse(splitted[1]).getTime()));
            
            List<String> pids = new ArrayList<String>(); {
                for (Map<String, Object> map : vals) {
                    pids.add((String)map.get("pid"));
                }
            }
            
            final StringTemplate detailRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectDateDurationReport_2");
            detailRecord.setAttribute("pids", pids);
            
            List<Map<String,Object>> details = new JDBCQueryTemplate<Map<String, Object>>(connectionProvider.get()) {

                @Override
                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                    Map<String, Object> val = new HashMap<String, Object>();
                    val.put(PID_KEY, rs.getString("pid"));
                    val.put(TITLE_KEY, rs.getString("title"));
                    val.put(MODEL_KEY, rs.getString("model"));
                    returnsList.add(val);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(detailRecord.toString());

            List<Map<String, Object>> merged = merge(vals, details);
            for (Map<String, Object> m : merged) {
                sup.processReportRecord(m);
            }
            
        } catch (ParseException e) {
            throw new StatisticsReportException(e);
        }
    }
}
