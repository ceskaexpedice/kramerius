package cz.inovatika.kramerius.fedora.om.processingindex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.fedora.RepositoryAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.repository.AkubraRepository;
import cz.incad.kramerius.fedora.om.repository.RepositoryException;
import cz.incad.kramerius.fedora.om.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProcessingIndexCheck {

    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexCheck.class.getName());

    public static void main(String[] args) throws IOException, SolrServerException, RepositoryException {
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        final RepositoryAccess fa = injector.getInstance(Key.get(RepositoryAccess.class, Names.named("rawFedoraAccess")));
        final AkubraRepository repo = fa.getInternalAPI();
        final ProcessingIndexFeeder instance = injector.getInstance(ProcessingIndexFeeder.class);

        List<String> pidsToDelete = new ArrayList<>();
        instance.iterateProcessingSortedByPid(ProcessingIndexFeeder.DEFAULT_ITERATE_QUERY, (SolrDocument doc) -> {
            try {
                Object source = doc.getFieldValue("source");
                if (!repo.objectExists(source.toString())) {
                    LOGGER.info("Object marked for delete :" + source.toString());
                    pidsToDelete.add(source.toString());
                }
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });

        pidsToDelete.stream().forEach(pid -> {
            try {
                LOGGER.info("Deleting pid :" + pid);
                instance.deleteByPid(pid);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SolrServerException e) {
                throw new RuntimeException(e);
            }
        });
        instance.commit();
    }
}
