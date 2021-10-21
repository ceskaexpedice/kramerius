package cz.incad.kramerius.export;

import cz.incad.kramerius.LicensesExport;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;

public class ParametrizedLicenseExport {


    @Process
    public static void process(@ParameterName("license") String label) throws  IOException, InterruptedException, BrokenBarrierException, ParserConfigurationException, SAXException, MigrateSolrIndexException {
        LicensesExport.main(new String[] {label});
    }

}
