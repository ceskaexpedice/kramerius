package cz.inovatika.kramerius.fedora;

import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProcessingIndexAccessHelper {

    public List<String> getPidsOfAllObjects() throws RepositoryException, IOException, SolrServerException;

    public List<String> getPidsOfObjectsByModel(String model) throws RepositoryException, IOException, SolrServerException;

    public Pair<Long, List<String>> getPidsOfObjectsByModel(String model, int rows, int pageIndex) throws RepositoryException, IOException, SolrServerException;

    public Pair<Long, List<String>> getPidsOfObjectsByModel(String model, String titlePrefix, int rows, int pageIndex) throws RepositoryException, IOException, SolrServerException;

    public RepositoryApi.TitlePidPairs getPidsOfObjectsWithTitlesByModel(String model, boolean ascendingOrder, int offset, int limit) throws RepositoryException, IOException, SolrServerException;

    public RepositoryApi.TitlePidPairs getPidsOfObjectsWithTitlesByModelWithCursor(String model, boolean ascendingOrder, String cursor, int limit) throws RepositoryException, IOException, SolrServerException;

    public Map<String, String> getDescription(String objectPid) throws RepositoryException, IOException, SolrServerException;

    public List<String> getTripletTargets(String sourcePid, String relation) throws RepositoryException, IOException, SolrServerException;

    public List<RepositoryApi.Triplet> getTripletTargets(String sourcePid) throws RepositoryException, IOException, SolrServerException;

    public List<String> getTripletSources(String relation, String targetPid) throws RepositoryException, IOException, SolrServerException;

    public List<RepositoryApi.Triplet> getTripletSources(String targetPid) throws RepositoryException, IOException, SolrServerException;

    /**
     * Process fedora object tree
     *
     * @param pid PID of processing object
     * @param processor Processing tree handler (receives callbacks)
     * @throws ProcessSubtreeException Something happened during tree walking
     * @throws IOException IO error has been occurred
     */
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException;

    /**
     * @param objectPid
     * @return Pair of values: 1. Triplet of relation from own parent (or null if the object is top-level, i.e. has no parent), 2. Triplets of relations from foster parents
     * @throws RepositoryException
     * @throws IOException
     * @throws SolrServerException
     */
    public Pair<RepositoryApi.Triplet, List<RepositoryApi.Triplet>> getParents(String objectPid) throws RepositoryException, IOException, SolrServerException;

    /**
     * @param objectPid
     * @return Pair of values: 1. Triplets of relations to own children, 2. Triplets of relations to foster children
     * @throws RepositoryException
     * @throws IOException
     * @throws SolrServerException
     */
    public Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> getChildren(String objectPid) throws RepositoryException, IOException, SolrServerException;

    /**
     * @param collectionPid
     * @return Pids of items that are (directly) contained in collection
     * @throws RepositoryException
     * @throws IOException
     * @throws SolrServerException
     */
    public List<String> getPidsOfItemsInCollection(String collectionPid) throws RepositoryException, IOException, SolrServerException;

    /**
     * @param itemPid
     * @return Pids of collections that (directly) contain the item
     * @throws RepositoryException
     * @throws IOException
     * @throws SolrServerException
     */
    public List<String> getPidsOfCollectionsContainingItem(String itemPid) throws RepositoryException, IOException, SolrServerException;

    /**
     * @param objectPid
     * @return Model of the object
     * @throws RepositoryException
     * @throws IOException
     * @throws SolrServerException
     */
    public String getModel(String objectPid) throws RepositoryException, IOException, SolrServerException;


}
