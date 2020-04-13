package cz.incad.kramerius.repository;

import cz.incad.kramerius.fedora.om.RepositoryException;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;

import java.io.IOException;
import java.util.List;

/**
 * Interface for accessing data in repository (Akubra) as used by Kramerius. Methods are Kramerius - specific.
 * Built on cz.incad.kramerius.repository.RepositoryApi
 *
 * @see cz.incad.kramerius.repository.RepositoryApi
 */
public interface KrameriusRepositoryApi {

    public static class KnownXmlFormatUris {
        public static final String RELS_EXT = "info:fedora/fedora-system:FedoraRELSExt-1.0";
        public static final String BIBLIO_MODS = "http://www.loc.gov/mods/v3";
        public static final String BIBLIO_DC = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    }

    enum KnownDatastreams {
        RELS_EXT("RELS-EXT"),

        BIBLIO_MODS("BIBLIO_MODS"),
        BIBLIO_DC("DC"),

        OCR_ALTO("ALTO"),
        OCR_TEXT("TEXT_OCR"),

        IMG_FULL("IMG_FULL"),
        IMG_THUMB("IMG_THUMB"),
        IMG_PREVIEW("IMG_PREVIEW"),

        AUDIO_MP3("MP3"),
        AUDIO_OGG("OGG"),
        AUDIO_WAV("WAV"),

        POLICY("POLICY"),

        MIGRATION("MIGRATION");

        private final String value;

        KnownDatastreams(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    enum KnownRelations {
        //own relations (define object tree)
        HAS_PAGE("hasPage"),
        HAS_UNIT("hasUnit"), //monograph -> monographUnit
        HAS_VOLUME("hasVolume"), //periodical -> periodicalVolume
        HAS_ITEM("hasItem"), //periodical -> (periodicalItem, supplement)
        HAS_SOUND_UNIT("hasSoundUnit"), //soundRecording -> soundUnit
        HAS_TRACK("hasTrack"), //(soundRecording, soundUnit) -> track
        CONTAINS_TRACK("containsTrack"), //old version of HAS_TRACK
        HAS_INT_COMP_PART("hasIntCompPart"), //periodicalItem  -> (internalPart, article)
        //foster relations
        IS_ON_PAGE("isOnPage"), //(article, internalPart) -> page
        CONTAINS("contains"); //collection -> (monograph, periodical, ... anything, even other collection)

        private final String value;

        KnownRelations(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    //TODO: methods for getting ocr, images, audio
    //TODO: methods for updating datastream data (done for inline xml datastreams)

    /**
     * @return Low level repository API. Through that can be accessed any kind of datastream or property, regardless if it is used by Kramerius or not
     */
    public RepositoryApi getLowLevelApi();

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream RELS-EXT is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isRelsExtAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream RELS-EXT, provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public Document getRelsExt(String pid, boolean namespaceAware) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream BIBLIO_MODS is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isModsAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream BIBLIO_MODS, provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public Document getMods(String pid, boolean namespaceAware) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream DC is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isDublinCoreAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream DC, provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public Document getDublinCore(String pid, boolean namespaceAware) throws IOException, RepositoryException;

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
     * Appends new version of inline xml datastream RELS-EXT
     *
     * @param pid        Persistent identifier of the object
     * @param relsExtDoc New version of RELS-EXT
     * @throws IOException
     * @throws RepositoryException
     */
    public void updateRelsExt(String pid, Document relsExtDoc) throws IOException, RepositoryException;

    /**
     * Appends new version of inline xml datastream BIBLIO_MODS
     *
     * @param pid     Persistent identifier of the object
     * @param modsDoc New version of MODS
     * @throws IOException
     * @throws RepositoryException
     */
    public void updateMods(String pid, Document modsDoc) throws IOException, RepositoryException;

    /**
     * Appends new version of inline xml datastream DC
     *
     * @param pid   Persistent identifier of the object
     * @param dcDoc New version of Dublin Core
     * @throws IOException
     * @throws RepositoryException
     */
    public void updateDublinCore(String pid, Document dcDoc) throws IOException, RepositoryException;

}
