package cz.cas.lib.knav;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import cz.cas.lib.knav.indexer.CollectPidForIndexing;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ParametrizedApplyMovingWall {


    @Process
    public static void process(@ParameterName("userValue") String uVal, @ParameterName("pids") String pidsString) throws XPathExpressionException, IOException, RightCriteriumException, RepositoryException {

        FedoraAccess fa = new FedoraAccessImpl(KConfiguration.getInstance(),null);
        SolrAccess sa = new SolrAccessImpl();
        CollectPidForIndexing coll = new CollectPidForIndexing();

        String[] pids = pidsString.split(",");
        ApplyMWUtils.applyMWOverPidsArray(fa, sa, coll, uVal, pids);
    }
}
