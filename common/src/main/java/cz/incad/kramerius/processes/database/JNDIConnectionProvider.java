package cz.incad.kramerius.processes.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.google.inject.Provider;

public class JNDIConnectionProvider implements Provider<Connection>{
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(JNDIConnectionProvider.class.getName());
	
	@Override
	public Connection get() {
		try {
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("_NO_BINDING_");
			return ds.getConnection();
		} catch (NamingException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);;
			return null;
		}
	}
	
}
