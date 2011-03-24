package cz.incad.kramerius;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.security.SecuredFedoraAccessImpl;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

/**
 * This is main point to access to fedora through REST-API
 * 
 * @see FedoraAccessImpl
 * @see SecuredFedoraAccessImpl
 * @author pavels
 */
public interface FedoraAccess {

    /**
     * Returns parsed rels-ext
     * 
     * @param uuid
     *            Object uuid
     * @return
     * @throws IOException
     */
    public Document getRelsExt(String uuid) throws IOException;

    /**
     * Returns model's name of given object relsext
     * @param relsExt
     * @return
     * @throws IOException 
     */
    public String getKrameriusModelName(Document relsExt) throws IOException;
    
    /**
     * Returns model's name of given uuid
     * @param uuid
     * @return
     * @throws IOException 
     */
    public String getKrameriusModelName(String uuid) throws IOException;
    

    

    /**
     * Returns KrameriusModel parsed from given document
     *
     * @param relsExt
     *            RELS-EXT document
     * @return
     */
    public List<String> getModelsOfRel(Document relsExt);

    /**
     * Returns KrameriusModel of given object
     *
     * @param uuid
     *            uuid of object
     * @return
     * @throws IOException
     */
    public List<String> getModelsOfRel(String uuid) throws IOException;
    
    /**
     * Returns Donator parsed from given document
     * 
     * @param relsExt
     *            RELS-EXT document
     * @return
     * if hasDonator dosn't exist return empty string
     */
    public String getDonator(Document relsExt);

    /**
     * Returns Donator of given object
     * 
     * @param uuid
     *            uuid of object
     * @return
     * if hasDonator dosn't exist return empty string
     * @throws IOException
     */
    public String getDonator(String uuid) throws IOException;


    /**
     * Return parsed biblio mods stream
     * 
     * @param uuid
     * @return
     * @throws IOException
     */
    public Document getBiblioMods(String uuid) throws IOException;

    /**
     * Returns DC stream
     * 
     * @param uuid
     * @return
     * @throws IOException
     */
    public Document getDC(String uuid) throws IOException;


    /**
     * Returns uuid of first document through rels-ext, which has IMG_FULL datastream
     * @param uuid
     * @return
     * @throws IOException
     */
    public String findFirstViewablePid(String uuid) throws IOException;


    /**
     * Fill path of pids and models up to first document throw rels-ext, which has IMG_FULL datastream
     * @param uuid
     * @return true to stop recursion
     * @throws IOException
    */
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException;


    /**
     * Parse, find and returns all pages
     * 
     * @param uuid
     *            UUID of object
     * @return
     */
    @Deprecated
    public List<Element> getPages(String uuid, boolean deep) throws IOException;

    /**
     * Find and returns all pages
     * 
     * @param uuid
     *            UUID of object
     * @param rootElementOfRelsExt
     *            Root element of RelsExt
     * @return
     * @throws IOException
     */
    @Deprecated
    public List<Element> getPages(String uuid, Element rootElementOfRelsExt) throws IOException;

    /**
     * Returns input stream of thumbnail
     * 
     * @param uuid
     * @return
     * @throws IOException
     */
    public InputStream getSmallThumbnail(String uuid) throws IOException;

    Document getSmallThumbnailProfile(String uuid) throws IOException;

    public String getSmallThumbnailMimeType(String uuid) throws IOException, XPathExpressionException;

    public boolean isFullthumbnailAvailable(String uuid) throws IOException;

    public InputStream getFullThumbnail(String uuid) throws IOException;

    Document getFullThumbnailProfile(String uuid) throws IOException;

    public String getFullThumbnailMimeType(String uuid) throws IOException, XPathExpressionException;

    /**
     * Returns djvu image of the object
     * 
     * @param uuid
     * @return
     * @throws IOException
     */
    public InputStream getImageFULL(String uuid) throws IOException;

    /**
     * REturns profile of full image stream
     * 
     * @param uuid
     * @return
     * @throws IOException
     */
    public Document getImageFULLProfile(String uuid) throws IOException;

    /**
     * Returns full image mime type
     * 
     * @param uuid
     * @return
     * @throws IOException
     * @throws XPathExpressionException
     */
    public String getImageFULLMimeType(String uuid) throws IOException, XPathExpressionException;

    /**
     * Check whether full image is available, is present and accessible
     * 
     * @param uuid
     * @return
     * @throws IOException
     */
    public boolean isImageFULLAvailable(String uuid) throws IOException;

    
    /**
     * Check whether stream is available, is present and accessible
     * 
     * @param uuid
     * @param streamName Stream name
     * @return
     * @throws IOException
     */
    public boolean isStreamAvailable(String uuid, String streamName) throws IOException;

    
    /**
     * Checks whetere content is acessiable
     * 
     * @param uuid
     *            uuid of object which can be protected
     * @return
     * @throws IOException
     */
    public boolean isContentAccessible(String uuid) throws IOException;

    public FedoraAPIA getAPIA();

    public FedoraAPIM getAPIM();

    public ObjectFactory getObjectFactory();

    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException;

    public Set<String> getPids(String pid) throws IOException;

    /**
     * Returns inputStream of given datastream 
     * @param pid UUID of reqested object 
     * @param datastreamName datastream name
     * @return 
     * @throws IOException
     */
    public InputStream getDataStream(String pid, String datastreamName) throws IOException;

    /**
     * Returns mimeType of given datastream
     * @param pid UUID of requested object
     * @param datastreamName Datastream name
     * @return
     * @throws IOException
     */
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException;

}
