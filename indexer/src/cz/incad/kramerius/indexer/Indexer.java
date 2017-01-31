/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.indexer;

/**
 *
 * @author Incad
 */
// TODO: Rewrite it !!
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

public class Indexer {

    private static final Logger logger = Logger.getLogger(Indexer.class.getName());
    private ProgramArguments arguments;
    //public static Configuration conf;
    //public static Properties conf = new Properties();
    public static boolean hasData = false;
    public static String outFilename = "outfile.zip";
    public static ZipOutputStream outZip;
    

    public Indexer(ProgramArguments args) throws Exception {
        arguments = args;
        //PropertyConfigurator.configure(arguments.log4jFile);
        //conf.load(new FileInputStream(arguments.configFile));
        
        logger.info("Indexer initialized");
    }

    public void run() throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat("");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            String to = formatter.format(date);
            logger.log(Level.INFO, "Current index time: {0}", date);

            String from = "";
            String updateTimeFile = "time";
            File dateFile = new File(updateTimeFile);
            if (arguments.from == null) {
                if ((new File(updateTimeFile)).exists()) {
                    BufferedReader in = new BufferedReader(new FileReader(updateTimeFile));
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
            } else {
                if (!update(from, to)) {
                    success = false;
                }
            }

            if (success) {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)));
                out.write(to);
                out.close();
                logger.info("Index process success");
            }else{
                throw new Exception("Index process failed");
            }


            long timeInMiliseconds = System.currentTimeMillis() - startTime;
            showResults();
            logger.info(formatElapsedTime(timeInMiliseconds));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Run failed", ex);
            throw new Exception(ex);
        }
    }

    private boolean update(String from, String to) {
        try {
            logger.info("Update index...");
            doUpdate();
            return true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Update failed", ex);
            return false;
        }
    }

    private void doUpdate() throws Exception {
        updateIndex(arguments.action, arguments.value);
    }

    public void updateIndex(String action, String value) throws Exception {
        FedoraOperations ops = new FedoraOperations();
        ArrayList<String> params = new ArrayList<String>();
        ops.updateIndex(action, value, params);
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

