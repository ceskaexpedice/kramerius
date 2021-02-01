package cz.incad.kramerius.repository;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Interface for accessing data in repository (Akubra, formerly Fedora).
 * Only subset of Fedora's API (API-A, API-M) is implemented, as needed.
 * This is independent from domain-specific logic built on the repository (i.e. Kramerius),
 * as it should be with regard to separation of abstraction levels.
 *
 * @link https://wiki.lyrasis.org/display/FEDORA38/REST+API
 * @see cz.incad.kramerius.repository.KrameriusRepositoryApi
 */
public interface RepositoryApi {

    public static final String NAMESPACE_FOXML = "info:fedora/fedora-system:def/foxml#";
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    //TODO: methods for fetching other types of datastreams (redirect, external referenced, probably not managed)
    //TODO: methods for updating datastreams (new versions)

    //CREATE
    public void ingestObject(Document foxmlDoc) throws RepositoryException, IOException;

    //READ
    public boolean objectExists(String pid) throws RepositoryException;

    public String getProperty(String pid, String propertyName) throws IOException, RepositoryException;

    public String getPropertyLabel(String pid) throws IOException, RepositoryException;

    public LocalDateTime getPropertyCreated(String pid) throws IOException, RepositoryException;

    public LocalDateTime getPropertyLastModified(String pid) throws IOException, RepositoryException;

    public Document getFoxml(String pid) throws RepositoryException, IOException;

    public boolean datastreamExists(String pid, String dsId) throws RepositoryException, IOException;

    public String getDatastreamMimetype(String pid, String dsId) throws RepositoryException, IOException;

    public InputStream getLatestVersionOfDatastream(String pid, String dsId) throws RepositoryException, IOException;

    public Document getLatestVersionOfInlineXmlDatastream(String pid, String dsId) throws RepositoryException, IOException;

    public String getLatestVersionOfManagedTextDatastream(String pid, String dsId) throws RepositoryException, IOException;

    public List<String> getPidsOfAllObjects() throws RepositoryException, IOException, SolrServerException;

    public List<String> getPidsOfObjectsByModel(String model) throws RepositoryException, IOException, SolrServerException;

    public List<Pair<String, String>> getPidsOfObjectsWithTitlesByModel(String model, boolean ascendingOrder, int offset, int limit) throws RepositoryException, IOException, SolrServerException;

    public Map<String, String> getDescription(String objectPid) throws RepositoryException, IOException, SolrServerException;

    public List<String> getTripletTargets(String sourcePid, String relation) throws RepositoryException, IOException, SolrServerException;

    public List<Triplet> getTripletTargets(String sourcePid) throws RepositoryException, IOException, SolrServerException;

    public List<String> getTripletSources(String relation, String targetPid) throws RepositoryException, IOException, SolrServerException;

    public List<Triplet> getTripletSources(String targetPid) throws RepositoryException, IOException, SolrServerException;

    //UPDATE
    public void updateInlineXmlDatastream(String pid, String dsId, Document streamDoc, String formatUri) throws RepositoryException, IOException;

    //DELETE
    public void deleteObject(String pid) throws RepositoryException, IOException;


    class Triplet {
        public final String source;
        public final String relation;
        public final String target;

        public Triplet(String source, String relation, String target) {
            this.source = source;
            this.relation = relation;
            this.target = target;
        }

        @Override
        public String toString() {
            return String.format("%s -%s-> %s", source, relation, target);
        }
    }

}

