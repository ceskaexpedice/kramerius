package cz.incad.kramerius.resourceindex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexQueryParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProcessingIndexCheck {

    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexCheck.class.getName());

    public static void main(String[] args) throws IOException, SolrServerException {
        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));

        List<String> pidsToDelete = new ArrayList<>();
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString("*:*")
                .sortField("pid")
                .ascending(true)
                .cursorMark(ProcessingIndex.CURSOR_MARK_START)
                .fieldsToFetch(List.of("source"))
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            if (!akubraRepository.exists(processingIndexItem.source())) {
                LOGGER.info("Object marked for delete :" + processingIndexItem.source());
                pidsToDelete.add(processingIndexItem.source());
            }
        });
        pidsToDelete.stream().forEach(pid -> {
            LOGGER.info("Deleting pid :" + pid);
            akubraRepository.pi().deleteByPid(pid);
        });
        akubraRepository.pi().commit();
    }
}
