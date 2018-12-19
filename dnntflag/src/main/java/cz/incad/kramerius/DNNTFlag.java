package cz.incad.kramerius;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.RelationshipTuple;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DNNTFlag {

    public static final String DNNT_FILE_KEY = "dnnt.file";
    public static final String DNNT_MODE_KEY = "dnnt.mode";
    public static final String DNNT_COLUMN_NUMBER = "dnnt.pidcolumn";
    public static final String DNNT_SKIPHEADER = "dnnt.skipheader";
    public static final String DNNT_THREADS = "dnnt.threads";

    public static final String DNNT_MODE = "dnnt.solr.mode";

    private Client client;


    public static void main(String[] args) throws IOException {
        String file = KConfiguration.getInstance().getConfiguration().getString(DNNT_FILE_KEY, Constants.WORKING_DIR + File.separator + "dnnt.csv");
        String mode = KConfiguration.getInstance().getConfiguration().getString(DNNT_MODE_KEY,"add");
        int pidcolumn = KConfiguration.getInstance().getConfiguration().getInt(DNNT_COLUMN_NUMBER,0);
        boolean skipHeader = KConfiguration.getInstance().getConfiguration().getBoolean(DNNT_SKIPHEADER,true);

        int numberofThreads = KConfiguration.getInstance().getConfiguration().getInt(DNNT_THREADS,2);

        FedoraAccess fedoraAccess = new FedoraAccessImpl(KConfiguration.getInstance(), null);
        ExecutorService executor = Executors.newFixedThreadPool(numberofThreads);

        Client client = Client.create();

        File f = new File(file);
        if (f.exists() && f.canRead()) {
            Reader in = new FileReader(f);
            CSVFormat format = skipHeader ? CSVFormat.EXCEL.withFirstRecordAsHeader() : CSVFormat.EXCEL;
            Iterable<CSVRecord> records = format.parse(in);
            for (CSVRecord record : records) {
                String pid = record.get(pidcolumn);
                executor.submit(new DNNTWorker(pid, fedoraAccess, client));
            }
        } else throw new IOException("not exist or cannot read from "+f.getAbsolutePath());

    }





}
