package cz.incad.kramerius.processes.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class StandardConnectionProvider implements Provider<Connection>{

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(StandardConnectionProvider.class.getName());
	
	
	private String url;
	private String userName;
	private String userPass;
	
	
	
	@Inject
	public StandardConnectionProvider(KConfiguration configuration) {
		super();
		this.url = configuration.getJdbcUrl();
		this.userName = configuration.getJdbcUserName();
		this.userPass = configuration.getJdbcUserPass();
	}



	@Override
	public Connection get() {
		try {
			Class.forName("org.postgresql.Driver");
			Connection conn = DriverManager.getConnection(this.url, this.userName, this.userPass);
			conn.setAutoCommit(true);
			return conn;
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}
}
