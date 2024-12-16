package cz.cas.lib.knav;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.cas.lib.knav.indexer.CollectPidForIndexing;
import cz.incad.kramerius.fedora.RepositoryAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.repository.RepositoryException;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.fedora.om.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;

public class ParametrizedApplyMovingWall {


    @Process
    public static void process(@ParameterName("userValue") String uVal,  @ParameterName("mode") String mode, @ParameterName("pids") String pidsString) throws XPathExpressionException, IOException, RightCriteriumException, RepositoryException {

        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        RepositoryAccess fa = injector.getInstance(Key.get(RepositoryAccess.class, Names.named("rawFedoraAccess")));

        SolrAccess sa = new SolrAccessImplNewIndex();
        CollectPidForIndexing coll = new CollectPidForIndexing();

        String[] pids = pidsString.split(",");

        ApplyMWUtils.applyMWOverPidsArray(fa, sa, coll, uVal, mode, pids);
    }
}
