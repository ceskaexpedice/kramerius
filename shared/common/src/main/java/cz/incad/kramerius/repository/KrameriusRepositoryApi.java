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

    public static class KnownDatastreams {
        public static final String RELS_EXT = "RELS-EXT";

        public static final String BIBLIO_MODS = "BIBLIO_MODS";
        public static final String BIBLIO_DC = "DC";

        public static final String OCR_ALTO = "ALTO";
        public static final String OCR_TEXT = "TEXT_OCR";

        public static final String IMG_FULL = "IMG_FULL";
        public static final String IMG_THUMB = "IMG_THUMB";
        public static final String IMG_PREVIEW = "IMG_PREVIEW";

        public static final String AUDIO_MP3 = "MP3";
        public static final String AUDIO_OGG = "OGG";
        public static final String AUDIO_WAV = "WAV";

        public static final String POLICY = "POLICY";

        public static final String MIGRATION = "MIGRATION";
    }

    public static class KnownRelations {
        //own relations (define object tree)
        public static final String HAS_PAGE = "hasPage";
        public static final String HAS_UNIT = "hasUnit"; //monograph -> monographUnit
        public static final String HAS_VOLUME = "hasVolume"; //periodical -> periodicalVolume
        public static final String HAS_ITEM = "hasItem"; //periodical -> (periodicalItem, supplement)
        public static final String HAS_SOUND_UNIT = "hasSoundUnit"; //soundRecording -> soundUnit
        public static final String HAS_TRACK = "hasTrack"; //(soundRecording, soundUnit) -> track
        public static final String CONTAINS_TRACK = "containsTrack"; //old version of HAS_TRACK
        public static final String HAS_INT_COMP_PART = "hasIntCompPart"; //periodicalItem  -> (internalPart, article)
        //foster relations
        public static final String IS_ON_PAGE = "isOnPage"; //(article, internalPart) -> page
        public static final String CONTAINS = "contains"; //collection -> (monograph, periodical, ... anything, even other collection)
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
