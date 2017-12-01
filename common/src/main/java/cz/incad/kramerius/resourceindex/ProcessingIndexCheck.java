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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProcessingIndexCheck {

    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexCheck.class.getName());

    public static void main(String[] args) throws IOException, SolrServerException, RepositoryException {
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        final FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        final Repository repo = fa.getInternalAPI();
        final ProcessingIndexFeeder instance = injector.getInstance(ProcessingIndexFeeder.class);

        List<String> pidsToDelete = new ArrayList<>();
        instance.iterateProcessing((SolrDocument doc) ->{
            try {
                Object source = doc.getFieldValue("source");
                if (!repo.objectExists(source.toString())) {
                    LOGGER.info("Object marked for delete :"+source.toString());
                    pidsToDelete.add(source.toString());
                }
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });

        pidsToDelete.stream().forEach(pid ->{
            try {
                LOGGER.info("Deleting pid :"+pid);
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
