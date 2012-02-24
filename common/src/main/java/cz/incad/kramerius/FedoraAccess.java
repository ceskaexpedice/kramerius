package cz.incad.kramerius;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
     * @param pid
     *            Object pid
     * @return
     * @throws IOException
     */
    public Document getRelsExt(String pid) throws IOException;

    /**
     * Returns model's name of given object relsext
     * @param relsExt
     * @return
     * @throws IOException 
     */
    public String getKrameriusModelName(Document relsExt) throws IOException;
    
    /**
     * Returns model's name of given pid
     * @param pid
     * @return
     * @throws IOException 
     */
    public String getKrameriusModelName(String pid) throws IOException;
    

    

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
     * @param pid
     *            pid of object
     * @return
     * @throws IOException
     */
    public List<String> getModelsOfRel(String pid) throws IOException;
    
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
     * @param pid
     *            pid of object
     * @return
     * if hasDonator dosn't exist return empty string
     * @throws IOException
     */
    public String getDonator(String pid) throws IOException;


    /**
     * Return parsed biblio mods stream
     * 
     * @param pid
     * @return
     * @throws IOException
     */
    public Document getBiblioMods(String pid) throws IOException;

    /**
     * Returns DC stream
     * 
     * @param pid
     * @return
     * @throws IOException
     */
    public Document getDC(String pid) throws IOException;


    /**
     * Returns pid of first document through rels-ext, which has IMG_FULL datastream
     * @param pid
     * @return
     * @throws IOException
     */
    public String findFirstViewablePid(String pid) throws IOException;


    /**
     * Fill path of pids and models up to first document throw rels-ext, which has IMG_FULL datastream
     * @param pid
     * @return true to stop recursion
     * @throws IOException
    */
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException;

    
    

    /**
     * Parse, find and returns all pages
     * 
     * @param pid
     *            pid of object
     * @return
     */
    @Deprecated
    public List<Element> getPages(String pid, boolean deep) throws IOException;

    /**
     * Find and returns all pages
     * 
     * @param pid
     *            pid of object
     * @param rootElementOfRelsExt
     *            Root element of RelsExt
     * @return
     * @throws IOException
     */
    @Deprecated
    public List<Element> getPages(String pid, Element rootElementOfRelsExt) throws IOException;

    /**
     * Returns input stream of thumbnail
     * 
     * @param pid
     * @return
     * @throws IOException
     */
    public InputStream getSmallThumbnail(String pid) throws IOException;

    /**
     * Returns profile for thumbnail
     * @param pid PID of requested object
     * @return
     * @throws IOException
     */
    Document getSmallThumbnailProfile(String pid) throws IOException;

    /**
     * Returns mime type of mime type
     * @param pid PID of requested object
     * @return
     * @throws IOException
     * @throws XPathExpressionException
     */
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException;

    /**
     * Tests whether IMG_PREVIEW is available
     * @param pid PID of reqested object
     * @return
     * @throws IOException
     */
    public boolean isFullthumbnailAvailable(String pid) throws IOException;

    /**
     * Returns data of IMG_PREVIEW stream
     * @param pid PID of requested object
     * @return
     * @throws IOException
     */
    public InputStream getFullThumbnail(String pid) throws IOException;

    
    /**
     * Returns mime type of requested IMG_PREVIEW
     * @param pid PID of reqested object
     * @return
     * @throws IOException
     * @throws XPathExpressionException
     */
    public String getFullThumbnailMimeType(String pid) throws IOException, XPathExpressionException;

    /**
     * Returns djvu image of the object
     * 
     * @param pid
     * @return
     * @throws IOException
     */
    public InputStream getImageFULL(String pid) throws IOException;

    /**
     * REturns profile of full image stream
     * 
     * @param pid
     * @return
     * @throws IOException
     */
    public Document getImageFULLProfile(String pid) throws IOException;

    /**
     * Returns full image mime type
     * 
     * @param pid
     * @return
     * @throws IOException
     * @throws XPathExpressionException
     */
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException;

    /**
     * Check whether full image is available, is present and accessible
     * 
     * @param pid
     * @return
     * @throws IOException
     */
    public boolean isImageFULLAvailable(String pid) throws IOException;

    
    /**
     * Check whether stream is available, is present and accessible
     * 
     * @param pid
     * @param streamName Stream name
     * @return
     * @throws IOException
     */
    public boolean isStreamAvailable(String pid, String streamName) throws IOException;


    
    /**
     * Checks whetere content is acessiable
     * 
     * @param pid
     *            pid of object which can be protected
     * @return
     * @throws IOException
     */
    public boolean isContentAccessible(String pid) throws IOException;

    /**
     * Returns API-A stub
     * @return
     */
    public FedoraAPIA getAPIA();

    /**
     * Returns API-M stub
     * @return
     */
    public FedoraAPIM getAPIM();

    public ObjectFactory getObjectFactory();

    /**
     * Process fedora object tree
     * @param pid PID of processing object
     * @param processor Processing tree handler (receives callbacks)
     * @throws ProcessSubtreeException
     * @throws IOException
     */
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException;

    public Set<String> getPids(String pid) throws IOException;

    /**
     * Returns inputStream of given datastream 
     * @param pid pid of reqested object 
     * @param datastreamName datastream name
     * @return 
     * @throws IOException
     */
    public InputStream getDataStream(String pid, String datastreamName) throws IOException;

    //public void copyDataStreamContent(String pid, String datastreamName, )
    
    /**
     * Returns mimeType of given datastream
     * @param pid pid of requested object
     * @param datastreamName Datastream name
     * @return
     * @throws IOException
     */
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException;

 
    /**
     * Returns current version of fedora
     * @return
     * @throws IOException 
     */
    public String getFedoraVersion() throws IOException;

    
    /**
     * Returns profile for stream 
     * @param pid
     * @param stream
     * @return
     * @throws IOException
     */
    Document getStreamProfile(String pid, String stream) throws IOException;

    /**
     * Datastreams description document
     * @param pid PID of requested object
     * @return
     * @throws IOException
     */
    Document getObjectProfile(String pid) throws IOException;

    
    /**
     * Returns document which describes datastrem of requested pid
     * @param pid requested pid
     * @return
     * @throws IOException 
     */
    InputStream getFedoraDataStreamsList(String pid ) throws IOException;

}
