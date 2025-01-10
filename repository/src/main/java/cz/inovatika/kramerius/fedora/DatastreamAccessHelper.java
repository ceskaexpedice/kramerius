package cz.inovatika.kramerius.fedora;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public interface DatastreamAccessHelper {

    //--------RELS-EXT info
    /**
     * Returns pid of the first periodical Item of given object
     *
     * @param pid Object's pid = pid of volume
     * @return pid of the first periodical Item or empty string (if hasItem relationship doesn't
     * exist)
     * @throws IOException IO error has been occurred
     */
    public String getFirstItemPid(String pid) throws IOException;
    /**
     * Returns Donator parsed from given document
     *
     * @param relsExt RELS-EXT document
     * @return donator or empty string (if hasDonator relationship doesn't
     * exist)
     */
    public String getDonator(Document relsExt);
    /**
     * Returns Donator of given object
     *
     * @param pid Object's pid
     * @return donator or empty string (if hasDonator relationship doesn't
     * exist)
     * @throws IOException IO error has been occurred
     */
    public String getDonator(String pid) throws IOException;
    /**
     * Returns pid of first document through rels-ext, which has IMG_FULL
     * datastream
     *
     * @param pid Object's pid
     * @return first page pid
     * @throws IOException IO error has been occurred
     */
    public String findFirstViewablePid(String pid) throws IOException;
    /**
     * Fill path of pids and models up to first document throw rels-ext, which
     * has IMG_FULL datastream
     *
     * @param pids xxx
     * @param models xxx
     * @return true to stop recursion
     * @throws IOException IO error has been occurred
     */
    @Deprecated
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException;
    /**
     * Returns pid of the first periodical Volume parsed from given document
     * *
     * @param relsExt RELS-EXT document
     * @return pid of the first periodical Volume or empty string (if hasVolume relationship doesn't
     * exist)
     * @throws IOException IO error has been occurred
     */
    public String getFirstVolumePid(Document relsExt) throws IOException;
    /**
     * Returns pid of the first periodical Volume of given object
     *
     * @param pid Object's pid = pid of periodical
     * @return pid of the first periodical Volume or empty string (if hasVolume relationship doesn't
     * exist)
     * @throws IOException IO error has been occurred
     */
    public String getFirstVolumePid(String pid) throws IOException;

    //-------------- dsId
    //public boolean datastreamExists(String pid, String dsId) throws RepositoryException, IOException;
    /**
     * For observe HTTP headers
     * @param pid Requested pid
     * @param datastreamName Data stream name
     * @param streamObserver Header fileds observer
     * @throws IOException
     */
    public void observeStreamHeaders(String pid, String datastreamName, StreamHeadersObserver streamObserver) throws IOException;
    /**
     * Returns URL of external datastream data
     *
     * @param pid pid of reqested object
     * @param datastreamName datastream name
     * @return URL of external datastream data location
     * @throws IOException IO error has been occurred
     */
    public String getExternalStreamURL(String pid, String datastreamName) throws IOException;
    /**
     * TODO: Not used
     * Returns profile for stream
     *
     * @param pid Requested pid
     * @param stream Requested stream
     * @return Parsed profile
     * @throws IOException IO error has been occurred
     */
    Document getStreamProfile(String pid, String stream) throws IOException;

    // ------------- content
    /**
     * Returns data of IMG_PREVIEW stream
     *
     * @param pid PID of requested object
     * @return data of IMG_PREVIEW stream
     * @throws IOException IO error has been occurred
     */
    public InputStream getFullThumbnail(String pid) throws IOException;
    /**
     * Returns input stream of thumbnail
     *
     * @param pid Object's pid
     * @return IMG_THUMBs stream
     * @throws IOException IO error has been occurred
     */
    public InputStream getSmallThumbnail(String pid) throws IOException;
    /**
     * Returns profile for thumbnail
     *
     * @param pid PID of requested object
     * @return parsed profile
     * @throws IOException IO error has been occurred
     */
    Document getSmallThumbnailProfile(String pid) throws IOException;
    /**
     * Returns data of IMG_FULL stream
     *
     * @param pid Requested pid
     * @return IMG_FULL data
     * @throws IOException IO error has been occurred
     */
    public InputStream getImageFULL(String pid) throws IOException;
    /**
     * TODO: Not used
     * Returns IMG_FULL profile
     *
     * @param pid Requested pid
     * @return parsed profile
     * @throws IOException IO error has been occurred
     */
    public Document getImageFULLProfile(String pid) throws IOException;
    /**
     * Return parsed biblio mods stream
     *
     * @param pid Object's pid
     * @return return biblio mods stream
     * @throws IOException IO erro has been occurred
     */
    public Document getBiblioMods(String pid) throws IOException;
    /**
     * Returns DC stream
     *
     * @param pid Object's pid
     * @return parsed DC stream
     * @throws IOException IO erro has been occurred
     */
    public Document getDC(String pid) throws IOException;
    /**
     * Returns parsed RELS-EXT
     *
     * @param pid Object pid
     * @return parsed RELS-EXT
     * @throws IOException IO error has been occurred
     */
    Document getRelsExt(String pid);
    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream RELS-EXT provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    org.dom4j.Document getRelsExt(String pid, boolean namespaceAware);
    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream BIBLIO_MODS provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public org.dom4j.Document getMods(String pid, boolean namespaceAware) throws IOException, RepositoryException;
    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream DC provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public org.dom4j.Document getDublinCore(String pid, boolean namespaceAware) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return latest version of object's datastream OCR_TEXT provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getOcrText(String pid) throws IOException, RepositoryException;
    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream OCR_ALTO provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public org.dom4j.Document getOcrAlto(String pid, boolean namespaceAware) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing image data from IMG_FULL if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getImgFull(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing image data from IMG_THUMB if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getImgThumb(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing image data from IMG_PREVIEW if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getImgPreview(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing audio data from MP3 if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getAudioMp3(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing audio data from OGG if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getAudioOgg(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing audio data from WAV if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getAudioWav(String pid) throws IOException, RepositoryException;

    // --------- mimeType
    /**
     * Returns mime type of mime type
     *
     * @param pid PID of requested object
     * @return mime type
     * @throws IOException IO error has been occurred
     * @throws XPathExpressionException Error during xpath evaluation has been
     * occurred
     */
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException;
    /**
     * Returns mime type of requested IMG_PREVIEW
     *
     * @param pid PID of reqested object
     * @return mimetype
     * @throws IOException IO error has been occurred
     * @throws XPathExpressionException Error during xpath evaluation has been
     * occurred
     */
    public String getFullThumbnailMimeType(String pid) throws IOException, XPathExpressionException;
    /**
     * Returns IMG_FULL mimetype
     *
     * @param pid Requested object
     * @return mime type
     * @throws IOException IO error has been occurred
     * @throws XPathExpressionException Error during xpath evaluation has been
     * occurred
     */
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException;
    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream IMG_FULL if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getImgFullMimetype(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream IMG_THUMB if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getImgThumbMimetype(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream IMG_PREVIEW if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getImgPreviewMimetype(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream MP3 if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getAudioMp3Mimetype(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream OGG if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getAudioOggMimetype(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream WAV if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getAudioWavMimetype(String pid) throws IOException, RepositoryException;

    // --- available--------------------
    /**
     * Tests whether IMG_PREVIEW is available
     *
     * @param pid PID of reqested object
     * @return true if IMG_PREVIEW is available
     * @throws IOException IO error has been occurred
     */
    public boolean isFullthumbnailAvailable(String pid) throws IOException;
    /**
     * Check whether IMG_FULL is available, is present and accessible
     *
     * @param pid Requested object
     * @return true if IMG_FULL is available
     * @throws IOException IO error has been occurred
     */
    public boolean isImageFULLAvailable(String pid) throws IOException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream RELS-EXT is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isRelsExtAvailable(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream BIBLIO_MODS is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isModsAvailable(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream DC is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isDublinCoreAvailable(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream OCR_TEXT is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isOcrTextAvailable(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream OCR_ALTO is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isOcrAltoAvailable(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream IMG_FULL is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isImgFullAvailable(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream IMG_THUMB is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isImgThumbAvailable(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream IMG_PREVIEW is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isImgPreviewAvailable(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream MP3 is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isAudioMp3Available(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream OGG is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isAudioOggAvailable(String pid) throws IOException, RepositoryException;
    /**
     * @param pid Persistent identifier of the object
     * @return if datastream WAV is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isAudioWavAvailable(String pid) throws IOException, RepositoryException;

    /**
     * TODO: Not used
     * Returns document which describes datastrem of requested pid
     *
     * @param pid requested pid
     * @return returns list of datastreams
     * @throws IOException IO error has been occurred
     */
    InputStream getFedoraDataStreamsList(String pid) throws IOException;
    /**
     * Returns document which describes datastrem of requested pid
     *
     * @param pid requested pid
     * @return returns list of datastreams in document
     * @throws IOException IO error has been occurred
     */
    Document getFedoraDataStreamsListAsDocument(String pid) throws IOException;
    Date getStreamLastmodifiedFlag(String pid, String stream) throws IOException;
    public boolean isPidAvailable(String pid) throws IOException, RepositoryException;
    /**
     * Appends new version of inline xml datastream RELS-EXT
     *
     * @param pid        Persistent identifier of the object
     * @param relsExtDoc New version of RELS-EXT
     * @throws IOException
     * @throws RepositoryException
     */
    public void updateRelsExt(String pid, org.dom4j.Document relsExtDoc) throws IOException, RepositoryException;
    /**
     * Appends new version of inline xml datastream BIBLIO_MODS
     *
     * @param pid     Persistent identifier of the object
     * @param modsDoc New version of MODS
     * @throws IOException
     * @throws RepositoryException
     */
    public void updateMods(String pid, org.dom4j.Document modsDoc) throws IOException, RepositoryException;
    /**
     * Appends new version of inline xml datastream DC
     *
     * @param pid   Persistent identifier of the object
     * @param dcDoc New version of Dublin Core
     * @throws IOException
     * @throws RepositoryException
     */
    public void updateDublinCore(String pid, org.dom4j.Document dcDoc) throws IOException, RepositoryException;
    /**
     * Returns model's name of given object relsext
     *
     * @param relsExt parsed RELS-EXT stream
     * @return model's name
     * @throws IOException IO error has been occurred
     */
    public String getKrameriusModelName(Document relsExt) throws IOException;
    /**
     * Returns model's name of given pid
     *
     * @param pid Object's pid
     * @return model's name
     * @throws IOException IO error has been occurred
     */
    public String getKrameriusModelName(String pid) throws IOException;
    /**
     * Returns models parsed from given document
     *
     * @param relsExt RELS-EXT document
     * @return models from RELS-EXt
     */
    public List<String> getModelsOfRel(Document relsExt);
    /**
     * Returns models of given object
     *
     * @param pid Object's pid
     * @return modelsget
     * @throws IOException IO error has been occurred
     */
    public List<String> getModelsOfRel(String pid) throws IOException;
    /**
     * Parse, find and returns all pages
     *
     * @param pid Object's pid
     * @param deep if should go into deep
     * @return all pages
     * @throws IOException IO error has been occurred
     */
    @Deprecated
    public List<Element> getPages(String pid, boolean deep) throws IOException;
    /**
     * Find and returns all pages
     *
     * @param pid pid of object
     * @param rootElementOfRelsExt Root element of RelsExt
     * @return all pages
     * @throws IOException IO error has been occurred
     */
    @Deprecated
    public List<Element> getPages(String pid, Element rootElementOfRelsExt) throws IOException;


}
