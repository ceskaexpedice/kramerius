package cz.incad.kramerius.rest.apiNew.admin.v10;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.SolrIndexAccess;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.List;


/**
 * Helper for accessing Search index from code implementing API endpoints.
 * Typically for synchronous deletion from Search index without the overhead of long-runnig processes.
 * <p>
 * This should not be used for more complicated scenarios, like iterations, reindexation of objects, etc.
 * For those use long-runnig processes a cz.kramerius.searchIndex.indexerProcess.Indexer.
 */
public class SearchIndexHelper {
    private SolrIndexAccess searchIndex;

    public SearchIndexHelper() {
        SolrConfig solrConfig = new SolrConfig(KConfiguration.getInstance());
        this.searchIndex = new SolrIndexAccess(solrConfig);
    }

    public void deleteFromIndex(String pid) throws IOException {
        try {
            this.searchIndex.deleteById(pid);
        } catch (SolrServerException e) {
            throw new IOException(e);
        }
    }

    public void deleteFromIndex(List<String> pids) throws IOException {
        try {
            this.searchIndex.deleteByIds(pids);
        } catch (SolrServerException e) {
            throw new IOException(e);
        }
    }
}
