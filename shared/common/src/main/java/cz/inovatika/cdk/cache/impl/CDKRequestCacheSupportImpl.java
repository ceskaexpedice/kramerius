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
import cz.incad.kramerius.cache.CacheStatistics;
import cz.incad.kramerius.database.provider.CDKCacheConnectionProvider;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
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

        String category = cacheCategory(url, pid, userIdentification);
        Connection connection = connectionFor(category);
        if (connection != null) {
            List<CDKRequestItem> items = new JDBCQueryTemplate<CDKRequestItem>(connection, true) {
                @Override
                public boolean handleRow(ResultSet rs, List<CDKRequestItem> returnsList) throws SQLException {
                    CDKRequestItem<?> cacheItem = CDKRequestItemFactory.createCacheItemFromResultSet(rs);
                    returnsList.add(cacheItem);
                    return true;
                }
            }.executeQuery(String.format("SELECT * FROM query_cache where  %s",wherePart), queryList.stream().map(Pair::getValue).toArray());
            recordLookup(items, pid == null && StringUtils.isAnyString(userIdentification));
            return items;
        } else {
            CacheStatistics.miss(pid == null && StringUtils.isAnyString(userIdentification)
                    ? CacheStatistics.CDK_USERS : CacheStatistics.CDK_ITEMS);
            return new ArrayList<>();
        }
    }

    private void recordLookup(List<CDKRequestItem> items, boolean userLookup) {
        String cacheName = userLookup ? CacheStatistics.CDK_USERS : CacheStatistics.CDK_ITEMS;
        if (items.isEmpty()) {
            CacheStatistics.miss(cacheName);
            return;
        }

        int maxAgeDays = userLookup
                ? KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.user.providedBy", 30)
                : KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.item", 30);
        if (items.get(0).isExpired(maxAgeDays)) {
            CacheStatistics.expired(cacheName);
            CacheStatistics.miss(cacheName);
        } else {
            CacheStatistics.hit(cacheName);
        }
    }

    @Override
    public JSONObject inspect(String cacheType, String dlAcronym, String url, String pid,
                              String userIdentification, int limit) {
        boolean usersCache = "cdkUsers".equals(cacheType);
        String dedicatedCategory = usersCache ? "users"
                : "cdkBibliomods".equals(cacheType) ? "bibliomods"
                : "cdkThumbs".equals(cacheType) ? "thumbs" : null;
        List<Pair<String, String>> filters = new ArrayList<>();
        String categoryCondition = usersCache
                ? PID_COLUMN + " IS NULL AND " + USER_IDENTIFICATION_COLUMN + " IS NOT NULL"
                : "cdkBibliomods".equals(cacheType)
                ? PID_COLUMN + " IS NOT NULL AND (" + URL_COLUMN + " ILIKE '%BIBLIO_MODS%' OR " + URL_COLUMN + " ILIKE '%/metadata/mods%')"
                : "cdkThumbs".equals(cacheType)
                ? PID_COLUMN + " IS NOT NULL AND (" + URL_COLUMN + " ILIKE '%/image/thumb%' OR " + URL_COLUMN + " ILIKE '%/streams/IMG_PREVIEW%')"
                : "(" + PID_COLUMN + " IS NOT NULL OR " + USER_IDENTIFICATION_COLUMN + " IS NULL)";
        addFilter(filters, SOURCE_LIBRARY_COLUMN, dlAcronym);
        addFilter(filters, URL_COLUMN, url);
        addFilter(filters, PID_COLUMN, pid);
        addFilter(filters, USER_IDENTIFICATION_COLUMN, userIdentification);

        List<String> conditions = new ArrayList<>();
        conditions.add(categoryCondition);
        filters.forEach(filter -> conditions.add(filter.getLeft() + " = ?"));
        String where = String.join(" AND ", conditions);
        Object[] parameters = filters.stream().map(Pair::getRight).toArray();

        JSONObject response = new JSONObject();
        response.put("cache", cacheType);
        response.put("limit", limit);
        JSONArray entries = new JSONArray();
        response.put("entries", entries);

        Connection connection = connectionFor(dedicatedCategory);
        if (connection == null) {
            response.put("available", false);
            response.put("matched", 0);
            response.put("truncated", false);
            return response;
        }

        response.put("available", true);
        try {
            // Keep the connection open for the following entries query. The
            // entries template owns and closes it after reading the rows.
            List<Long> counts = new JDBCQueryTemplate<Long>(connection, false) {
                @Override
                public boolean handleRow(ResultSet rs, List<Long> returnsList) throws SQLException {
                    returnsList.add(rs.getLong(1));
                    return true;
                }
            }.executeQuery("SELECT COUNT(*) FROM query_cache WHERE " + where, parameters);
            long matched = counts.isEmpty() ? 0 : counts.get(0);

            List<JSONObject> rows = new JDBCQueryTemplate<JSONObject>(connection, true) {
                @Override
                public boolean handleRow(ResultSet rs, List<JSONObject> returnsList) throws SQLException {
                    JSONObject key = new JSONObject();
                    key.put("id", rs.getString(ID_COLUMN));
                    key.put("source", rs.getString(SOURCE_LIBRARY_COLUMN));
                    key.put("url", rs.getString(URL_COLUMN));
                    key.put("pid", rs.getString(PID_COLUMN));
                    key.put("user", rs.getString(USER_IDENTIFICATION_COLUMN));
                    Timestamp createdAt = rs.getTimestamp(CREATED_AT_COLUMN);
                    if (createdAt != null) {
                        LocalDateTime created = createdAt.toLocalDateTime();
                        key.put("createdAt", created.toString());
                        int maxAgeDays = usersCache
                                ? KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.user.providedBy", 30)
                                : KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.item", 30);
                        key.put("expired", created.isBefore(LocalDateTime.now().minusDays(maxAgeDays)));
                    } else {
                        key.put("createdAt", JSONObject.NULL);
                        key.put("expired", false);
                    }
                    returnsList.add(key);
                    return true;
                }
            }.executeQuery("SELECT " + ID_COLUMN + ", " + SOURCE_LIBRARY_COLUMN + ", " + URL_COLUMN
                    + ", " + PID_COLUMN + ", " + USER_IDENTIFICATION_COLUMN + ", " + CREATED_AT_COLUMN
                    + " FROM query_cache WHERE " + where + " ORDER BY " + CREATED_AT_COLUMN
                    + " DESC LIMIT ?", appendLimit(parameters, limit));

            rows.forEach(entries::put);
            response.put("matched", matched);
            response.put("truncated", matched > entries.length());
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Cannot inspect CDK request cache", e);
            response.put("available", false);
            response.put("error", "Cache metadata query failed");
            response.put("matched", 0);
            response.put("truncated", false);
        }
        return response;
    }

    private static void addFilter(List<Pair<String, String>> filters, String column, String value) {
        if (StringUtils.isAnyString(value)) {
            filters.add(Pair.of(column, value));
        }
    }

    private static Object[] appendLimit(Object[] parameters, int limit) {
        Object[] result = java.util.Arrays.copyOf(parameters, parameters.length + 1);
        result[result.length - 1] = limit;
        return result;
    }



    @Override
    public void save(CDKRequestItem item)  {
        try {
            Connection con = connectionFor(cacheCategory(item.getUrl(), item.getPid(), item.getUserIdentification()));
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
            Connection connection = connectionFor(cacheCategory(item.getUrl(), item.getPid(), item.getUserIdentification()));
            if (connection != null) {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, true);
                String query = String.format("delete from query_cache where %s = ?",
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
        Map<String, List<String>> idsByCategory = new java.util.HashMap<>();
        for (CDKRequestItem item : items) {
            String category = cacheCategory(item.getUrl(), item.getPid(), item.getUserIdentification());
            idsByCategory.computeIfAbsent(category == null ? "" : category, ignored -> new ArrayList<>())
                    .add(item.getId());
        }
        idsByCategory.forEach((key, ids) -> {
            try {
                Connection connection = connectionFor(key.isEmpty() ? null : key);
                if (connection == null || ids.isEmpty()) {
                    return;
                }
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, true);
                String query = "delete from query_cache where " + ID_COLUMN + " in ("
                        + ids.stream().map(ignored -> "?").collect(Collectors.joining(",")) + ")";
                template.executeUpdate(query, ids.toArray());
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        });
    }

    @Override
    public void removeByPid(String pid) {
        List<String> categories = new ArrayList<>();
        categories.add(null);
        categories.add("bibliomods");
        categories.add("thumbs");
        for (String category : categories) {
            if (category != null && !CDKCacheConnectionProvider.hasDedicatedDatabase(category)) {
                continue;
            }
            try {
                Connection connection = connectionFor(category);
                if (connection == null) {
                    continue;
                }
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, true);
                String query = String.format("delete from query_cache where %s = ?",
                        PID_COLUMN
                );
                LOGGER.log(Level.FINE, String.format("PostgreSQL query %s", query));
                template.executeUpdate(query, pid);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private Connection connectionFor(String category) {
        return category == null ? this.connectionProvider.get()
                : CDKCacheConnectionProvider.getConnectionFor(category);
    }

    private static String cacheCategory(String url, String pid, String userIdentification) {
        if (pid == null && StringUtils.isAnyString(userIdentification)) {
            return "users";
        }
        String normalizedUrl = url == null ? "" : url.toLowerCase(java.util.Locale.ROOT);
        if (normalizedUrl.contains("biblio_mods") || normalizedUrl.contains("/metadata/mods")) {
            return "bibliomods";
        }
        if (normalizedUrl.contains("/image/thumb") || normalizedUrl.contains("/streams/img_preview")) {
            return "thumbs";
        }
        return null;
    }
}
