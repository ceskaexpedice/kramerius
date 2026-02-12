package cz.incad.kramerius.uiconfig;

import com.google.inject.Provider;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * DbConnectionProvider
 *
 * @author ppodsednik
 */
public class DbConnectionProvider implements Provider<Connection> {

    private DataSource dataSource;

    public DbConnectionProvider(Properties config) {
        dataSource = createDataSource(config);
    }

    private static DataSource createDataSource(Properties config) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl(config.getProperty("JDBC_URL"));
        ds.setUsername(config.getProperty("JDBC_USERNAME"));
        ds.setPassword(config.getProperty("JDBC_PASSWORD"));
        return ds;
    }

    @Override
    public Connection get() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot get database connection from the pool.", e);
        }
    }

    public void close() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }
}
