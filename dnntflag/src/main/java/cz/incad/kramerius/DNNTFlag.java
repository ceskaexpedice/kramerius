package cz.incad.kramerius;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process for association of DNNT flag
 */
public class DNNTFlag {

    public static final Logger LOGGER = Logger.getLogger(DNNTFlag.class.getName());

    private static final String DNNT_FILE_KEY = "dnnt.file";
    private static final String DNNTUNSET_FILE_KEY = "dnntunset.file";
    private static final String DNNT_MODE_KEY = "dnnt.mode";
    private static final String DNNT_COLUMN_NUMBER = "dnnt.pidcolumn";
    private static final String DNNT_SKIPHEADER = "dnnt.skipheader";
    private static final String DNNT_THREADS = "dnnt.threads";

    private static final String DNNT_DELIMITER = "dnnt.delimiter";


    public static void main(String[] args) throws IOException, BrokenBarrierException, InterruptedException {

        String mode = KConfiguration.getInstance().getConfiguration().getString(DNNT_MODE_KEY,"add");
        int pidcolumn = KConfiguration.getInstance().getConfiguration().getInt(DNNT_COLUMN_NUMBER,0);
        boolean skipHeader = KConfiguration.getInstance().getConfiguration().getBoolean(DNNT_SKIPHEADER,true);
        String delimiter = KConfiguration.getInstance().getConfiguration().getString(DNNT_DELIMITER,",");
        int numberofThreads = KConfiguration.getInstance().getConfiguration().getInt(DNNT_THREADS,2);

        boolean flag = true;
        if (args.length>0) {
            flag = Boolean.valueOf(args[0]);
        }


        String file = null;
        if (args.length > 1) {
            file = args[1];
        } else {
            if (flag) {
                file = KConfiguration.getInstance().getConfiguration().getString(DNNT_FILE_KEY, Constants.WORKING_DIR + File.separator + "dnnt.csv");
            } else {
                file = KConfiguration.getInstance().getConfiguration().getString(DNNTUNSET_FILE_KEY, Constants.WORKING_DIR + File.separator + "dnntunset.csv");
            }
        }

        FedoraAccess fedoraAccess = new FedoraAccessImpl(KConfiguration.getInstance(), null);
        Client client = Client.create();

        final List<DNNTWorker>  dnntWorkers = new ArrayList<>();
        File f = new File(file);
        if (f.exists() && f.canRead()) {
            Reader in = new FileReader(f);
            CSVFormat format = skipHeader ? CSVFormat.EXCEL.withFirstRecordAsHeader() : CSVFormat.EXCEL;
            // set delimiter
            format = format.withDelimiter(delimiter.charAt(0));

            Iterable<CSVRecord> records = format.parse(in);
            for (CSVRecord record : records) {
                if ( pidcolumn < record.size()) {
                    String pid = record.get(pidcolumn);
                    if (dnntWorkers.size() >= numberofThreads) {
                        startWorkers(dnntWorkers);
                        dnntWorkers.clear();
                        dnntWorkers.add(new DNNTWorker(pid, fedoraAccess, client,flag));
                    } else {
                        dnntWorkers.add(new DNNTWorker(pid, fedoraAccess, client,flag));
                    }

                } else {
                    LOGGER.log(Level.WARNING, "Ommiting row '"+record+"' number of col is "+record.size());
                }
            }

            if (!dnntWorkers.isEmpty()) {
                startWorkers(dnntWorkers);
                dnntWorkers.clear();
            }
        } else throw new IOException("not exist or cannot read from "+f.getAbsolutePath());

    }




    private static void startWorkers(List<DNNTWorker> worksWhasHasToBeDone) throws BrokenBarrierException, InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(worksWhasHasToBeDone.size()+1);
        worksWhasHasToBeDone.stream().forEach(th->{
            th.setBarrier(barrier);
            new Thread(th).start();
        });
        barrier.await();
    }

}
