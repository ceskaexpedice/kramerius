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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticReportOffset;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

/**
 * @author pavels
 *
 */
public class AuthorReport implements StatisticReport{
    
    public static final String REPORT_ID = "author";
    
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    
    @Override
    public List<Map<String, Object>> getReportPage(StatisticReportOffset reportOffset) {
        
        
        final StringTemplate authors = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectAuthorReport");
        String sql = authors.toString();
        List<Map<String,Object>> auths = new JDBCQueryTemplate<Map<String,Object>>(connectionProvider.get()) {

            @Override
            public boolean handleRow(ResultSet rs, List<Map<String,Object>> returnsList) throws SQLException {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("count", rs.getInt("count"));
                map.put("author_name", rs.getString("author_name"));
                returnsList.add(map);
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(sql.toString(), reportOffset.getOffset(), reportOffset.getSize());
        
        return auths;
    }

    @Override
    public List<String> getOptionalValues() {
        return new ArrayList<String>();
    }

    @Override
    public String getReportId() {
        return REPORT_ID;
    }
}
