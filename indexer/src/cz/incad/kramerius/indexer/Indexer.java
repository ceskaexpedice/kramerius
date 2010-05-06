/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.indexer;

/**
 *
 * @author Incad
 */
import cz.incad.utils.Formating;
import dk.defxws.fedoragsearch.server.GenericOperationsImpl;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import javax.security.auth.Subject;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
//import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.TimeZone;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;

public class Indexer {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private ProgramArguments arguments;
    //public static Configuration conf;
    public static Properties conf = new Properties();
    private Connection conn;
    public static boolean hasData = false;
    public static String outFilename = "outfile.zip";
    public static ZipOutputStream outZip;

    public Indexer(ProgramArguments args) throws Exception {
        arguments = args;
        //PropertyConfigurator.configure(arguments.log4jFile);
        //conf = new Configuration(arguments.configFile);
        conf.load(new FileInputStream(arguments.configFile));
        logger.info("Indexer initialized");
    }

    public void run() {
        long startTime = (new Date()).getTime();
        try {
            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat("");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            String to = formatter.format(date);
            logger.info("Current index time: " + date);

            String from = "";
            String updateTimeFile = "time";
            File dateFile = new File(updateTimeFile);
            if (arguments.from == null) {
                if ((new File(updateTimeFile)).exists()) {
                    BufferedReader in = new BufferedReader(new FileReader(updateTimeFile));
//                    Date fromDate = formatter.parse(from);
//                    fromDate.
                    from = in.readLine();
                } else {
                    from = "1900-01-01";
                }
            } else {
                from = arguments.from;
            }

            boolean success = true;


            if (arguments.docId > 0) {
                //indexDoc(arguments.docId);
                success = false;
            } else if (arguments.fullIndex) {
                logger.info("full index from db...");
                if (!fullIndex(arguments.maxDocuments)) {
                    success = false;
                }
            } else {
                if (!update(from, to)) {
                    success = false;
                }
            }

            if (success) {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)));
                out.write(to);
                out.close();
                logger.info("Update index success");
            }


            long timeInMiliseconds = (new Date()).getTime() - startTime;
            showResults();
            logger.info(formatElapsedTime(timeInMiliseconds));
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        } finally {
            //disconnect();
        }
    }

    private boolean fullIndex(int max) {
        try {
            logger.info("Full index...");
            return true;
        } catch (Exception ex) {
            logger.error("Full index failed");
            logger.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
    }

    private boolean update(String from, String to) {
        try {
            logger.info("Update index...");
            doUpdate("yo");
            return true;
        } catch (Exception ex) {
            logger.error("Update failed");
            logger.error(ex.toString());
            ex.printStackTrace();
            return false;
        }
    }
    private String repositoryName;
    private String indexName;
    private String resultPageXslt;
    private String restXslt;

    private void doUpdate(String user) throws Exception {
        Date startTime = new Date();

        
        StringBuffer resultXml = new StringBuffer("<resultPage/>");
        repositoryName = "";
        indexName = "";
        resultPageXslt = "";
        restXslt = "";
        String[] params = new String[8];
        params[0] = "ERRORMESSAGE";
        params[1] = "";
        params[2] = "TIMEUSEDMS";
        params[3] = "";
        try {
            resultXml = new StringBuffer(updateIndex(user, arguments.action, arguments.value, arguments.indexDocXslt));

        } catch (java.rmi.RemoteException e) {
            resultXml = new StringBuffer("<resultPage>");
            resultXml.append("<error><message><![CDATA[" + e.getMessage() + "]]></message></error>");
            resultXml.append("</resultPage>");
            params[1] = e.getMessage();
            logger.error(e);
            e.printStackTrace();
        }
        //String timeusedms = Long.toString((new Date()).getTime() - startTime.getTime());
        String timeusedms = Formating.formatElapsedTime((new Date()).getTime() - startTime.getTime());
        logger.info(timeusedms);
    }

    public String updateIndex(String user, String action, String value, String indexDocXslt)
            throws java.rmi.RemoteException, Exception {
        GenericOperationsImpl ops = new GenericOperationsImpl();
        ops.init(user, "", conf);
        ArrayList<String> params = new ArrayList<String>();
        //System.out.println(value);
        String result = ops.updateIndex(action, value, repositoryName, indexName, indexDocXslt, resultPageXslt, params);
        return result;
    }

    private String formatElapsedTime(long timeInMiliseconds) {
        long hours, minutes, seconds;
        long timeInSeconds = timeInMiliseconds / 1000;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;
        return hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)";
    }

    private void showResults() {
    }
}

