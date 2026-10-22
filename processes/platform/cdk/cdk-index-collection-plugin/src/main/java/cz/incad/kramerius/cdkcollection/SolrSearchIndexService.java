
package cz.incad.kramerius.cdkcollection;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.params.CursorMarkParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class SolrSearchIndexService implements SearchIndexService {

    private static final int PAGE_SIZE = 500;

    private static final String FIELD_PID = "pid";
    private static final String FIELD_OWN_PID_PATH = "own_pid_path";
    private static final String FIELD_OWN_PID_PATH_CHILDREN = "own_pid_path.children";
    private static final String FIELD_PID_PATHS = "pid_paths";
    private static final String FIELD_PID_PATHS_CHILDREN = "pid_paths.children";
    private static final String FIELD_IN_COLLECTION = "in_collections";
    private static final String FIELD_IN_COLLECTION_DIRECT = "in_collections.direct";
    private static final String FIELD_CDK_COLLECTION = "cdk.collection";

    private final HttpSolrClient solrClient;

    public SolrSearchIndexService(HttpSolrClient httpSolrClient) {
        this.solrClient = httpSolrClient;
    }

    @Override
    public SearchDocument get(String pid) {
        SolrQuery query = new SolrQuery();
        query.setQuery(FIELD_PID + ":" + ClientUtils.escapeQueryChars(pid));
        query.setRows(1);
        query.setFields(
                FIELD_PID,
                FIELD_OWN_PID_PATH,
                FIELD_IN_COLLECTION,
                FIELD_IN_COLLECTION_DIRECT,
                FIELD_PID_PATHS);
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

    @Override
    public void findUnlinkedSubtree(String rootPid, List<String> cdkPath, Consumer<List<SearchDocument>> pageConsumer) {
        if (pageConsumer == null) {
            throw new IllegalArgumentException("Page consumer must not be null");
        }
        SearchDocument root = get(rootPid);
        if (root == null || root.getOwnPidPath() == null || root.getOwnPidPath().isEmpty()) {
            return;
        }
        if (cdkPath == null || cdkPath.isEmpty()) {
            throw new IllegalArgumentException("CDK path must not be empty");
        }
        String targetCdkPath = String.join("/", cdkPath) + "/" + root.getOwnPidPath();
        String sourcePathQuery =
                FIELD_OWN_PID_PATH_CHILDREN + ":\""
                        + ClientUtils.escapeQueryChars(root.getOwnPidPath())
                        + "\"";
        String missingCdkPathQuery =
                "-" + FIELD_PID_PATHS_CHILDREN + ":\""
                        + ClientUtils.escapeQueryChars(targetCdkPath)
                        + "\"";
        SolrQuery query = new SolrQuery();
        query.setQuery(sourcePathQuery + " AND " + missingCdkPathQuery);
        query.setFields(
                FIELD_PID,
                FIELD_OWN_PID_PATH,
                FIELD_IN_COLLECTION,
                FIELD_IN_COLLECTION_DIRECT,
                FIELD_PID_PATHS);
        query.setRows(PAGE_SIZE);
        query.setSort(FIELD_PID, SolrQuery.ORDER.asc);
        executeCursorPagedQuery(query,
                "Cannot find unlinked subtree for PID " + rootPid + ", target CDK path " + targetCdkPath, pageConsumer);
    }

    /**
     * Finds documents whose in_collections.direct field contains
     * the specified collection PID.
     */
    @Override
    public void findByDirectCollection(String collectionPid, Consumer<List<SearchDocument>> pageConsumer) {
        SolrQuery query = new SolrQuery();
        query.setQuery(FIELD_IN_COLLECTION_DIRECT + ":"
                + ClientUtils.escapeQueryChars(collectionPid));
        query.setFields(
                FIELD_PID,
                FIELD_OWN_PID_PATH,
                FIELD_IN_COLLECTION,
                FIELD_IN_COLLECTION_DIRECT,
                FIELD_PID_PATHS);
        query.setRows(PAGE_SIZE);
        query.setSort(FIELD_PID, SolrQuery.ORDER.asc);
        executeCursorPagedQuery(
                query,
                "Cannot find documents directly assigned to CDK collection "
                        + collectionPid,
                pageConsumer);
    }

    @Override
    public void findByPidPathPrefix(String pidPathPrefix, Consumer<List<SearchDocument>> pageConsumer) {
        SolrQuery query = new SolrQuery();
        query.setQuery(
                FIELD_PID_PATHS_CHILDREN + ":\""
                        + ClientUtils.escapeQueryChars(pidPathPrefix)
                        + "\"");
        query.setFields(
                FIELD_PID,
                FIELD_OWN_PID_PATH,
                FIELD_IN_COLLECTION,
                FIELD_IN_COLLECTION_DIRECT,
                FIELD_PID_PATHS);
        query.setRows(PAGE_SIZE);
        query.setSort(FIELD_PID, SolrQuery.ORDER.asc);
        executeCursorPagedQuery(
                query,
                "Cannot find documents with PID path prefix " + pidPathPrefix,
                pageConsumer);
    }

    @Override
    public void updateDocumentFieldsBatch(List<SearchDocumentUpdate> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        List<SolrInputDocument> solrUpdates = new ArrayList<>(updates.size());
        for (SearchDocumentUpdate update : updates) {
            SolrInputDocument document = new SolrInputDocument();
            document.setField(FIELD_PID, update.getPid());
            document.setField(
                    FIELD_PID_PATHS,
                    Collections.singletonMap("set",
                            update.getPidPaths() == null ? Collections.emptyList() : update.getPidPaths()));
            document.setField(
                    FIELD_IN_COLLECTION,
                    Collections.singletonMap("set",
                            update.getInCollections() == null ? Collections.emptyList() : update.getInCollections()));
            document.setField(
                    FIELD_IN_COLLECTION_DIRECT,
                    Collections.singletonMap("set",
                            update.getInCollectionsDirect() == null ? Collections.emptyList() : update.getInCollectionsDirect()));
            solrUpdates.add(document);
        }
        try {
            solrClient.add(solrUpdates);
        } catch (SolrServerException | IOException e) {
            throw new IllegalStateException("Cannot update document fields in batch", e);
        }
    }

    @Override
    public void commit() {
        try {
            solrClient.commit();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeCursorPagedQuery(SolrQuery query, String errorMessage, Consumer<List<SearchDocument>> pageConsumer) {
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        try {
            while (true) {
                query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
                QueryResponse response = solrClient.query(query);
                SolrDocumentList documents = response.getResults();
                if (!documents.isEmpty()) {
                    List<SearchDocument> page = new ArrayList<>(documents.size());
                    for (SolrDocument document : documents) {
                        page.add(toSearchDocument(document));
                    }
                    pageConsumer.accept(page);
                }
                String nextCursorMark = response.getNextCursorMark();
                if (documents.isEmpty() || nextCursorMark == null || nextCursorMark.equals(cursorMark)) {
                    break;
                }
                cursorMark = nextCursorMark;
            }
        } catch (SolrServerException | IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private SearchDocument toSearchDocument(SolrDocument document) {
        List<String> cdkCollections = asStringList(document.getFieldValue(FIELD_CDK_COLLECTION));
        return new SearchDocument(
                asString(document.getFieldValue(FIELD_PID)),
                asString(document.getFieldValue(FIELD_OWN_PID_PATH)),
                asStringList(document.getFieldValue(FIELD_IN_COLLECTION)),
                asStringList(document.getFieldValue(FIELD_IN_COLLECTION_DIRECT)),
                asStringList(document.getFieldValue(FIELD_PID_PATHS)),
                cdkCollections.isEmpty());
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

    @Override
    public List<String> getDirectCollectionValues() {
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setRows(0);
        query.setFacet(true);
        query.addFacetField(FIELD_IN_COLLECTION_DIRECT);
        query.setFacetLimit(-1);
        query.setFacetMinCount(1);
        query.setFacetSort("index");
        try {
            QueryResponse response = solrClient.query(query);
            FacetField facet = response.getFacetField(FIELD_IN_COLLECTION_DIRECT);
            List<String> values = new ArrayList<>();
            if (facet != null && facet.getValues() != null) {
                for (FacetField.Count count : facet.getValues()) {
                    values.add(count.getName());
                }
            }
            return values;
        } catch (SolrServerException | IOException e) {
            throw new IllegalStateException("Cannot retrieve direct collection values", e);
        }
    }
}
