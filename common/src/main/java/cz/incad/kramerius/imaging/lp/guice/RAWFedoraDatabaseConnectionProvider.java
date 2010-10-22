package cz.incad.kramerius.imaging.lp.guice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.google.inject.Provider;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class RAWFedoraDatabaseConnectionProvider implements Provider<Connection>{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger
	    .getLogger(RAWFedoraDatabaseConnectionProvider.class.getName());
    
    @Override
    public Connection get() {
	try {
	    Class.forName("org.postgresql.Driver");
	    String urlCon = KConfiguration.getInstance().getConfiguration().getString("fedora3.connectionURL");
	    String user = KConfiguration.getInstance().getConfiguration().getString("fedora3.connectionUser");
	    String pass = KConfiguration.getInstance().getConfiguration().getString("fedora3.connectionPass");
	    Connection con = DriverManager.getConnection(urlCon,user,pass);
	    return con;
	} catch (ClassNotFoundException e) {
	    LOGGER.severe(e.getMessage());
	    throw new RuntimeException(e);
	} catch (SQLException e) {
	    LOGGER.severe(e.getMessage());
	    throw new RuntimeException(e);
	}
    }
    
}
