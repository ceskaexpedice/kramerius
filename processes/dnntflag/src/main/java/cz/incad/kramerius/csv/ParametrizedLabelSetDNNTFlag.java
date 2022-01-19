package cz.incad.kramerius.csv;

import cz.incad.kramerius.fedora.om.impl.AkubraDOManager;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;

public class ParametrizedLabelSetDNNTFlag {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ParametrizedLabelSetDNNTFlag.class.getName());

    @Process
    public static void process(
            @ParameterName("csvfile") String csvFile, @ParameterName("label") String label) throws IOException, InterruptedException, JAXBException, SAXException, BrokenBarrierException {
        try {
            String formatted = String.format("DNNT label set. Label: %s and CSV file: %s", label, csvFile);
            ProcessStarter.updateName(formatted);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }
        System.setProperty(AbstractDNNTCSVProcess.DNNT_FILE_KEY, csvFile);
        DDNTCSVLabeledFlag.main(new String[]{Boolean.TRUE.toString(), label});

    }


    public static void main(String[] args) throws BrokenBarrierException, InterruptedException, IOException {
        try {
            System.setProperty(AbstractDNNTCSVProcess.DNNT_FILE_KEY, "/c:/Users/happy/.kramerius4/import-dnnt/periodicalvolume.csv");
            System.setProperty(AbstractDNNTCSVProcess.DNNTUNSET_FILE_KEY, "/c:/Users/happy/.kramerius4/import-dnnt/periodicalvolume.csv");
            DDNTCSVLabeledFlag.main(new String[]{Boolean.TRUE.toString(), "dnnto"});
        } finally {
            LOGGER.info("Trying to shutdown akubra manager");
            AkubraDOManager.shutdown();
            LOGGER.info("Akubra is down");
        }
        //
    }


}
