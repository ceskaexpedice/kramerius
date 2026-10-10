package cz.incad.kramerius.cdk.index1;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SolrSearchService implements SearchIndexService {

    private static final int PAGE_SIZE = 500;

    private static final String FIELD_PID = "pid";
    private static final String FIELD_OWN_PID_PATH = "own_pid_path";
    private static final String FIELD_OWN_PID_PATH_CHILDREN = "own_pid_path.children";
    private static final String FIELD_PID_PATHS = "pid_paths";
    private static final String FIELD_PID_PATHS_CHILDREN = "pid_paths.children";
    private static final String FIELD_IN_COLLECTION = "in-collection";
    private static final String FIELD_IN_COLLECTION_DIRECT = "in-collection-direct";

    private final HttpSolrClient solrClient;

    public SolrSearchService(HttpSolrClient solrClient) {
        this.solrClient = solrClient;
    }

    @Override
    public SearchDocument get(String pid) {
        SolrQuery query = new SolrQuery();
        query.setQuery(FIELD_PID + ":" + ClientUtils.escapeQueryChars(pid));
        query.setRows(1);
        query.setFields(
                FIELD_PID,
                FIELD_OWN_PID_PATH,
                FIELD_PID_PATHS,
                FIELD_IN_COLLECTION,
                FIELD_IN_COLLECTION_DIRECT);

        try {
            QueryResponse response = solrClient.query(query);
            SolrDocumentList results = response.getResults();
            if (results.isEmpty()) {
                return null;
            }
            return toSearchDocument(results.get(0));
        } catch (SolrServerException | IOException e) {
            throw new IllegalStateException("Cannot load search document with PID " + pid, e);
        }
    }

    /**
     * Finds documents in the source subtree that do not yet contain
     * the target CDK path in pid_paths.
     *
     * @param rootPid PID of the source subtree root
     * @param cdkPath ordered CDK path from the top-level collection
     *                to the collection referencing rootPid
     */
    public List<SearchDocument> findUnlinkedSubtree(String rootPid, List<String> cdkPath) {
        SearchDocument root = get(rootPid);
        if (root == null || root.getOwnPidPath() == null || root.getOwnPidPath().isEmpty()) {
            return Collections.emptyList();
        }
        if (cdkPath == null || cdkPath.isEmpty()) {
            throw new IllegalArgumentException("CDK path must not be empty");
        }
        String targetCdkPath = String.join("/", cdkPath) + "/" + root.getOwnPidPath();
        String sourcePathQuery =
                FIELD_OWN_PID_PATH_CHILDREN
                        + ":\""
                        + ClientUtils.escapeQueryChars(
                        root.getOwnPidPath())
                        + "\"";
        String missingCdkPathQuery =
                "-" + FIELD_PID_PATHS_CHILDREN
                        + ":\""
                        + ClientUtils.escapeQueryChars(
                        targetCdkPath)
                        + "\"";
        SolrQuery query = new SolrQuery();
        query.setQuery(sourcePathQuery + " AND " + missingCdkPathQuery);
        query.setFields(
                FIELD_PID,
                FIELD_OWN_PID_PATH,
                FIELD_PID_PATHS,
                FIELD_IN_COLLECTION,
                FIELD_IN_COLLECTION_DIRECT);
        query.setRows(PAGE_SIZE);
        query.setSort(FIELD_PID, SolrQuery.ORDER.asc);
        List<SearchDocument> result = new ArrayList<>();
        int start = 0;
        try {
            while (true) {
                query.setStart(start);
                QueryResponse response = solrClient.query(query);
                SolrDocumentList documents = response.getResults();
                for (SolrDocument document : documents) {
                    result.add(toSearchDocument(document));
                }
                start += documents.size();
                if (documents.isEmpty() || start >= documents.getNumFound()) {
                    break;
                }
            }
            return result;
        } catch (SolrServerException | IOException e) {
            throw new IllegalStateException("Cannot find unlinked subtree for PID "
                    + rootPid + ", target CDK path " + targetCdkPath, e);
        }
    }

    @Override
    public void updateCollectionsFields(String pid, List<String> inCollections, String inCollectionsDirect) {
        SolrInputDocument update = new SolrInputDocument();
        update.setField(FIELD_PID, pid);
        update.setField(
                FIELD_IN_COLLECTION,
                Collections.singletonMap("set", inCollections == null ? Collections.emptyList() : inCollections));
        update.setField(
                FIELD_IN_COLLECTION_DIRECT,
                Collections.singletonMap("set", inCollectionsDirect));
        submitUpdate(update);
    }

    @Override
    public void updatePidPaths(String pid, List<String> pidPaths) {
        SolrInputDocument update = new SolrInputDocument();
        update.setField(FIELD_PID, pid);
        update.setField(
                FIELD_PID_PATHS,
                Collections.singletonMap(
                        "set", pidPaths == null ? Collections.emptyList() : pidPaths));
        submitUpdate(update);
    }

    public void commit() {
        try {
            solrClient.commit();
        } catch (SolrServerException | IOException e) {
            throw new IllegalStateException("Cannot commit Solr updates", e);
        }
    }

    private void submitUpdate(SolrInputDocument update) {
        try {
            solrClient.add(update);
        } catch (SolrServerException | IOException e) {
            throw new IllegalStateException("Cannot update Solr document", e);
        }
    }

    private SearchDocument toSearchDocument(SolrDocument document) {
        return new SearchDocument(
                asString(document.getFieldValue(FIELD_PID)),
                asString(document.getFieldValue(FIELD_OWN_PID_PATH)),
                asStringList(document.getFieldValue(FIELD_IN_COLLECTION)),
                asString(document.getFieldValue(FIELD_IN_COLLECTION_DIRECT)),
                asStringList(document.getFieldValue(FIELD_PID_PATHS)));
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private List<String> asStringList(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        if (value instanceof Collection<?>) {
            List<String> result = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                result.add(item.toString());
            }
            return result;
        }
        return Collections.singletonList(value.toString());
    }
}
