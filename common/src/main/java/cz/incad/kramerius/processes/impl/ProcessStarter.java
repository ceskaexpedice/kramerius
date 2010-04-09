package cz.incad.kramerius.processes.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.database.DatabaseUtils;

public class ProcessStarter {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ProcessStarter.class.getName());
	
	public static final String MAIN_CLASS_KEY="mainClass";
	public static final String UUID_KEY="uuid";
	public static final String CLASSPATH_NAME="CLASSPATH";
	public static final String LR_SERVLET_URL="LR_SERVLET_URL";
	
	public static final String SOUT_FILE = "SOUT";
	public static final String SERR_FILE = "SERR";
	
	
	public static void main(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SQLException, MalformedURLException, IOException {
		String mainClass = System.getProperty(MAIN_CLASS_KEY);
		PrintStream outStream = createPrintStream(System.getProperty(SOUT_FILE));
		PrintStream errStream = createPrintStream(System.getProperty(SERR_FILE));
		System.setErr(errStream);
		System.setOut(outStream);
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
			if (outStream != null) {
				try {
					outStream.close();
				} catch (Exception e1) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
				}
			}
			if (errStream != null) {
				try {
					errStream.close();
				} catch (Exception e1) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

	
	private static PrintStream createPrintStream(String file) throws FileNotFoundException {
		return new PrintStream(new FileOutputStream(file));
	}


	private static void updateStatus(States state) throws  MalformedURLException, IOException {
		String uuid = System.getProperty(UUID_KEY);
		String lrURl = System.getProperty(LR_SERVLET_URL);
		String restURL = lrURl + "?action=updateStatus&uuid="+uuid+"&state="+state;
		httpGet(restURL);
	}

	private static void updatePID(String pid) throws IOException {
		String uuid = System.getProperty(UUID_KEY);
		String lrURl = System.getProperty(LR_SERVLET_URL);
		String restURL = lrURl + "?action=updatePID&uuid="+uuid+"&pid="+pid;
		httpGet(restURL);
	}

	private static void httpGet(String restURL) throws MalformedURLException,
			IOException {
		URL url = new URL(restURL);
		URLConnection connection = url.openConnection();
		InputStream inputStream = connection.getInputStream();
		byte[] buffer = new byte[1<<12];
		int read = -1;
		while((read=inputStream.read(buffer)) > 0) {};
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
	
	
}
