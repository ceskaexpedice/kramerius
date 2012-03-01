package cz.incad.kramerius.processes.impl;

import java.io.ByteArrayOutputStream;
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
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.database.ProcessDatabaseUtils;
import cz.incad.kramerius.processes.logging.LoggingLoader;
import cz.incad.kramerius.processes.utils.ProcessUtils;

public class ProcessStarter {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessStarter.class.getName());

    public static final String LOGGING_FILE_PROPERTY = "java.util.logging.config.file";
    public static final String LOGGING_CLASS_PROPERTY = "java.util.logging.config.class";
    
    public static final String MAIN_CLASS_KEY = "mainClass";
    public static final String UUID_KEY = "uuid";
    public static final String TOKEN_KEY = "token";
    public static final String CLASSPATH_NAME = "CLASSPATH";

    public static final String SOUT_FILE = "SOUT";
    public static final String SERR_FILE = "SERR";

    private static boolean STATUS_UPDATED = false;
    
    public static void main(String[] args)  {
        PrintStream outStream = null;
        PrintStream errStream = null;
        try {

            String mainClass = System.getProperty(MAIN_CLASS_KEY);
            outStream = createPrintStream(System.getProperty(SOUT_FILE));
            errStream = createPrintStream(System.getProperty(SERR_FILE));
            System.setErr(errStream);
            System.setOut(outStream);
            
            setDefaultLoggingIfNecessary();

            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    try {
                        if (!STATUS_UPDATED) {
                            updateStatus(States.KILLED);
                        }
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            });
            Class<?> clz = Class.forName(mainClass);
            Method method = clz.getMethod("main", args.getClass());
            String pid = getPID();
            updatePID(pid);
            Object[] objs = new Object[1];
            objs[0] = args;
            method.invoke(null, objs);
            updateStatus(States.FINISHED);
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            try {
                updateStatus(States.FAILED);
            } catch(IOException ex) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
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

    private static void setDefaultLoggingIfNecessary() {
        String classProperty = System.getProperty(LOGGING_CLASS_PROPERTY);
        String fileProperty = System.getProperty(LOGGING_FILE_PROPERTY);
        if ((classProperty == null) && (fileProperty == null)) {
            // loads default logging 
            new LoggingLoader();
        }
    }
    
    private static PrintStream createPrintStream(String file) throws FileNotFoundException {
        return new PrintStream(new FileOutputStream(file));
    }

    public static void updateStatus(States state) throws MalformedURLException, IOException {
        String uuid = System.getProperty(UUID_KEY);
        String lrURl = ProcessUtils.getLrServlet();
        String restURL = lrURl + "?action=updateStatus&uuid=" + uuid + "&state=" + state;
        httpGet(restURL);
        STATUS_UPDATED = true;
    }

    public static void updatePID(String pid) throws IOException {
        String uuid = System.getProperty(UUID_KEY);
        String lrURl = ProcessUtils.getLrServlet();
        
        String restURL = lrURl + "?action=updatePID&uuid=" + uuid + "&pid=" + pid;
        httpGet(restURL);
    }

    public static void updateName(String name) throws IOException {
        String uuid = System.getProperty(UUID_KEY);
        String lrURl = ProcessUtils.getLrServlet();
        
        String restURL = lrURl + "?action=updateName&uuid=" + uuid + "&name=" + URLEncoder.encode(name, "UTF-8");
        LOGGER.info("requesting url :" + restURL);
        httpGet(restURL);
    }

    public static byte[] httpGet(String restURL) throws MalformedURLException, IOException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            URL url = new URL(restURL);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            
            byte[] buffer = new byte[1 << 12];
            int read = -1;
            while ((read = inputStream.read(buffer)) > 0) {
                bos.write(buffer,0,read);
            }
            ;
            
            return buffer;
        } catch (Exception ex) {
            LOGGER.severe("Problem connecting to REST URL: " + restURL + " - " + ex);
            throw new RuntimeException(ex);
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
}
