package cz.incad.kramerius.processes.database;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariDataSource;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Provides connection to kramerius4 database
 * @author pavels
 */
public class Kramerius4ConnectionProvider implements Provider<Connection> {

    private static DataSource dataSource = createDataSource();

    private static DataSource createDataSource(){
            HikariDataSource ds = new HikariDataSource();
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setJdbcUrl(KConfiguration.getInstance().getJdbcUrl());
            ds.setUsername(KConfiguration.getInstance().getJdbcUserName());
            ds.setPassword(KConfiguration.getInstance().getJdbcUserPass());
            ds.setLeakDetectionThreshold(KConfiguration.getInstance().getConfiguration().getInt("jdbcLeakDetectionThreshold"));
            ds.setMaximumPoolSize(KConfiguration.getInstance().getConfiguration().getInt("jdbcMaximumPoolSize"));
            ds.setConnectionTimeout(KConfiguration.getInstance().getConfiguration().getInt("jdbcConnectionTimeout"));
            ds.addDataSourceProperty("socketTimeout", "30");
            return ds;
    }

    @Inject
    public Kramerius4ConnectionProvider() {
        super();
    }

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Kramerius4ConnectionProvider.class.getName());

    @Override
    public Connection get() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);;
            return null;
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);;
            return null;
        }
    }

}
