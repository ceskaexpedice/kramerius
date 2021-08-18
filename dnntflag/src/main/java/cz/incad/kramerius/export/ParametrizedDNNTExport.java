package cz.incad.kramerius.export;

import cz.incad.kramerius.DNNTExport;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;

public class ParametrizedDNNTExport {


    @Process
    public static void process(@ParameterName("dnnt-label") String label, @ParameterName("pid") String pid) throws  IOException, InterruptedException, BrokenBarrierException, ParserConfigurationException, SAXException, MigrateSolrIndexException {
        DNNTExport.main(new String[] {label});
    }

}
