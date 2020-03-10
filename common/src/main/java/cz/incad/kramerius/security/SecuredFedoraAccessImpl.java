/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.security;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
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
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * This is secured variant of class FedoraAccessImpl {@link FedoraAccessImpl}.
 * <br>
 *
 * @author pavels
 */
public class SecuredFedoraAccessImpl implements FedoraAccess {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SecuredFedoraAccessImpl.class.getName());
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

    @Override
    public Document getBiblioMods(String pid) throws IOException {
        return rawAccess.getBiblioMods(pid);
    }

    @Override
    public Document getDC(String pid) throws IOException {
        return rawAccess.getDC(pid);
    }

    @Override
    public String findFirstViewablePid(String pid) throws IOException {
        return rawAccess.findFirstViewablePid(pid);
    }

    @Override
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException {
        return rawAccess.getFirstViewablePath(pids, models);
    }

    @Override
    public InputStream getImageFULL(String pid) throws IOException {

        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        for (ObjectPidsPath path : paths) {
            if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, FedoraUtils.IMG_FULL_STREAM, path)) {
                return rawAccess.getImageFULL(pid);
            }
        }
        throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.READ,pid,FedoraUtils.IMG_FULL_STREAM));
    }

    @Override
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        return rawAccess.getImageFULLMimeType(pid);
    }

    @Override
    public Document getImageFULLProfile(String pid) throws IOException {
        return rawAccess.getImageFULLProfile(pid);
    }

    @Override
    public List<String> getModelsOfRel(Document relsExt) {
        return rawAccess.getModelsOfRel(relsExt);
    }

    @Override
    public List<String> getModelsOfRel(String pid) throws IOException {
        return rawAccess.getModelsOfRel(pid);
    }

    @Override
    public String getDonator(Document relsExt) {
        return rawAccess.getDonator(relsExt);
    }

    @Override
    public String getDonator(String pid) throws IOException {
        return rawAccess.getDonator(pid);
    }
    
    @Override
    public String getFirstItemPid(Document relsExt) throws IOException {
        return rawAccess.getFirstItemPid(relsExt);
    }

    @Override
    public String getFirstItemPid(String pid) throws IOException {
        return rawAccess.getFirstItemPid(pid);
    }
    
    @Override
    public String getFirstVolumePid(Document relsExt) throws IOException {
        return rawAccess.getFirstItemPid(relsExt);
    }

    @Override
    public String getFirstVolumePid(String pid) throws IOException {
        return rawAccess.getFirstItemPid(pid);
    }

    @Override
    public List<Element> getPages(String pid, boolean deep) throws IOException {
        return rawAccess.getPages(pid, deep);
    }

    @Override
    public List<Element> getPages(String pid, Element rootElementOfRelsExt) throws IOException {
        return rawAccess.getPages(pid, rootElementOfRelsExt);
    }

    @Override
    public Document getRelsExt(String pid) throws IOException {
        return rawAccess.getRelsExt(pid);
    }

    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        return rawAccess.getSmallThumbnail(pid);
    }

    @Override
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return rawAccess.getSmallThumbnailMimeType(pid);
    }

    @Override
    public Document getSmallThumbnailProfile(String pid) throws IOException {
        return rawAccess.getSmallThumbnailProfile(pid);
    }

    @Override
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

    @Override
    public FedoraAPIA getAPIA() {
        return rawAccess.getAPIA();
    }

    @Override
    public FedoraAPIM getAPIM() {
        return rawAccess.getAPIM();
    }

    @Override
    public ObjectFactory getObjectFactory() {
        return rawAccess.getObjectFactory();
    }

    @Override
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException {
        rawAccess.processSubtree(pid, processor);
    }

    @Override
    public Set<String> getPids(String pid) throws IOException {
        return rawAccess.getPids(pid);
    }

    static boolean isDefaultSecuredStream(String streamName) {
        return FedoraUtils.IMG_FULL_STREAM.equals(streamName)
                || FedoraUtils.IMG_PREVIEW_STREAM.equals(streamName)
                || FedoraUtils.TEXT_OCR_STREAM.equals(streamName)
                || FedoraUtils.MP3_STREAM.equals(streamName)
                || FedoraUtils.WAV_STREAM.equals(streamName)
                || FedoraUtils.ALTO_STREAM.equals(streamName)
                || FedoraUtils.OGG_STREAM.equals(streamName);
    }

    
    
    @Override
    public void observeStreamHeaders(String pid, String datastreamName,
            StreamHeadersObserver streamObserver) throws IOException {
        this.rawAccess.observeStreamHeaders(pid, datastreamName, streamObserver);
    }

    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        if (isDefaultSecuredStream(datastreamName)) {
            ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
            for (int i = 0; i < paths.length; i++) {
                if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, datastreamName, paths[i])) {
                    return rawAccess.getDataStream(pid, datastreamName);
                }
            }
            
            throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.READ,pid,datastreamName));
        } else {
            String[] securedStreamsExtension = KConfiguration.getInstance().getSecuredAditionalStreams();
            int indexOf = Arrays.asList(securedStreamsExtension).indexOf(datastreamName);
            if (indexOf >= 0) {
                ObjectPidsPath[] paths = this.solrAccess.getPath(pid + "/" + datastreamName);
                for (int i = 0; i < paths.length; i++) {
                    if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, datastreamName, paths[i])) {
                        return rawAccess.getDataStream(pid, datastreamName);
                    }
                }
                throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.READ,pid,datastreamName));
            } else {
                return rawAccess.getDataStream(pid, datastreamName);
            }
        }
    }

    @Override
    public InputStream getDataStreamXml(String pid, String datastreamName) throws IOException {
        return rawAccess.getDataStreamXml(pid, datastreamName);
    }

    @Override
    public Document getDataStreamXmlAsDocument(String pid, String datastreamName) throws IOException {
        return rawAccess.getDataStreamXmlAsDocument(pid, datastreamName);
    }

    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        return this.rawAccess.isStreamAvailable(pid, streamName);
    }
    

    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        return this.rawAccess.isObjectAvailable(pid);
    }

    @Override
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        return rawAccess.getMimeTypeForStream(pid, datastreamName);
    }

    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        boolean accessed = false;
        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        for (ObjectPidsPath path : paths) {
            if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, FedoraUtils.IMG_PREVIEW_STREAM, path)) {
                accessed = true;
                break;
            }
        }

        if (accessed) {
            if (this.isStreamAvailable(pid, FedoraUtils.IMG_PREVIEW_STREAM)) {
                return rawAccess.getFullThumbnail(pid);
            } else {
                throw new IOException("preview not found");
            }
        } else {
            throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.READ,pid,FedoraUtils.IMG_PREVIEW_STREAM));
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

    @Override
    public Document getStreamProfile(String pid, String stream) throws IOException {
        return rawAccess.getStreamProfile(pid, stream);
    }

    @Override
    public Document getObjectProfile(String pid) throws IOException {
        return rawAccess.getObjectProfile(pid);
    }

    @Override
    public InputStream getFedoraDataStreamsList(String pid) throws IOException {
        return rawAccess.getFedoraDataStreamsList(pid);
    }

    @Override
    public Document getFedoraDataStreamsListAsDocument(String pid) throws IOException {
        return rawAccess.getFedoraDataStreamsListAsDocument(pid);
    }
}
