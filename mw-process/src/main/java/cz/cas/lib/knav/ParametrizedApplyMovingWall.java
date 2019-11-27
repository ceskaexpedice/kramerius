package cz.cas.lib.knav;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.xml.xpath.XPathExpressionException;

import cz.cas.lib.knav.indexer.CollectPidForIndexing;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.utils.conf.KConfiguration;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class ParametrizedApplyMovingWall {


    @Process
    public static void process(@ParameterName("userValue") String uVal, @ParameterName("mode") String mode, @ParameterName("pids") String pidsString) throws XPathExpressionException, IOException, RightCriteriumException, ParserConfigurationException, SAXException {

        FedoraAccess fa = new FedoraAccessImpl(KConfiguration.getInstance(),null);
        SolrAccess sa = new SolrAccessImpl();
        CollectPidForIndexing coll = new CollectPidForIndexing();
 
        String[] pids = pidsString.split(",");   
        ApplyMWUtils.applyMWOverPidsArray(fa, sa, coll, uVal, mode, pids);
    }
}
