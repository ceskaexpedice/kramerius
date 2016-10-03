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

import cz.incad.kramerius.statistics.DateFilter;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.Offset;

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
    public List<Map<String, Object>> getReportPage(ReportedAction repAction, DateFilter filter, Offset rOffset,
            Object filteringValue) {
        try {
            // filtered value
            // String[] splitted = filteringValue.toString().split("-");

            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup
                    .getInstanceOf("selectModelReport");
            statRecord.setAttribute("model", filteringValue);
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging", true);
            statRecord.setAttribute("fromDefined", filter.getFromDate() != null);
            statRecord.setAttribute("toDefined", filter.getToDate() != null);

            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(filter, rOffset);
            String sql = statRecord.toString();
            List<Map<String, Object>> returns = new JDBCQueryTemplate<Map<String, Object>>(connectionProvider.get()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
                    Map<String, Object> val = new HashMap<String, Object>();
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
    public void processAccessLog(final ReportedAction repAction, final DateFilter filter, final StatisticsReportSupport sup,
            Object filteringValue, Object... args) throws StatisticsReportException {
        try {
            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectModelReport");
            statRecord.setAttribute("model", filteringValue);
            statRecord.setAttribute("action", repAction != null ? repAction.name() : null);
            statRecord.setAttribute("paging", false);
            
            statRecord.setAttribute("fromDefined", filter.getFromDate() != null);
            statRecord.setAttribute("toDefined", filter.getToDate() != null);

            @SuppressWarnings("rawtypes")
            List params = StatisticUtils.jdbcParams(filter);
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
        }

    }
}
