package cz.incad.kramerius.csv;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;

public class ParametrizedUnsetDNNTFlag {


    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ParametrizedUnsetDNNTFlag.class.getName());

    @Process
    public static void process(
            @ParameterName("csvfile") String csvFile) throws IOException, InterruptedException, JAXBException, SAXException, BrokenBarrierException {
        try {
            String formatted = String.format("DNNT unset. CSV file: %s", csvFile);
            ProcessStarter.updateName(formatted);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }

        System.setProperty(AbstractDNNTCSVProcess.DNNTUNSET_FILE_KEY, csvFile);
        DNNTCSVFlag.main(new String[]{Boolean.FALSE.toString()});
    }

}
