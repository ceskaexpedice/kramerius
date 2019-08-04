package cz.incad.kramerius;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
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
            //TODO: I18N
            ProcessStarter.updateName("Dnnt unset  '"+csvFile+"'");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }

        System.setProperty(DNNTFlag.DNNT_FILE_KEY, csvFile);
        DNNTFlag.main(new String[]{Boolean.FALSE.toString()});
    }

}
