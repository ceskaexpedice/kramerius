package cz.incad.kramerius.processes.impl;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;

import sun.font.CreatedFontTracker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.database.DatabaseUtils;

public class ProcessStarter {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ProcessStarter.class.getName());
	
	public static final String MAIN_CLASS_KEY="mainClass";
	public static final String UUID_KEY="uuid";
	public static final String CLASSPATH_NAME="CLASSPATH";

	public static void main(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SQLException {
		String mainClass = System.getProperty(MAIN_CLASS_KEY);
		try {
			Class<?> clz = Class.forName(mainClass);
			Method method = clz.getMethod("main", args.getClass());
			String pid = getPID();
			updatePID(pid);
			Object[] objs = new Object[1];
			objs[0] = args;
			method.invoke(null,objs);
			updateStatus(States.FINISHED);
		} catch (Throwable e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			updateStatus(States.FAILED);
		}
	}
	
	private static void updateStatus(States state) throws SQLException {
		String uuid = System.getProperty(UUID_KEY);
		if (uuid != null) {
			Connection con = null;
			try {
				Injector inj = microProcessModule();
				con = inj.getInstance(Connection.class);
				DatabaseUtils.updateProcessState(con, uuid, state);
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				if (con != null) con.close();
			}
		}
	}

	private static void updatePID(String pid) throws SQLException {
		String uuid = System.getProperty(UUID_KEY);
		if (uuid != null) {
			LOGGER.info("updating database. Pid =  '"+pid+"'");
			Connection con = null;
			try {
				Injector inj = microProcessModule();
				con = inj.getInstance(Connection.class);
				DatabaseUtils.updateProcessPID(con, pid, uuid);
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				if (con != null) con.close();
			}
		}
	}


	public static String getPID() {
		String pid = null;
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String[] split = name.split("@");
		if ((split != null) && (split.length > 1)) {
			pid = split[0];
		}
		return pid;
	}
	
	public static Injector microProcessModule() {
		Injector injector = Guice.createInjector(new ProcessMicroModule());
		return injector;
	}
}
