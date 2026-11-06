/*
 * Copyright (C) 2025  Inovatika
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package cz.incad.kramerius.database.provider;

import com.google.inject.Provider;
import com.zaxxer.hikari.HikariDataSource;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/** Provides connections for the shared and optional dedicated CDK cache databases. */
public class CDKCacheConnectionProvider implements Provider<Connection> {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(CDKCacheConnectionProvider.class.getName());
    private static final String[] DEDICATED_CACHES = {"users", "bibliomods", "thumbs"};
    private static final Map<String, DataSource> DATA_SOURCES = new HashMap<>();

    @Override
    public Connection get() {
        return getConnectionFor(null);
    }

    /**
     * Returns a connection for a cache category. If that category has no
     * dedicated JDBC URL configured, the legacy shared CDK cache is used.
     */
    public static Connection getConnectionFor(String category) {
        String prefix = category == null ? "cdk.cache" : "cdk.cache." + category;
        DataSource dataSource;
        synchronized (DATA_SOURCES) {
            if (category != null && !hasDedicatedDatabase(category)) {
                dataSource = getDataSource("cdk.cache");
            } else {
                dataSource = getDataSource(prefix);
            }
        }

        if (dataSource == null) {
            return null;
        }
        try {
            Connection connection = dataSource.getConnection();
            connection.setTransactionIsolation(KConfiguration.getInstance().getConfiguration()
                    .getInt("cdk.cache.jdbcDefaultTransactionIsolationLevel", Connection.TRANSACTION_READ_COMMITTED));
            return connection;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalStateException("Cannot get database connection from the pool for " + prefix + ".", e);
        }
    }

    public static boolean hasDedicatedDatabase(String category) {
        if (category == null || !java.util.Arrays.asList(DEDICATED_CACHES).contains(category)) {
            return false;
        }
        return StringUtils.isAnyString(KConfiguration.getInstance().getConfiguration()
                .getString("cdk.cache." + category + ".jdbcUrl"));
    }

    /** Opens a connection only when a dedicated category database is configured. */
    public static Connection getDedicatedConnection(String category) {
        return hasDedicatedDatabase(category) ? getConnectionFor(category) : null;
    }

    /** Returns the configured category names, for startup schema initialization. */
    public static String[] dedicatedCategories() {
        return DEDICATED_CACHES.clone();
    }

    private static DataSource getDataSource(String prefix) {
        if (DATA_SOURCES.containsKey(prefix)) {
            return DATA_SOURCES.get(prefix);
        }
        DataSource dataSource = createDataSource(prefix);
        DATA_SOURCES.put(prefix, dataSource);
        return dataSource;
    }

    private static DataSource createDataSource(String prefix) {
        String jdbcUrl = KConfiguration.getInstance().getConfiguration().getString(prefix + ".jdbcUrl");
        String username = KConfiguration.getInstance().getConfiguration().getString(prefix + ".jdbcUserName");
        String password = KConfiguration.getInstance().getConfiguration().getString(prefix + ".jdbcUserPass");

        if (!StringUtils.isAnyString(jdbcUrl)) {
            return null;
        }
        if (!StringUtils.isAnyString(username) || !StringUtils.isAnyString(password)) {
            LOGGER.warning("CDK cache database URL is configured for " + prefix
                    + " but its jdbcUserName or jdbcUserPass is missing.");
            return null;
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setLeakDetectionThreshold(KConfiguration.getInstance().getConfiguration()
                .getLong(prefix + ".jdbcLeakDetectionThreshold", 0L));
        ds.setMaximumPoolSize(KConfiguration.getInstance().getConfiguration()
                .getInt(prefix + ".jdbcMaximumPoolSize", 20));
        ds.setConnectionTimeout(KConfiguration.getInstance().getConfiguration()
                .getLong(prefix + ".jdbcConnectionTimeout", 30000L));
        ds.setValidationTimeout(KConfiguration.getInstance().getConfiguration()
                .getLong(prefix + ".jdbcValidationTimeout", 30000L));
        ds.setIdleTimeout(KConfiguration.getInstance().getConfiguration()
                .getLong(prefix + ".jdbcIdleTimeout", 600000L));
        ds.setMaxLifetime(KConfiguration.getInstance().getConfiguration()
                .getLong(prefix + ".jdbcMaxLifetime", 1800000L));
        int socketTimeout = KConfiguration.getInstance().getConfiguration().getInt("datasourceSocketTimeout", 30);
        ds.addDataSourceProperty("socketTimeout", socketTimeout);
        ds.setKeepaliveTime(120000L);
        return ds;
    }
}
