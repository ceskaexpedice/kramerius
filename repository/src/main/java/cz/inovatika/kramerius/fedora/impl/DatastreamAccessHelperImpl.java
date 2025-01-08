package cz.inovatika.kramerius.fedora.impl;

import com.qbizm.kramerius.imp.jaxb.DatastreamVersionType;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.fedora.DatastreamAccess;
import cz.incad.kramerius.fedora.RepositoryAccess;
import cz.incad.kramerius.fedora.impl.tmp.TmpAbstractRepositoryAccess;
import cz.incad.kramerius.fedora.om.repository.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.repository.RepositoryException;
import cz.incad.kramerius.fedora.om.repository.RepositoryObject;
import cz.incad.kramerius.fedora.om.repository.impl.AkubraDOManager;
import cz.incad.kramerius.fedora.utils.AkubraUtils;
import cz.incad.kramerius.fedora.utils.FedoraUtils;
import cz.incad.kramerius.fedora.utils.pid.LexerException;
import cz.incad.kramerius.repository.utils.NamespaceRemovingVisitor;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

public class DatastreamAccessHelperImpl implements DatastreamAccess {
    @Override
    public InputStream getImgFull(String pid) throws IOException, RepositoryException {
        this.accessLog.reportAccess(pid, RepositoryAccess.KnownDatastreams.IMG_FULL.toString());
        return getLatestVersionOfDatastream(pid, RepositoryAccess.KnownDatastreams.IMG_FULL.toString());
    }
    @Override
    public InputStream getImgThumb(String pid) throws IOException, RepositoryException {
        return getLatestVersionOfDatastream(pid, RepositoryAccess.KnownDatastreams.IMG_THUMB.toString());
    }
    @Override
    public InputStream getImgPreview(String pid) throws IOException, RepositoryException {
        this.accessLog.reportAccess(pid, RepositoryAccess.KnownDatastreams.IMG_PREVIEW.toString());
        return getLatestVersionOfDatastream(pid, RepositoryAccess.KnownDatastreams.IMG_PREVIEW.toString());
    }
    @Override
    public InputStream getAudioMp3(String pid) throws IOException, RepositoryException {
        this.accessLog.reportAccess(pid, RepositoryAccess.KnownDatastreams.AUDIO_MP3.toString());
        return getLatestVersionOfDatastream(pid, RepositoryAccess.KnownDatastreams.AUDIO_MP3.toString());
    }
    @Override
    public InputStream getAudioOgg(String pid) throws IOException, RepositoryException {
        this.accessLog.reportAccess(pid, RepositoryAccess.KnownDatastreams.AUDIO_OGG.toString());
        return getLatestVersionOfDatastream(pid, RepositoryAccess.KnownDatastreams.AUDIO_OGG.toString());
    }
    @Override
    public InputStream getAudioWav(String pid) throws IOException, RepositoryException {
        return getLatestVersionOfDatastream(pid, RepositoryAccess.KnownDatastreams.AUDIO_WAV.toString());
    }
    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        try {
            return getDataStream(makeSureObjectPid(pid), FedoraUtils.IMG_PREVIEW_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }
    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        try {
            return getDataStream(makeSureObjectPid(pid), FedoraUtils.IMG_THUMB_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }
    @Override
    public InputStream getImageFULL(String pid) throws IOException {
        return getDataStream(pid, FedoraUtils.IMG_FULL_STREAM);
    }
    @Override
    public String getOcrText(String pid) throws IOException, RepositoryException {
        return getLatestVersionOfManagedTextDatastream(pid, RepositoryAccess.KnownDatastreams.OCR_TEXT.toString());
    }
    @Override
    public org.dom4j.Document getRelsExt(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        org.dom4j.Document doc = getLatestVersionOfInlineXmlDatastream(pid, RepositoryAccess.KnownDatastreams.RELS_EXT.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }
    @Override
    public Document getRelsExt(String pid) throws IOException {
        try {
            // consider to change to metadata
            return getStream(makeSureObjectPid(pid), FedoraUtils.RELS_EXT_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }
    @Override
    public org.dom4j.Document getMods(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        org.dom4j.Document doc = getLatestVersionOfInlineXmlDatastream(pid, RepositoryAccess.KnownDatastreams.BIBLIO_MODS.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }
    @Override
    public Document getBiblioMods(String pid) throws IOException {
        try {
            return getStream(makeSureObjectPid(pid), FedoraUtils.BIBLIO_MODS_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }
    @Override
    public org.dom4j.Document getDublinCore(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        org.dom4j.Document doc = getLatestVersionOfInlineXmlDatastream(pid, RepositoryAccess.KnownDatastreams.BIBLIO_DC.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }
    @Override
    public Document getDC(String pid) throws IOException {
        try {
            return getStream(makeSureObjectPid(pid), FedoraUtils.DC_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }
    @Override
    public org.dom4j.Document getOcrAlto(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        org.dom4j.Document doc = getLatestVersionOfInlineXmlDatastream(pid, RepositoryAccess.KnownDatastreams.OCR_ALTO.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }

    @Override
    public boolean isRelsExtAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.RELS_EXT.toString());
    }
    @Override
    public boolean isModsAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.BIBLIO_MODS.toString());
    }
    @Override
    public boolean isDublinCoreAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.BIBLIO_DC.toString());
    }
    @Override
    public boolean isOcrTextAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.OCR_TEXT.toString());
    }
    @Override
    public boolean isOcrAltoAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.OCR_ALTO.toString());
    }
    @Override
    public boolean isImgFullAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.IMG_FULL.toString());
    }
    @Override
    public boolean isImgThumbAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.IMG_THUMB.toString());
    }
    @Override
    public boolean isImgPreviewAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.IMG_PREVIEW.toString());
    }
    @Override
    public boolean isAudioMp3Available(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.AUDIO_MP3.toString());
    }
    @Override
    public boolean isAudioOggAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.AUDIO_OGG.toString());
    }
    @Override
    public boolean isAudioWavAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, RepositoryAccess.KnownDatastreams.AUDIO_WAV.toString());
    }
    @Override
    public boolean isImageFULLAvailable(String pid) throws IOException {
        return super.isImageFULLAvailable(pid);
    }
    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        return this.isStreamAvailable(pid, FedoraUtils.IMG_PREVIEW_STREAM);
    }

    // check stream mime
    @Override
    public String getImgFullMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, RepositoryAccess.KnownDatastreams.IMG_FULL.toString());
    }
    @Override
    public String getImgThumbMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, RepositoryAccess.KnownDatastreams.IMG_THUMB.toString());
    }
    @Override
    public String getImgPreviewMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, RepositoryAccess.KnownDatastreams.IMG_PREVIEW.toString());
    }
    @Override
    public String getAudioMp3Mimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, RepositoryAccess.KnownDatastreams.AUDIO_MP3.toString());
    }
    @Override
    public String getAudioOggMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, RepositoryAccess.KnownDatastreams.AUDIO_OGG.toString());
    }
    @Override
    public String getAudioWavMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, RepositoryAccess.KnownDatastreams.AUDIO_WAV.toString());
    }
    @Override
    public String getFullThumbnailMimeType(String pid) throws IOException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_PREVIEW_STREAM);
    }
    @Override
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_THUMB_STREAM);
    }
    @Override
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_FULL_STREAM);
    }

    // TODO individual stream properties from DatastreamVersionType and ???; make it more general
    @Override
    public String getExternalStreamURL(String pid, String datastreamName) throws IOException {
        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {

            DatastreamVersionType stream = AkubraUtils.getLastStreamVersion(object, datastreamName);

            if (stream != null) {
                if (stream.getContentLocation() != null && "URL".equals(stream.getContentLocation().getTYPE())) {
                    return stream.getContentLocation().getREF();
                } else {
                    throw new IOException("Expected external datastream: " + pid + " - " + datastreamName);
                }
            }
            throw new IOException("Datastream not found: " + pid + " - " + datastreamName);
        }
        throw new IOException("Object not found: " + pid);
    }
    @Override
    public Date getStreamLastmodifiedFlag(String pid, String streamName) throws IOException {
        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {
            DatastreamVersionType stream = AkubraUtils.getLastStreamVersion(object, streamName);
            if (stream != null) {
                if (stream.getCREATED() == null) {
                    return new Date();
                } else {
                    return stream.getCREATED().toGregorianCalendar().getTime();
                }
            }
            throw new IOException("Datastream not found: " + pid + " - " + streamName);
        }
        throw new IOException("Object not found: " + pid);
    }
    @Override
    public String getMimeTypeForStream(String pid, String streamName) throws IOException {
        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {
            DatastreamVersionType stream = AkubraUtils.getLastStreamVersion(object, streamName);
            if (stream != null) {
                return stream.getMIMETYPE();
            }
            throw new IOException("Datastream not found: " + pid + " - " + streamName);
        }
        throw new IOException("Object not found: " + pid);
    }
    @Override
    public String getDatastreamMimetype(String pid, String dsId) throws RepositoryException, IOException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            if (object != null) {
                RepositoryDatastream stream = object.getStream(dsId);
                if (stream != null) {
                    return stream.getMimeType();
                }
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }
    public String getTypeOfDatastream(String pid, String dsId) throws RepositoryException, IOException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            if (object.streamExists(dsId)) {
                RepositoryDatastream stream = object.getStream(dsId);
                return stream.getStreamType().name();
            } else {
                return null;
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void updateRelsExt(String pid, org.dom4j.Document relsExtDoc) throws IOException, RepositoryException {
        updateInlineXmlDatastream(pid, RepositoryAccess.KnownDatastreams.RELS_EXT.toString(), relsExtDoc, RepositoryAccess.KnownXmlFormatUris.RELS_EXT);
    }
    @Override
    public void updateMods(String pid, org.dom4j.Document modsDoc) throws IOException, RepositoryException {
        updateInlineXmlDatastream(pid, RepositoryAccess.KnownDatastreams.BIBLIO_MODS.toString(), modsDoc, RepositoryAccess.KnownXmlFormatUris.BIBLIO_MODS);
    }
    @Override
    public void updateDublinCore(String pid, org.dom4j.Document dcDoc) throws IOException, RepositoryException {
        updateInlineXmlDatastream(pid, RepositoryAccess.KnownDatastreams.BIBLIO_DC.toString(), dcDoc, RepositoryAccess.KnownXmlFormatUris.BIBLIO_DC);
    }

    // relsExt
    @Override
    public String getDonator(String pid) throws IOException {
        return getDonator(getRelsExt(pid));
    }
    @Override
    public String getKrameriusModelName(String pid) throws IOException {
        return getKrameriusModelName(getRelsExt(pid));
    }
    @Override
    public String findFirstViewablePid(String pid) throws IOException {
        final List<String> foundPids = new ArrayList<String>();
        try {
            processSubtree(makeSureObjectPid(pid), new TreeNodeProcessor() {
                boolean breakProcess = false;
                int previousLevel = 0;

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return breakProcess;
                }

                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }

                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    try {
                        if (previousLevel < level || level == 0) {
                            if (TmpAbstractRepositoryAccess.this.isImageFULLAvailable(pid)) {
                                foundPids.add(pid);
                                breakProcess = true;
                            }
                        } else if (previousLevel > level) {
                            breakProcess = true;
                        } else if ((previousLevel == level) && (level != 0)) {
                            breakProcess = true;
                        }
                        previousLevel = level;
                    } catch (Exception e) {
                        throw new ProcessSubtreeException(e);
                    }
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        } catch (LexerException e) {
            throw new IOException(e);
        }

        return foundPids.isEmpty() ? null : foundPids.get(0);
    }
    @Override
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException {
        try {
            pid = makeSureObjectPid(pid);
            Document relsExt = null;
            try {
                // should be from
                if (isStreamAvailable(pid, FedoraUtils.RELS_EXT_STREAM)) {
                    relsExt = getRelsExt(pid);
                } else {
                    LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + ")");
                }
            } catch (Exception ex) {
                LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + "):" + ex);
            }
            if (!processor.skipBranch(pid, 0)) {
                processSubtreeInternal(pid, relsExt, processor, 0, new Stack<String>());
            }
        } catch (LexerException e) {
            LOGGER.warning("Error in pid: " + pid);
            throw new ProcessSubtreeException(e);
        } catch (XPathExpressionException e) {
            throw new ProcessSubtreeException(e);
        }
    }
    @Override
    public List<Element> getPages(String pid, boolean deep) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getPages(pid, relsExt.getDocumentElement());
    }
    @Override
    public String getFirstItemPid(String pid) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getFirstItemPid(relsExt);
    }
    @Override
    public String getFirstVolumePid(String pid) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getFirstVolumePid(relsExt);
    }
    @Override
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException {
        try {
            String pid = pids.get(pids.size() - 1);
            pid = makeSureObjectPid(pid);
            if (isImageFULLAvailable(pid)) {
                return true;
            }
            Document relsExt = getRelsExt(pid);
            Element descEl = XMLUtils.findElement(relsExt.getDocumentElement(), "Description",
                    FedoraNamespaces.RDF_NAMESPACE_URI);
            List<Element> els = XMLUtils.getElements(descEl);
            for (Element el : els) {
                if (getTreePredicates().contains(el.getLocalName())) {
                    if (el.hasAttribute("rdf:resource")) {
                        pid = el.getAttributes().getNamedItem("rdf:resource").getNodeValue();
                        pids.add(pid);
                        models.add(getKrameriusModelName(pid));
                        if (getFirstViewablePath(pids, models)) {
                            return true;
                        } else {
                            pids.remove(pids.size() - 1);
                            models.remove(pids.size() - 1);
                        }
                    }
                }
            }
            return false;
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    /*
    // TODO current stream access methods, just one method rendering 2 content types; also we can add new par dsId and if null the whole foxml will be returned
    // <--- AkubraObject.getFoXml
    public org.dom4j.Document getDatastreamXml(String pid, String dsId) throws RepositoryException, IOException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            if (object.streamExists(dsId)) {
                org.dom4j.Document foxml = Utils.inputstreamToDocument(object.getFoxml(), true);
                org.dom4j.Element dcEl = (org.dom4j.Element) Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:datastream[@ID='%s']", dsId)).selectSingleNode(foxml);
                org.dom4j.Element detached = (org.dom4j.Element) dcEl.detach();
                org.dom4j.Document result = DocumentHelper.createDocument();
                result.add(detached);
                return result;
            } else {
                return null;
            }
        } finally {
            readLock.unlock();
        }
    }

    // <--- AkubraObject.getStream.getContent (6x)
    public InputStream getLatestVersionOfDatastream(String pid, String dsId) throws RepositoryException, IOException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            if (object.streamExists(dsId)) {
                RepositoryDatastream stream = object.getStream(dsId);
                return stream.getContent();
            } else {
                return null;
            }
        } finally {
            readLock.unlock();
        }
    }
    // <-- DigitalObject, AkubraUtils.getLastStreamVersion (3x)
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        try {
            pid = makeSureObjectPid(pid);
            if (this.accessLog != null && this.accessLog.isReportingAccess(pid, datastreamName)) {
                reportAccess(pid, datastreamName);
            }
            DigitalObject object = manager.readObjectFromStorage(pid);
            if (object != null) {
                DatastreamVersionType stream = AkubraUtils.getLastStreamVersion(object, datastreamName);
                if (stream != null) {
                    return AkubraUtils.getStreamContent(stream, manager);
                } else {
                    throw new IOException("cannot find stream '" + datastreamName + "' for pid '" + pid + "'");
                }
            } else {
                throw new IOException("cannot find pid '" + pid + "'");
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    // getLatestVersionOfDatastream (4x)
    public org.dom4j.Document getLatestVersionOfInlineXmlDatastream(String pid, String dsId) throws RepositoryException, IOException {
        InputStream is = getLatestVersionOfDatastream(pid, dsId);
        return is == null ? null : Utils.inputstreamToDocument(is, true);
    }
    // getLatestVersionOfDatastream (1x)
    public String getLatestVersionOfManagedTextDatastream(String pid, String dsId) throws RepositoryException, IOException {
        InputStream is = getLatestVersionOfDatastream(pid, dsId);
        return is == null ? null : Utils.inputstreamToString(is);
    }

    // <-- DigitalObject, AkubraUtils.getLastStreamVersion (3x)
    public Document getStream(String pid, String streamName) throws IOException {
        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {
            DatastreamVersionType stream = AkubraUtils.getLastStreamVersion(object, streamName);
            if (stream != null) {
                if (stream.getXmlContent() != null) {
                    List<Element> elementList = stream.getXmlContent().getAny();
                    if (!elementList.isEmpty()) {
                        return elementList.get(0).getOwnerDocument();
                    } else {
                        throw new IOException("Datastream not found: " + pid + " - " + streamName);
                    }
                } else {
                    throw new IOException("Expected XML datastream: " + pid + " - " + streamName);
                }
            }
            throw new IOException("Datastream not found: " + pid + " - " + streamName);
        }
        throw new IOException("Object not found: " + pid);
    }
*/
    /*
    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        try {
            DigitalObject object = manager.readObjectFromStorage(pid);
            return AkubraUtils.streamExists(object, streamName);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    @Override
    public boolean datastreamExists(String pid, String dsId) throws RepositoryException, IOException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            return object == null ? false : object.streamExists(dsId);
        } finally {
            readLock.unlock();
        }
    }
     */

}
