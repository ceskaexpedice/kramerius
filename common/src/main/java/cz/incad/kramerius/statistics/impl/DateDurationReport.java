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
            
            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectDateDurationReport");
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging",true);
            String sql = statRecord.toString();
            
            List<Map<String,Object>> vals = new JDBCQueryTemplate<Map<String,Object>>(connectionProvider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                    Map<String, Object> map = createMap(rs);
                    returnsList.add(map);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql, new Timestamp(FORMAT.parse(splitted[0]).getTime()), new Timestamp(FORMAT.parse(splitted[1]).getTime()) , reportOffset.getOffset(), reportOffset.getSize());
            
            return vals;
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


    private Map<String, Object> createMap(ResultSet rs) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pid",rs.getString("pid"));
        map.put("count",new Integer(rs.getInt("count")));
        map.put("record_id",new Integer(rs.getInt("record_id")));
        map.put("title",rs.getString("title"));
        map.put("model",rs.getString("model"));
        map.put("rights",rs.getString("rights"));
        map.put("lang",rs.getString("lang"));
        return map;
    }

    @Override
    public void processAccessLog(ReportedAction repAction, final StatisticsReportSupport sup, Object filteringValue, Object... args) throws StatisticsReportException {
        try {
            String[] splitted = filteringValue.toString().split("-");
            
            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectDateDurationReport");
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging", false);
            String sql = statRecord.toString();
            
            new JDBCQueryTemplate<Map<String,Object>>(connectionProvider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                    Map<String, Object> map = createMap(rs);
                    returnsList.add(map);
                    sup.processReportRecord(map);
                    
                    return super.handleRow(rs, returnsList);
                }

            }.executeQuery(sql, new Timestamp(FORMAT.parse(splitted[0]).getTime()), new Timestamp(FORMAT.parse(splitted[1]).getTime()));
            
        } catch (ParseException e) {
            throw new StatisticsReportException(e);
        }
    }
}
