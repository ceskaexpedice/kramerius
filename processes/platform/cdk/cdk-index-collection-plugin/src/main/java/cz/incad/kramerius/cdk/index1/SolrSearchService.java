
package cz.incad.kramerius.cdk.index1;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import org.apache.solr.client.solrj.SolrServerException;

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
    private static final String FIELD_IN_COLLECTION = "in_collections";
    private static final String FIELD_IN_COLLECTION_DIRECT = "in_collections.direct";

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

    /**
     * Finds documents in the source subtree that do not yet contain
     * the target CDK path in pid_paths.
     */
    @Override
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
        return executePagedQuery(query, "Cannot find unlinked subtree for PID " + rootPid +
                ", target CDK path " + targetCdkPath);
    }

    /**
     * Finds documents whose in_collections.direct field contains
     * the specified collection PID.
     */
    @Override
    public List<SearchDocument> findByDirectCollection(String collectionPid) {
        SolrQuery query = new SolrQuery();
        query.setQuery(FIELD_IN_COLLECTION_DIRECT + ":" + ClientUtils.escapeQueryChars(collectionPid));
        query.setFields(
                FIELD_PID,
                FIELD_OWN_PID_PATH,
                FIELD_IN_COLLECTION,
                FIELD_IN_COLLECTION_DIRECT,
                FIELD_PID_PATHS);
        query.setRows(PAGE_SIZE);
        query.setSort(FIELD_PID, SolrQuery.ORDER.asc);
        return executePagedQuery(query, "Cannot find documents directly assigned to CDK collection " + collectionPid);
    }

    /**
     * Finds documents whose pid_paths contains the specified path
     * or a descendant of that path.
     * <p>
     * This relies on pid_paths.children being populated by the
     * PathHierarchyTokenizer (or equivalent schema configuration).
     */
    @Override
    public List<SearchDocument> findByPidPathPrefix(String pidPathPrefix) {
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
        return executePagedQuery(
                query, "Cannot find documents with PID path prefix " + pidPathPrefix);
    }

    @Override
    public void updateCollectionsFields(String pid, List<String> inCollections, List<String> inCollectionsDirect) {
        SolrInputDocument update = new SolrInputDocument();
        update.setField(FIELD_PID, pid);
        update.setField(
                FIELD_IN_COLLECTION,
                Collections.singletonMap("set", inCollections == null ? Collections.emptyList() : inCollections));
        update.setField(
                FIELD_IN_COLLECTION_DIRECT,
                Collections.singletonMap("set", inCollectionsDirect == null ? Collections.emptyList() : inCollectionsDirect));
        submitUpdate(update);
    }

    @Override
    public void updatePidPaths(String pid, List<String> pidPaths) {
        SolrInputDocument update = new SolrInputDocument();
        update.setField(FIELD_PID, pid);
        update.setField(
                FIELD_PID_PATHS,
                Collections.singletonMap("set", pidPaths == null ? Collections.emptyList() : pidPaths));
        submitUpdate(update);
    }

    public void commit() {
        try {
            solrClient.commit();
        } catch (SolrServerException | IOException e) {
            throw new IllegalStateException("Cannot commit Solr updates", e);
        }
    }

    private List<SearchDocument> executePagedQuery(SolrQuery query, String errorMessage) {
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
            throw new IllegalStateException(errorMessage, e);
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
                asStringList(document.getFieldValue(FIELD_IN_COLLECTION_DIRECT)),
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
