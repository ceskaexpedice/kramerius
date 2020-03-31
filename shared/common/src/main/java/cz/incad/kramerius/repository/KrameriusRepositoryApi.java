package cz.incad.kramerius.repository;

import cz.incad.kramerius.fedora.om.RepositoryException;
import org.dom4j.Document;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Interface for accessing data in repository (Akubra) as used by Kramerius. Methods are Kramerius - specific.
 * Built on cz.incad.kramerius.repository.RepositoryApi
 *
 * @see cz.incad.kramerius.repository.RepositoryApi
 */
public interface KrameriusRepositoryApi {

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

    //TODO: methods for getting ocr, images, audio
    //TODO: methods for updating datastream data

    /**
     * @return Low level repository API. Through that can be accessed any kind of datastream or property, regardless if it is used by Kramerius or not
     */
    public RepositoryApi getLowLevelApi();

    /**
     * @param pid Persistent identifier of the object
     * @return Timestamp of object's creation in repository.
     * @throws IOException
     * @throws RepositoryException
     */
    public LocalDateTime getTimestampCreated(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return Timestamp of object's modification in repository.
     * @throws IOException
     * @throws RepositoryException
     */
    public LocalDateTime getTimestampLastModified(String pid) throws IOException, RepositoryException;

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

}
