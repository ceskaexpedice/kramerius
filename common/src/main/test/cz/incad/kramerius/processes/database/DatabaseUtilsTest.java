package cz.incad.kramerius.processes.database;


import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.net.ssl.SSLEngineResult.Status;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;



import cz.incad.kramerius.processes.AbstractGuiceTestCase;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.impl.LRProcessDefinitionImpl;
import cz.incad.kramerius.processes.os.impl.unix.UnixLRProcessImpl;

public class DatabaseUtilsTest extends AbstractGuiceTestCase {

	@Before
	public void doBefore() {
	}
	
	@After
	public void doAfter() {
		dropTables();
	}

	@Test
	public void tableExistsAndCreateTableTest() throws SQLException {
		Connection connection = connection();
		System.out.println("Connection "+System.identityHashCode(connection));
		boolean firstAssert = false;
		try {
			firstAssert = DatabaseUtils.tableExists(connection);
		} catch (Exception e) {
			// ok
		}
		DatabaseUtils.createTable(connection);
		boolean secondAssert = DatabaseUtils.tableExists(connection);
		Assert.assertFalse(firstAssert);
		Assert.assertTrue(secondAssert);
		connection.close();
		System.out.println("Connection is closed "+connection.isClosed());
	}

	@Test
	public void insertInto() throws SQLException {
		Connection connection = connection();
		System.out.println("Connection "+System.identityHashCode(connection));
		LRProcessDefinitionImpl def = new LRProcessDefinitionImpl(null,null);
		UnixLRProcessImpl ulp = new UnixLRProcessImpl(def,null,null);
		DatabaseUtils.createTable(connection);
		DatabaseUtils.registerProcess(connection, ulp);
		
		Statement stm = connection.createStatement();
		ResultSet rs = stm.executeQuery("select * from processes");
		Assert.assertTrue(rs.next());
		connection.close();
		System.out.println("Connection is closed "+connection.isClosed());
	}
	
	@Test
	public void updatePID() throws SQLException {
		Connection connection = connection();
		LRProcessDefinitionImpl def = new LRProcessDefinitionImpl(null, null);
		UnixLRProcessImpl ulp = new UnixLRProcessImpl(def,null, null);
		DatabaseUtils.createTable(connection);
		DatabaseUtils.registerProcess(connection, ulp);
		DatabaseUtils.updateProcessPID(connection, "@22", ulp.getUUID());
		
		Statement stm = connection.createStatement();
		ResultSet rs = stm.executeQuery("select * from processes");
		Assert.assertTrue(rs.next());
		String pidFromDb = rs.getString("PID");
		Assert.assertTrue(pidFromDb != null);
		Assert.assertTrue("@22".equals(pidFromDb));
		connection.close();
	}

	@Test
	public void updateStatus() throws SQLException {
		Connection connection = connection();
		LRProcessDefinitionImpl def = new LRProcessDefinitionImpl(null, null);
		UnixLRProcessImpl ulp = new UnixLRProcessImpl(def,null, null);
		ulp.setProcessState(States.RUNNING);
		DatabaseUtils.createTable(connection);
		DatabaseUtils.registerProcess(connection, ulp);
		matchState(connection, States.RUNNING.getVal());

		ulp.setProcessState(States.FINISHED);
		DatabaseUtils.updateProcessState(connection, ulp.getUuid(), ulp.getProcessState());

		matchState(connection, States.FINISHED.getVal());

		ulp.setProcessState(States.FAILED);
		DatabaseUtils.updateProcessState(connection, ulp.getUuid(), ulp.getProcessState());
		matchState(connection, States.FAILED.getVal());

		//System.out.println(status);
		connection.close();
	}

	private void matchState(Connection connection, int expectingVal) throws SQLException {
		Statement stm = connection.createStatement();
		ResultSet rs = stm.executeQuery("select * from processes");
		Assert.assertTrue(rs.next());
		int status = rs.getInt("STATUS");
		Assert.assertTrue(expectingVal== status);
	}
}
