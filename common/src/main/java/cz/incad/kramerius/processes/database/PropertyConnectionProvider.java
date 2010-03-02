package cz.incad.kramerius.processes.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.internal.Nullable;
import com.google.inject.name.Named;

public class PropertyConnectionProvider implements Provider<Connection>{
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(PropertyConnectionProvider.class.getName());
	
	public static final String JDBC_URL = "jdbcUrl";
	public static final String JDBC_USER_NAME = "jdbcUserName";
	public static final String JDBC_USER_PASS = "jdbcUserPass";

	
	private String jdbcUserPass;// = System.getProperty("jdbcUserPass");
	private String jdbcUserName;// = System.getProperty("jdbcUserName");
	private String jdbcUserUrl;// = System.getProperty("jdbcUrl");

	@Inject
	public PropertyConnectionProvider(@Named("jdbcUrl")String url, @Named("jdbcUserName")String userName,
			@Named("jdbcUserPass")String userPass) {
		super();
		this.jdbcUserUrl = url;
		this.jdbcUserName = userName;
		this.jdbcUserPass = userPass;
		System.out.println("TEST");
	}




	@Override
	public Connection get() {
		try {
			Class.forName("org.postgresql.Driver");
			Connection conn = DriverManager.getConnection(this.jdbcUserUrl, this.jdbcUserName, this.jdbcUserPass);
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
