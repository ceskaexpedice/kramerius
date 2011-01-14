package cz.incad.kramerius.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * This is secured variant of class FedoraAccessImpl {@link FedoraAccessImpl}. <br>
 * Only three methos are secured:
 * <ul>
 * <li>FedoraAccess#getImageFULL(String)</li>
 * <li>FedoraAccess#isImageFULLAvailable(String)</li>
 * <li>FedoraAccess#getImageFULLMimeType(String)</li>
 * </ul>
 * 
 * @see FedoraAccess#getImageFULL(String)
 * @see FedoraAccess#isImageFULLAvailable(String)
 * @see FedoraAccess#getImageFULLMimeType(String)
 * @author pavels
 */
public class SecuredFedoraAccessImpl implements FedoraAccess {

    private FedoraAccess rawAccess;
    private IsActionAllowed isActionAllowed;
    private SolrAccess solrAccess;
    
    private DiscStrucutreForStore discStrucutreForStore;

    @Inject
    public SecuredFedoraAccessImpl(@Named("rawFedoraAccess") FedoraAccess rawAccess, DiscStrucutreForStore discStrucutreForStore, SolrAccess solrAccess, IsActionAllowed actionAllowed) {
        super();
        this.rawAccess = rawAccess;
        this.discStrucutreForStore = discStrucutreForStore;
        this.solrAccess = solrAccess;
        this.isActionAllowed = actionAllowed;
    }

    public Document getBiblioMods(String uuid) throws IOException {
        return rawAccess.getBiblioMods(uuid);
    }

    public Document getDC(String uuid) throws IOException {
        return rawAccess.getDC(uuid);
    }

    public InputStream getImageFULL(String uuid) throws IOException {
        String[] pathOfUUIDs = this.solrAccess.getPathOfUUIDs(uuid);
        if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), uuid, pathOfUUIDs)) {
            return rawAccess.getImageFULL(uuid);
        } else throw new SecurityException("access denided");
    }


    public String getImageFULLMimeType(String uuid) throws IOException, XPathExpressionException {
        String[] pathOfUUIDs = this.solrAccess.getPathOfUUIDs(uuid);
        if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), uuid, pathOfUUIDs)) {
            return rawAccess.getImageFULLMimeType(uuid);
        } else throw new SecurityException("access denided");
    }

    public Document getImageFULLProfile(String uuid) throws IOException {
        return rawAccess.getImageFULLProfile(uuid);
    }

    public KrameriusModels getKrameriusModel(Document relsExt) {
        return rawAccess.getKrameriusModel(relsExt);
    }

    public KrameriusModels getKrameriusModel(String uuid) throws IOException {
        return rawAccess.getKrameriusModel(uuid);
    }

    public List<Element> getPages(String uuid, boolean deep) throws IOException {
        return rawAccess.getPages(uuid, deep);
    }

    public List<Element> getPages(String uuid, Element rootElementOfRelsExt) throws IOException {
        return rawAccess.getPages(uuid, rootElementOfRelsExt);
    }

    public Document getRelsExt(String uuid) throws IOException {
        return rawAccess.getRelsExt(uuid);
    }

    public InputStream getSmallThumbnail(String uuid) throws IOException {
        return rawAccess.getSmallThumbnail(uuid);
    }

    public String getSmallThumbnailMimeType(String uuid) throws IOException, XPathExpressionException {
        return rawAccess.getSmallThumbnailMimeType(uuid);
    }

    public Document getSmallThumbnailProfile(String uuid) throws IOException {
        return rawAccess.getSmallThumbnailProfile(uuid);
    }

    public boolean isImageFULLAvailable(String uuid) throws IOException {
        // not checked method
        return rawAccess.isImageFULLAvailable(uuid);
    }

    @Override
    public boolean isContentAccessible(String uuid) throws IOException {
        String[] pathOfUUIDs = this.solrAccess.getPathOfUUIDs(uuid);
        return (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), uuid, pathOfUUIDs));
    }

    public void processRelsExt(Document relsExtDocument, RelsExtHandler handler) throws IOException {
        rawAccess.processRelsExt(relsExtDocument, handler);
    }

    public void processRelsExt(String uuid, RelsExtHandler handler) throws IOException {
        rawAccess.processRelsExt(uuid, handler);
    }



    public FedoraAPIA getAPIA() {
        return rawAccess.getAPIA();
    }

    public FedoraAPIM getAPIM() {
        return rawAccess.getAPIM();
    }

    public ObjectFactory getObjectFactory() {
        return rawAccess.getObjectFactory();
    }

    public void processSubtree(String pid, TreeNodeProcessor processor) {
        rawAccess.processSubtree(pid, processor);
    }

    public Set<String> getPids(String pid) {
        return rawAccess.getPids(pid);
    }

    boolean securedStream(String streamName) {
         return FedoraUtils.IMG_FULL_STREAM.equals(streamName) || FedoraUtils.IMG_PREVIEW_STREAM.equals(streamName);
    }
    
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        //if (FedoraUtils.IMG_FULL_STREAM.equals(datastreamName))
        
        if (securedStream(datastreamName)) {
            String uuid = pid.substring("uuid:".length());
            String[] pathOfUUIDs = this.solrAccess.getPathOfUUIDs(uuid);
            if (!this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), uuid, pathOfUUIDs)) {
                throw new SecurityException("access denided");
            }
        }
        return rawAccess.getDataStream(pid, datastreamName);
    }

    
    
    @Override
    public boolean isStreamAvailable(String uuid, String streamName) throws IOException {
        return this.rawAccess.isStreamAvailable(uuid, streamName);
    }

    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        return rawAccess.getMimeTypeForStream(pid, datastreamName);
    }

    @Override
    public InputStream getFullThumbnail(String uuid) throws IOException {
        String[] pathOfUUIDs = this.solrAccess.getPathOfUUIDs(uuid);
        if (!this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), uuid, pathOfUUIDs)) {
            throw new SecurityException("access denided");
        }
        if (this.isStreamAvailable(uuid, FedoraUtils.IMG_PREVIEW_STREAM)) {
            return rawAccess.getFullThumbnail(uuid);
        } else {
            String rootPath = KConfiguration.getInstance().getConfiguration().getString("fullThumbnail.cacheDirectory", "${sys:user.home}/.kramerius4/fullThumb");
            File fullImgThumb = discStrucutreForStore.getUUIDFile(uuid, rootPath);
            if (fullImgThumb.exists()) {
                return new FileInputStream(fullImgThumb);
            } else
                throw new IOException("cannot find ");
        }
        
    }

    @Override
    public Document getFullThumbnailProfile(String uuid) throws IOException {
        throw new UnsupportedOperationException("still unsupported !");
    }

    @Override
    public String getFullThumbnailMimeType(String uuid) throws IOException, XPathExpressionException {
        return "image/jpeg";
    }

    @Override
    public boolean isFullthumbnailAvailable(String uuid) throws IOException {
        if (this.isStreamAvailable(uuid, FedoraUtils.IMG_PREVIEW_STREAM)) return true;
        String rootPath = KConfiguration.getInstance().getConfiguration().getString("fullThumbnail.cacheDirectory", "${sys:user.home}/.kramerius4/fullThumb");
        File cachedFile = this.discStrucutreForStore.getUUIDFile(uuid, rootPath);
        return cachedFile != null && cachedFile.exists();
    }
}
