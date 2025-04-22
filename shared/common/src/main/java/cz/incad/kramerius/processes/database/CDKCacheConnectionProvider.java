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
package cz.incad.kramerius.processes.database;

import com.google.inject.Provider;
import com.zaxxer.hikari.HikariDataSource;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.logging.Level;

public class CDKCacheConnectionProvider implements Provider<Connection> {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(CDKCacheConnectionProvider.class.getName());

    private static DataSource dataSource = createDataSource();

    private static DataSource createDataSource() {

        String cacheJDBCUrl = KConfiguration.getInstance().getConfiguration().getString("cdk.cache.jdbcUrl");
        String cacheJDBCUserName =  KConfiguration.getInstance().getConfiguration().getString( "cdk.cache.jdbcUserName");
        String cacheJDBCPass =  KConfiguration.getInstance().getConfiguration().getString("cdk.cache.jdbcUserPass");

        if (StringUtils.isAnyString(cacheJDBCUrl) &&
            StringUtils.isAnyString(cacheJDBCUserName) &&
                StringUtils.isAnyString(cacheJDBCPass)) {

            HikariDataSource ds = new HikariDataSource();
            ds.setDriverClassName("org.postgresql.Driver");

//            jdbcUrl=jdbc:postgresql://localhost/kramerius4
//            jdbcUserName=fedoraAdmin
//            jdbcUserPass=fedoraAdmin
//            jdbcLeakDetectionThreshold=0
//            jdbcMaximumPoolSize=20
//            jdbcConnectionTimeout=30000


            ds.setJdbcUrl( KConfiguration.getInstance().getConfiguration().getString("cdk.cache.jdbcUrl"));
            ds.setUsername(KConfiguration.getInstance().getConfiguration().getString( "cdk.cache.jdbcUserName"));
            ds.setPassword( KConfiguration.getInstance().getConfiguration().getString("cdk.cache.jdbcUserPass"));

            ds.setLeakDetectionThreshold(KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.jdbcLeakDetectionThreshold", 0));
            ds.setMaximumPoolSize(KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.jdbcMaximumPoolSize",20));

            ds.setConnectionTimeout(KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.jdbcConnectionTimeout", 30000));

            ds.setValidationTimeout(KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.jdbcValidationTimeout",30000));
            ds.setIdleTimeout(KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.jdbcIdleTimeout",600000));
            ds.setMaxLifetime(KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.jdbcMaxLifetime",1800000));

            int datasourceSocketTimeout = KConfiguration.getInstance().getConfiguration().getInt("datasourceSocketTimeout",30);
            ds.addDataSourceProperty("socketTimeout", datasourceSocketTimeout);
            ds.setKeepaliveTime(120000);

            return ds;
        } else {
            // no settings; no datasource (it is only optional)
            return null;
        }

    }


    @Override
    public Connection get() {
        if (dataSource != null) {
            try {
                Connection connection = dataSource.getConnection();
                connection.setTransactionIsolation(KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.jdbcDefaultTransactionIsolationLevel",Connection.TRANSACTION_READ_COMMITTED));
                return connection;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new IllegalStateException("Cannot get database connection from the pool.", e);
            }
        } else return null;
    }

}
