package cz.incad.kramerius.resourceindex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.fcrepo.client.FcrepoOperationFailedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProcessingIndexRebuild {

    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexCheck.class.getName());

    public static void main(String[] args) throws IOException, SolrServerException, RepositoryException, FcrepoOperationFailedException {
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        final FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        final Repository repo = fa.getInternalAPI();
        final ProcessingIndexFeeder instance = injector.getInstance(ProcessingIndexFeeder.class);
        try {
            instance.deleteProcessingIndex();
            repo.iterateObjects((pid)->{
                LOGGER.info("Rebuilding processing index for  pid "+pid);
                try {
                    repo.getObject(pid).rebuildProcessingIndex();
                } catch (RepositoryException e) {
                    throw new RuntimeException(e);
                }
            });
        } finally {
            if (instance != null) {
                instance.commit();
            }
        }
    }

}
