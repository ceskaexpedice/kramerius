/*
 * Copyright (C) 2025  Inovatika
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
package cz.inovatika.cdk.cache.impl;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static cz.inovatika.cdk.cache.CDKRequestItem.*;

public class CDKRequestCacheSupportImpl implements CDKRequestCacheSupport  {

    public static final Logger LOGGER = Logger.getLogger(CDKRequestCacheSupportImpl.class.getName());

    @Inject
    @Named("cdk/cache")
    Provider<Connection> connectionProvider;



    @Override
    public List<CDKRequestItem> find(String dlAcronym, String url, String pid, String userIdentification) {
        List<Pair<String,String>> queryList = new ArrayList<>();
        if (StringUtils.isAnyString(dlAcronym)) {
            queryList.add(Pair.of(SOURCE_LIBRARY_COLUMN, dlAcronym));
        }
        if (StringUtils.isAnyString(url)) {
            queryList.add(Pair.of(URL_COLUMN, url));
        }
        if (StringUtils.isAnyString(pid)) {
            queryList.add(Pair.of(PID_COLUMN, pid));
        }
        if (StringUtils.isAnyString(userIdentification)) {
            queryList.add(Pair.of(USER_IDENTIFICATION_COLUMN, StringUtils.isAnyString(userIdentification)? userIdentification : COMMON_USER));
        }

        String wherePart = queryList.stream().map(p-> {
            return p.getKey() +" = ?";
        }).collect(Collectors.joining(" AND "));

        Connection connection = this.connectionProvider.get();
        if (connection != null) {
            List<CDKRequestItem> items = new JDBCQueryTemplate<CDKRequestItem>(connection, true) {
                @Override
                public boolean handleRow(ResultSet rs, List<CDKRequestItem> returnsList) throws SQLException {
                    CDKRequestItem<?> cacheItem = CDKRequestItemFactory.createCacheItemFromResultSet(rs);
                    returnsList.add(cacheItem);
                    return true;
                }
            }.executeQuery(String.format("SELECT * FROM query_cache where  %s",wherePart), queryList.stream().map(Pair::getValue).toArray());
            return items;
        } else return new ArrayList<>();
    }



    @Override
    public void save(CDKRequestItem item)  {
        try {
            Connection con = this.connectionProvider.get();
            if (con != null) {
                item.save(con);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public void remove(CDKRequestItem item) {
        try {
            Connection connection = this.connectionProvider.get();
            if (connection != null) {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, true);
                String query = String.format("delete from query_cache where %d = ?",
                        ID_COLUMN
                );
                LOGGER.log(Level.FINE, String.format("PostgreSQL query %s", query));
                template.executeUpdate(query, item.getId());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }

    }


    @Override
    public void remove(List<CDKRequestItem> items) {
        try {
            Connection connection = this.connectionProvider.get();
            if (connection != null) {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, true);
                String condition = "("+items.stream().map(CDKRequestItem::getId).map(it-> {
                    return "'"+it+"'";
                }).collect(Collectors.joining(","))+")";
                String query = String.format("delete from query_cache where %s in %s",
                        ID_COLUMN, condition
                );
                LOGGER.log(Level.FINE, String.format("PostgreSQL query %s", query));
                template.executeUpdate(query);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public void removeByPid(String pid) {
        try {
            Connection connection = this.connectionProvider.get();
            if (connection != null) {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, true);
                String query = String.format("delete from query_cache where %s = ?",
                        PID_COLUMN
                );
                LOGGER.log(Level.FINE, String.format("PostgreSQL query %s", query));
                template.executeUpdate(query, pid);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }
}
