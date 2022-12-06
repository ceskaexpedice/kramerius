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

    private DataSource dataSource = null;

    @Inject
    public Kramerius4ConnectionProvider() {
        super();
        if (dataSource == null) {// lazy setup of datasource
            HikariDataSource ds = new HikariDataSource();
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setJdbcUrl(KConfiguration.getInstance().getJdbcUrl());
            ds.setUsername(KConfiguration.getInstance().getJdbcUserName());
            ds.setPassword(KConfiguration.getInstance().getJdbcUserPass());
            ds.setLeakDetectionThreshold(0);
            ds.setMaximumPoolSize(10);
            ds.setConnectionTimeout(30000);
            dataSource = ds;
        }

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
