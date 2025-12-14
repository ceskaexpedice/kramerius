package cz.incad.kramerius.processes.database;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariDataSource;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.logging.Level;

/**
 * User connection provider
 */
public class UsersConnectionProvider implements Provider<Connection> {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Kramerius4ConnectionProvider.class.getName());
    private static DataSource dataSource = createDataSource();

    private static DataSource createDataSource() {

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");

        /*
            userJdbcUrl=${jdbcUrl}
            userJdbcUserName=${jdbcUserName}
            userJdbcUserPass=${jdbcUserPass}
            userJdbcLeakDetectionThreshold=${jdbcLeakDetectionThreshold}
            userJdbcMaximumPoolSize=${jdbcMaximumPoolSize}
            userJdbcConnectionTimeout=${jdbcConnectionTimeout}
         */

        ds.setJdbcUrl(KConfiguration.getInstance().getConfiguration().getString("userJdbcUrl"));
        ds.setUsername(KConfiguration.getInstance().getConfiguration().getString("userJdbcUserName"));
        ds.setPassword(KConfiguration.getInstance().getConfiguration().getString("userJdbcUserPass"));
        ds.setLeakDetectionThreshold(KConfiguration.getInstance().getConfiguration().getInt("userJdbcLeakDetectionThreshold"));
        ds.setMaximumPoolSize(KConfiguration.getInstance().getConfiguration().getInt("userJdbcMaximumPoolSize"));

        ds.setConnectionTimeout(KConfiguration.getInstance().getConfiguration().getInt("userJdbcConnectionTimeout"));

        ds.setValidationTimeout(KConfiguration.getInstance().getConfiguration().getInt("userJdbcValidationTimeout",30000));
        ds.setIdleTimeout(KConfiguration.getInstance().getConfiguration().getInt("userJdbcIdleTimeout",600000));
        ds.setMaxLifetime(KConfiguration.getInstance().getConfiguration().getInt("userJdbcMaxLifetime",1800000));

        int datasourceSocketTimeout = KConfiguration.getInstance().getConfiguration().getInt("datasourceSocketTimeout",30);
        ds.addDataSourceProperty("socketTimeout", datasourceSocketTimeout);
        ds.setKeepaliveTime(120000);

        return ds;
    }

    @Inject
    public UsersConnectionProvider() {
        super();
    }

    @Override
    public Connection get() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setTransactionIsolation(KConfiguration.getInstance().getConfiguration().getInt("userJdbcDefaultTransactionIsolationLevel",Connection.TRANSACTION_READ_COMMITTED));  //reset the default level (Process Manager sets it to SERIALIZABLE)
            return connection;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalStateException("Cannot get database connection from the pool.", e);
        }
    }

}
