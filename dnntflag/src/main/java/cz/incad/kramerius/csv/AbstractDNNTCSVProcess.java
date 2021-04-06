package cz.incad.kramerius.csv;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.AbstractDNNTProcess;
import cz.incad.kramerius.Constants;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.workers.DNNTWorker;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;

public abstract class AbstractDNNTCSVProcess extends AbstractDNNTProcess {

    public static final String DNNT_FILE_KEY = "dnnt.file";
    public static final String DNNTUNSET_FILE_KEY = "dnntunset.file";
    public static final String DNNT_MODE_KEY = "dnnt.mode";
    public static final String DNNT_COLUMN_NUMBER = "dnnt.pidcolumn";
    public static final String DNNT_SKIPHEADER = "dnnt.skipheader";

    public static final String DNNT_DELIMITER = "dnnt.delimiter";

    protected int pidcolumn = -1;
    protected boolean skipHeader = false;
    protected String delimiter = null;

    protected String csvFile;

    protected void iterateCSVFile() throws IOException, BrokenBarrierException, InterruptedException {
        FedoraAccess fedoraAccess = new FedoraAccessImpl(KConfiguration.getInstance(), null);
        Client client = Client.create();

        client.setReadTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));
        client.setConnectTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));


        final List<DNNTWorker> dnntWorkers = new ArrayList<>();
        File f = new File(csvFile);
        if (f.exists() && f.canRead()) {
            try {
                Reader in = new FileReader(f);
                CSVFormat format = skipHeader ? CSVFormat.EXCEL.withFirstRecordAsHeader() : CSVFormat.EXCEL;
                format = format.withDelimiter(delimiter.charAt(0));
                Iterable<CSVRecord> records = format.parse(in);
                for (CSVRecord record : records) {
                    if ( pidcolumn < record.size()) {
                        String pid = record.get(pidcolumn);
                        if (dnntWorkers.size() >= numberofThreads) {
                            startWorkers(dnntWorkers);
                            dnntWorkers.clear();
                            dnntWorkers.add(createWorker(pid, fedoraAccess, client, addRemoveFlag));
                        } else {
                            dnntWorkers.add(createWorker(pid, fedoraAccess, client, addRemoveFlag));
                        }

                    } else {
                        DNNTCSVFlag.LOGGER.log(Level.WARNING, "Ommiting row '"+record+"' number of col is "+record.size() +" and PID's column is "+pidcolumn);
                    }
                }

                if (!dnntWorkers.isEmpty()) {
                    startWorkers(dnntWorkers);
                    dnntWorkers.clear();
                }
            } finally {
                this.commit(client);

            }

        } else throw new IOException("not exist or cannot read from "+f.getAbsolutePath());
    }


    protected void initializeFromProperties() {
        super.initializeFromProperties();
        this.pidcolumn = KConfiguration.getInstance().getConfiguration().getInt(DNNT_COLUMN_NUMBER,0);
        this.skipHeader = KConfiguration.getInstance().getConfiguration().getBoolean(DNNT_SKIPHEADER,true);
        this.delimiter = KConfiguration.getInstance().getConfiguration().getString(DNNT_DELIMITER,",");
    }


    public void process(String[] args) throws IOException, BrokenBarrierException, InterruptedException {
        initializeFromProperties();
        initializeFromArgs(args);
        iterateCSVFile();
    }

    protected void defaultCSVFileInitialization(boolean flag) throws IOException {
        if (flag) {
            csvFile = System.getProperties().containsKey(DNNT_FILE_KEY) ? System.getProperty(DNNT_FILE_KEY) : KConfiguration.getInstance().getConfiguration().getString(DNNT_FILE_KEY, Constants.WORKING_DIR + File.separator + "dnnt.csv");
            //ProcessStarter.updateName("Set DNNT flag. Processing file : (" + this.csvFile + " )" );
        } else {
            csvFile = System.getProperties().containsKey(DNNTUNSET_FILE_KEY) ? System.getProperty(DNNTUNSET_FILE_KEY) : KConfiguration.getInstance().getConfiguration().getString(DNNTUNSET_FILE_KEY, Constants.WORKING_DIR + File.separator + "dnntunset.csv");
            //ProcessStarter.updateName("Unset DNNT flag. Processing file : (" + csvFile + " )" );
        }
    }
}
