package cz.cas.lib.knav;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.cas.lib.knav.indexer.CollectPidForIndexing;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;

public class ParametrizedApplyMovingWall {


    @Process
    public static void process(@ParameterName("userValue") String uVal,  @ParameterName("mode") String mode, @ParameterName("pids") String pidsString) throws XPathExpressionException, IOException, RightCriteriumException, RepositoryException {

        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        SolrAccess sa = injector.getInstance(Key.get(SolrAccess.class, Names.named("new-index")));

        CollectPidForIndexing coll = new CollectPidForIndexing();

        String[] pids = pidsString.split(",");

        ApplyMWUtils.applyMWOverPidsArray(fa, sa, coll, uVal, mode, pids);
    }
}
