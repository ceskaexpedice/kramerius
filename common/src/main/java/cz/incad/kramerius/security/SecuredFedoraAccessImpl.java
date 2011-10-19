package cz.incad.kramerius.security;

import java.io.File;
import java.io.FileInputStream;
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

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
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

    public Document getBiblioMods(String pid) throws IOException {
        return rawAccess.getBiblioMods(pid);
    }

    public Document getDC(String pid) throws IOException {
        return rawAccess.getDC(pid);
    }

    public String findFirstViewablePid(String pid) throws IOException {
        return rawAccess.findFirstViewablePid(pid);
    }

    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException{
        return rawAccess.getFirstViewablePath(pids, models);
    }

    public InputStream getImageFULL(String pid) throws IOException {

        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        for (ObjectPidsPath path : paths) {
            if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, FedoraUtils.IMG_FULL_STREAM, path)) {
                return rawAccess.getImageFULL(pid);
            }
        }
        throw new SecurityException("access denided");
        
    }


    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        return rawAccess.getImageFULLMimeType(pid);
    }

    public Document getImageFULLProfile(String pid) throws IOException {
        return rawAccess.getImageFULLProfile(pid);
    }


    public List<String> getModelsOfRel(Document relsExt){
        return rawAccess.getModelsOfRel(relsExt);
    }
    
    public List<String> getModelsOfRel(String pid) throws IOException{
        return rawAccess.getModelsOfRel(pid);
    }

    public String getDonator(Document relsExt) {
        return rawAccess.getDonator(relsExt);
    }

    public String getDonator(String pid) throws IOException {
        return rawAccess.getDonator(pid);
    }

    public List<Element> getPages(String pid, boolean deep) throws IOException {
        return rawAccess.getPages(pid, deep);
    }

    public List<Element> getPages(String pid, Element rootElementOfRelsExt) throws IOException {
        return rawAccess.getPages(pid, rootElementOfRelsExt);
    }

    public Document getRelsExt(String pid) throws IOException {
        return rawAccess.getRelsExt(pid);
    }

    public InputStream getSmallThumbnail(String pid) throws IOException {
        return rawAccess.getSmallThumbnail(pid);
    }

    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return rawAccess.getSmallThumbnailMimeType(pid);
    }

    public Document getSmallThumbnailProfile(String pid) throws IOException {
        return rawAccess.getSmallThumbnailProfile(pid);
    }

    public boolean isImageFULLAvailable(String pid) throws IOException {
        // not checked method
        return rawAccess.isImageFULLAvailable(pid);
    }

    @Override
    public boolean isContentAccessible(String pid) throws IOException {
        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        for (ObjectPidsPath path : paths) {
            if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, FedoraUtils.IMG_FULL_STREAM, path)) {
                return true;
            }
        }
        return false;
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

    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException {
        rawAccess.processSubtree(pid, processor);
    }

    public Set<String> getPids(String pid) throws IOException {
        return rawAccess.getPids(pid);
    }

    
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        for (int i = 0; i < paths.length; i++) {
            if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, datastreamName, paths[i])) { 
                return rawAccess.getDataStream(pid, datastreamName);
            }
        }
        throw new SecurityException("access denided");
    }

    
    
    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        return this.rawAccess.isStreamAvailable(pid, streamName);
    }
    
    
    @Override
    public boolean isStreamAccessible(String pid, String streamName) throws IOException {
        boolean accessed = false;
        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        for (ObjectPidsPath path : paths) {
            if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, streamName, path)) {
                accessed  = true;
                break;
            }
        }
        return accessed ? this.isStreamAvailable(pid, streamName) : false;
    }

    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        return rawAccess.getMimeTypeForStream(pid, datastreamName);
    }

    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        boolean accessed = false;
        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        for (ObjectPidsPath path : paths) {
            if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, FedoraUtils.IMG_PREVIEW_STREAM, path)) {
                accessed  = true;
                break;
            }
        }
        
        if (accessed) {
            if (this.isStreamAvailable(pid, FedoraUtils.IMG_PREVIEW_STREAM)) {
                return rawAccess.getFullThumbnail(pid);
            } else throw new IOException("preview not found");
        } else {
            throw new SecurityException("access denided");
        }
        
        
    }


    @Override
    public String getFullThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return "image/jpeg";
    }

    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        return (this.isStreamAvailable(pid, FedoraUtils.IMG_PREVIEW_STREAM));
    }

    @Override
    public String getKrameriusModelName(Document relsExt) throws IOException {
        return rawAccess.getKrameriusModelName(relsExt);
    }

    @Override
    public String getKrameriusModelName(String pid) throws IOException {
        return rawAccess.getKrameriusModelName(pid);
    }

    @Override
    public String getFedoraVersion() throws IOException {
        return rawAccess.getFedoraVersion();
    }

    public Document getStreamProfile(String pid, String stream) throws IOException {
        return rawAccess.getStreamProfile(pid, stream);
    }

    public Document getObjectProfile(String pid) throws IOException {
        return rawAccess.getObjectProfile(pid);
    }
}
