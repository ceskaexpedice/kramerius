package cz.incad.kramerius.processes.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * PRovides connection from datasource defined in context.xml
 * @author pavels
 */
public abstract class JNDIConnectionProvider implements Provider<Connection> {
    

	
}
