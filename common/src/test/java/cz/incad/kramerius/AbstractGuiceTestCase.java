package cz.incad.kramerius;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public abstract class AbstractGuiceTestCase {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(AbstractGuiceTestCase.class.getName());
	
	
	protected void dropTables() throws IOException, SQLException {
	    Connection con = connection();
	    try {
	        if (DatabaseUtils.tableExists(con,"PROCESSES")) {
	            new JDBCUpdateTemplate(con,false){
                    @Override
                    public PreparedStatement createPreparedStatement(Connection con, String sql) throws SQLException {
                        return con.prepareStatement(sql);
                    }
	            }.executeUpdate("drop table PROCESSES");
	        }
	        if (DatabaseUtils.tableExists(con,"DESIRABLE")) {
	            new JDBCUpdateTemplate(con,false){
                    @Override
                    public PreparedStatement createPreparedStatement(Connection con, String sql) throws SQLException {
                        return con.prepareStatement(sql);
                    }
	                
	            }.executeUpdate("drop table DESIRABLE");
	        }
	        if (DatabaseUtils.tableExists(con,"USER_ENTITY")) {
	            InputStream is = this.getClass().getClassLoader().getResourceAsStream("cz/incad/kramerius/security/database/res/droptablessecdb.sql");
	            String sqls = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
	            String[] splitted = sqls.split("\n");
	            for (String oneSql : splitted) {
	                new JDBCUpdateTemplate(con,false){
	                    @Override
	                    public PreparedStatement createPreparedStatement(Connection con, String sql) throws SQLException {
	                        return con.prepareStatement(sql);
	                    }
	                }.executeUpdate(oneSql);
                }
	            
	        }
	    } finally {
	        DatabaseUtils.tryClose(con);
	    }
	}

	public Connection connection() {
		Injector inj = injector();
		Provider<Connection> kramerius4Provider = inj.getProvider(Key.get(Connection.class, Names.named("kramerius4")));
		Connection connection = kramerius4Provider.get();
		return connection;
	}

	protected abstract Injector injector();
}
