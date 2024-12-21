package cz.incad.kramerius.fedora.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.qbizm.kramerius.imp.jaxb.DatastreamVersionType;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.fedora.RepositoryAccess;
import cz.incad.kramerius.fedora.om.repository.AkubraRepository;
import cz.incad.kramerius.fedora.om.repository.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.repository.RepositoryException;
import cz.incad.kramerius.fedora.om.repository.RepositoryObject;
import cz.incad.kramerius.fedora.om.repository.impl.AkubraDOManager;
import cz.incad.kramerius.fedora.om.repository.impl.AkubraRepositoryImpl;
import cz.incad.kramerius.fedora.utils.AkubraUtils;
import cz.incad.kramerius.fedora.om.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.repository.utils.NamespaceRemovingVisitor;
import cz.incad.kramerius.repository.utils.Utils;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.incad.kramerius.fedora.utils.FedoraUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.java.Pair;
import cz.incad.kramerius.fedora.utils.pid.LexerException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.QName;
import org.ehcache.CacheManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RepositoryAccessImpl implements RepositoryAccess {

    private AkubraDOManager manager;
    private AkubraRepository repository;
    private ProcessingIndexFeeder feeder;
    private AggregatedAccessLogs accessLog;


    @Inject
    public RepositoryAccessImpl(ProcessingIndexFeeder feeder, @Nullable AggregatedAccessLogs accessLog, @Named("akubraCacheManager") CacheManager cacheManager) throws IOException {
        super( accessLog);
        try {
            this.manager = new AkubraDOManager(cacheManager);
            this.feeder = feeder;
            this.repository = AkubraRepositoryImpl.build(feeder, this.manager);
            this.accessLog = accessLog;

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    //-------- get object property
    @Override
    public String getProperty(String pid, String propertyName) throws IOException, RepositoryException {
        org.dom4j.Document objectFoxml = getFoxml(pid);
        return objectFoxml == null ? null : extractProperty(objectFoxml, propertyName);
    }

    private String extractProperty(org.dom4j.Document foxmlDoc, String name) {
        org.dom4j.Node node = Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='%s']/@VALUE", name)).selectSingleNode(foxmlDoc);
        return node == null ? null : Dom4jUtils.toStringOrNull(node);
    }

    @Override
    public String getPropertyLabel(String pid) throws IOException, RepositoryException {
        return getProperty(pid, "info:fedora/fedora-system:def/model#label");
    }

    @Override
    public LocalDateTime getPropertyCreated(String pid) throws IOException, RepositoryException {
        String propertyValue = getProperty(pid, "info:fedora/fedora-system:def/model#createdDate");
        if (propertyValue != null) {
            try {
                return LocalDateTime.parse(propertyValue, RepositoryApi.TIMESTAMP_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(String.format("cannot parse createdDate %s from object %s", propertyValue, pid));
            }
        }
        return null;
    }

    @Override
    public LocalDateTime getPropertyLastModified(String pid) throws IOException, RepositoryException {
        String propertyValue = getProperty(pid, "info:fedora/fedora-system:def/view#lastModifiedDate");
        if (propertyValue != null) {
            try {
                return LocalDateTime.parse(propertyValue, RepositoryApi.TIMESTAMP_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(String.format("cannot parse lastModifiedDate %s from object %s", propertyValue, pid));
            }
        }
        return null;
    }
    @Override
    public Date getObjectLastmodifiedFlag(String pid) throws IOException {
        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {
            return AkubraUtils.getLastModified(object);
        }
        throw new IOException("Object not found: " + pid);
    }


    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        try {
            return this.repository.objectExists(pid);
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }
    @Override
    public boolean objectExists(String pid) throws RepositoryException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            return akubraRepositoryImpl.objectExists(pid);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getFedoraVersion() throws IOException {
        return "Akubra";
    }

    @Override
    public InputStream getFoxml(String pid, boolean archive) throws IOException {
        try {
            if (archive){
                DigitalObject obj = manager.readObjectCloneFromStorage(pid);
                manager.resolveArchivedDatastreams(obj);
                return this.manager.marshallObject(obj);
            }else {
                return this.manager.retrieveObject(pid);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    @Override
    public org.dom4j.Document getFoxml(String pid) throws RepositoryException, IOException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            return Utils.inputstreamToDocument(object.getFoxml(), true);
        } finally {
            readLock.unlock();
        }
    }

    // -------------  get metadata related to a pid's stream
    @Override
    public List<String> getDatastreamNames(String pid) throws RepositoryException, IOException, SolrServerException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            List<RepositoryDatastream> streams = object.getStreams();
            return streams.stream().map(it -> {
                try {
                    return it.getName();
                } catch (RepositoryException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    return null;
                }
            }).collect(Collectors.toList());
        } finally {
            readLock.unlock();
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

    @Override
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

    @Override
    public org.dom4j.Document getLatestVersionOfInlineXmlDatastream(String pid, String dsId) throws RepositoryException, IOException {
        InputStream is = getLatestVersionOfDatastream(pid, dsId);
        return is == null ? null : Utils.inputstreamToDocument(is, true);
    }

    @Override
    public String getLatestVersionOfManagedTextDatastream(String pid, String dsId) throws RepositoryException, IOException {
        InputStream is = getLatestVersionOfDatastream(pid, dsId);
        return is == null ? null : Utils.inputstreamToString(is);
    }

    @Override
    public boolean isRelsExtAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.RELS_EXT.toString());
    }

    @Override
    public org.dom4j.Document getRelsExt(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        org.dom4j.Document doc = repositoryApi.getLatestVersionOfInlineXmlDatastream(pid, KnownDatastreams.RELS_EXT.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }

    @Override
    public boolean isModsAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.BIBLIO_MODS.toString());
    }

    @Override
    public org.dom4j.Document getMods(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        org.dom4j.Document doc = repositoryApi.getLatestVersionOfInlineXmlDatastream(pid, KnownDatastreams.BIBLIO_MODS.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }

    @Override
    public boolean isDublinCoreAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.BIBLIO_DC.toString());
    }

    @Override
    public org.dom4j.Document getDublinCore(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        org.dom4j.Document doc = repositoryApi.getLatestVersionOfInlineXmlDatastream(pid, KnownDatastreams.BIBLIO_DC.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }

    @Override
    public boolean isOcrTextAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.OCR_TEXT.toString());
    }

    @Override
    public String getOcrText(String pid) throws IOException, RepositoryException {
        return repositoryApi.getLatestVersionOfManagedTextDatastream(pid, KnownDatastreams.OCR_TEXT.toString());
    }

    @Override
    public boolean isOcrAltoAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.OCR_ALTO.toString());
    }

    @Override
    public org.dom4j.Document getOcrAlto(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        org.dom4j.Document doc = repositoryApi.getLatestVersionOfInlineXmlDatastream(pid, KnownDatastreams.OCR_ALTO.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }

    @Override
    public boolean isImgFullAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.IMG_FULL.toString());
    }

    @Override
    public String getImgFullMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.IMG_FULL.toString());
    }

    @Override
    public InputStream getImgFull(String pid) throws IOException, RepositoryException {
        this.accessLog.reportAccess(pid, KnownDatastreams.IMG_FULL.toString());
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.IMG_FULL.toString());
    }

    @Override
    public boolean isImgThumbAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.IMG_THUMB.toString());
    }

    @Override
    public String getImgThumbMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.IMG_THUMB.toString());
    }

    @Override
    public InputStream getImgThumb(String pid) throws IOException, RepositoryException {
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.IMG_THUMB.toString());
    }

    @Override
    public boolean isImgPreviewAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.IMG_PREVIEW.toString());
    }

    @Override
    public String getImgPreviewMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.IMG_PREVIEW.toString());
    }

    @Override
    public InputStream getImgPreview(String pid) throws IOException, RepositoryException {
        this.accessLog.reportAccess(pid, KnownDatastreams.IMG_PREVIEW.toString());
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.IMG_PREVIEW.toString());
    }

    @Override
    public boolean isAudioMp3Available(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.AUDIO_MP3.toString());
    }

    @Override
    public String getAudioMp3Mimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.AUDIO_MP3.toString());
    }

    @Override
    public InputStream getAudioMp3(String pid) throws IOException, RepositoryException {
        this.accessLog.reportAccess(pid, KnownDatastreams.AUDIO_MP3.toString());
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.AUDIO_MP3.toString());
    }

    @Override
    public boolean isAudioOggAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.AUDIO_OGG.toString());
    }

    @Override
    public String getAudioOggMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.AUDIO_OGG.toString());
    }

    @Override
    public InputStream getAudioOgg(String pid) throws IOException, RepositoryException {
        this.accessLog.reportAccess(pid, KnownDatastreams.AUDIO_OGG.toString());
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.AUDIO_OGG.toString());
    }

    @Override
    public boolean isAudioWavAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.AUDIO_WAV.toString());
    }

    @Override
    public String getAudioWavMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.AUDIO_WAV.toString());
    }

    @Override
    public InputStream getAudioWav(String pid) throws IOException, RepositoryException {
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.AUDIO_WAV.toString());
    }

    @Override
    public String getModel(String objectPid) throws RepositoryException, IOException, SolrServerException {
        Map<String, String> description = repositoryApi.getDescription(objectPid);
        String model = description.get("model");
        return model == null ? null : model.substring("model:".length());
    }

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
    public List<Map<String, String>> getStreamsOfObject(String pid) throws IOException {
        try {
            List<Map<String, String>> results = new ArrayList<>();
            DigitalObject obj = manager.readObjectFromStorage(pid);

            return obj.getDatastream().stream().filter((o) -> {
                try {
                    // policy stream -> should be ommited?
                    return (!o.getID().equals("POLICY"));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    return false;
                }
            }).map((o) -> {
                Map<String, String> map = null;
                try {
                    map = createMap(o.getID());
                    List<DatastreamVersionType> datastreamVersionList = o.getDatastreamVersion();
                    map.put("mimetype", datastreamVersionList.get(datastreamVersionList.size() - 1).getMIMETYPE());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
                return map;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    private Map<String, String> createMap(String label) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("dsid", label);
        map.put("label", label);
        return map;
    }



    // -------- stream content

    // TODO is this needed?
    @Override
    public boolean isContentAccessible(String pid) throws IOException {
        return true;
    }

    // TODO here we always use AkubraUtils.getStreamContent but we have also AkubraObject.AkubraDatastream for fetching stream content
    @Override
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

    // XML data stream
    @Override
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


    //--------------------------------------

    /*
    @Override
    public AkubraRepository getInternalAPI() throws RepositoryException {
        return this.repository;
    }*/


    @Override
    public void shutdown() {
        manager.shutdown();
    }


    private void reportAccess(String pid, String streamName) {
        try {
            this.accessLog.reportAccess(pid, streamName);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid + ", stream name: " + streamName, e);
        }
    }

    // -----------------------------------------------Former RespositoryAPIImpl
/*
    @Override
    public boolean objectExists(String pid) throws RepositoryException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            return akubraRepositoryImpl.objectExists(pid);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public org.dom4j.Document getFoxml(String pid) throws RepositoryException, IOException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            return Utils.inputstreamToDocument(object.getFoxml(), true);
        } finally {
            readLock.unlock();
        }
    }
*/


    //-------------------------------------------------------------
    ///////Processing index..////////////////////////////////////////////////////////////////////////////////////////
    // PI 1
    @Override
    public Pair<Long, List<String>> getPidsOfObjectsByModel(String model, int rows, int pageIndex) throws RepositoryException, IOException, SolrServerException {
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp = akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(query, rows, pageIndex, Arrays.asList("source"));
        Long numberOfRecords = cp.getLeft();
        List<String> pids = cp.getRight().stream().map(sd -> {
            Object fieldValue = sd.getFieldValue("source");
            return fieldValue.toString();
        }).collect(Collectors.toList());
        return new Pair<>(numberOfRecords, pids);
    }

    // PI 2
    //TODO : Should be replaced by pairs
    @Override
    public Pair<Long, List<String>> getPidsOfObjectsByModel(String model, String titlePrefix, int rows, int pageIndex) throws RepositoryException, IOException, SolrServerException {
        String query = String.format("type:description AND model:%s", "model\\:" + model);
        if (StringUtils.isAnyString(titlePrefix)) {
            query = String.format("type:description AND model:%s AND title_edge:%s", "model\\:" + model, titlePrefix); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        }
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp = akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(query, rows, pageIndex, Arrays.asList("source"));
        Long numberOfRecords = cp.getLeft();
        List<String> pids = cp.getRight().stream().map(sd -> {
            Object fieldValue = sd.getFieldValue("source");
            return fieldValue.toString();
        }).collect(Collectors.toList());
        return new Pair<>(numberOfRecords, pids);
    }

    // PI 3
    @Override
    public TitlePidPairs getPidsOfObjectsWithTitlesByModel(String model, boolean ascendingOrder, int offset, int limit) throws RepositoryException, IOException, SolrServerException {
        List<Pair<String, String>> titlePidPairs = new ArrayList<>();
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze je mozna zbytecne (ten prefix)
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateSectionOfProcessingSortedByTitle(query, ascendingOrder, offset, limit, (doc) -> {
            Object fieldPid = doc.getFieldValue("source");
            Object fieldTitle = doc.getFieldValue("dc.title");
            String pid = null;
            String title = null;
            if (fieldPid != null) {
                pid = fieldPid.toString();
            }
            if (fieldTitle != null) {
                title = fieldTitle.toString().trim();
            }
            titlePidPairs.add(new Pair(title, pid));
        });
        TitlePidPairs result = new TitlePidPairs();
        result.titlePidPairs = titlePidPairs;
        return result;
    }

    // PI 4
    @Override
    public TitlePidPairs getPidsOfObjectsWithTitlesByModelWithCursor(String model, boolean ascendingOrder, String cursor, int limit) throws RepositoryException, IOException, SolrServerException {
        List<Pair<String, String>> titlePidPairs = new ArrayList<>();
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze je mozna zbytecne (ten prefix)
        String nextCursorMark = akubraRepositoryImpl.getProcessingIndexFeeder().iterateSectionOfProcessingSortedByTitleWithCursor(query, ascendingOrder, cursor, limit, (doc) -> {
            Object fieldPid = doc.getFieldValue("source");
            Object fieldTitle = doc.getFieldValue("dc.title");
            String pid = null;
            String title = null;
            if (fieldPid != null) {
                pid = fieldPid.toString();
            }
            if (fieldTitle != null) {
                title = fieldTitle.toString().trim();
            }
            titlePidPairs.add(new Pair(title, pid));
        });
        TitlePidPairs result = new TitlePidPairs();
        result.titlePidPairs = titlePidPairs;
        result.nextCursorMark = nextCursorMark;
        return result;
    }

    // PI 5
    @Override
    public Map<String, String> getDescription(String objectPid) throws RepositoryException, IOException, SolrServerException {
        Map<String, String> description = new HashMap<>();
        String query = String.format("type:description AND source:%s", objectPid.replace(":", "\\:"));
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByPid(query, (doc) -> { //iterating, but there should only be one hit
            for (String name : doc.getFieldNames()) {
                description.put(name, doc.getFieldValue(name).toString());
            }
        });
        return description;
    }

    // PI 6
    @Override
    public List<String> getTripletTargets(String sourcePid, String relation) throws RepositoryException, IOException, SolrServerException {
        List<String> pids = new ArrayList<>();
        String query = String.format("source:%s AND relation:%s", sourcePid.replace(":", "\\:"), relation);
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByIndexationDate(query, true, (doc) -> {
            Object fieldValue = doc.getFieldValue("targetPid");
            if (fieldValue != null) {
                String valueStr = fieldValue.toString();
                pids.add(valueStr);
            }
        });
        return pids;
    }

    // PI 7
    @Override
    public List<Triplet> getTripletTargets(String sourcePid) throws RepositoryException, IOException, SolrServerException {
        List<Triplet> triplets = new ArrayList<>();
        String query = String.format("source:%s", sourcePid.replace(":", "\\:"));
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByIndexationDate(query, true, (doc) -> {
            Object targetPid = doc.getFieldValue("targetPid");
            Object relation = doc.getFieldValue("relation");
            if (targetPid != null && relation != null) {
                triplets.add(new Triplet(sourcePid, relation.toString(), targetPid.toString()));
            }
        });
        return triplets;
    }

    // PI 8
    @Override
    public List<String> getTripletSources(String relation, String targetPid) throws RepositoryException, IOException, SolrServerException {
        List<String> pids = new ArrayList<>();
        String query = String.format("relation:%s AND targetPid:%s", relation, targetPid.replace(":", "\\:"));
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByIndexationDate(query, true, (doc) -> {
            Object fieldValue = doc.getFieldValue("source");
            if (fieldValue != null) {
                String valueStr = fieldValue.toString();
                pids.add(valueStr);
            }
        });
        return pids;
    }

    // PI 9
    @Override
    public List<Triplet> getTripletSources(String targetPid) throws RepositoryException, IOException, SolrServerException {
        List<Triplet> triplets = new ArrayList<>();
        String query = String.format("targetPid:%s", targetPid.replace(":", "\\:"));
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByIndexationDate(query, true, (doc) -> {
            Object sourcePid = doc.getFieldValue("source");
            Object relation = doc.getFieldValue("relation");
            if (sourcePid != null && relation != null) {
                triplets.add(new Triplet(sourcePid.toString(), relation.toString(), targetPid));
            }
        });
        return triplets;
    }

    // PI 10
    @Override
    public List<String> getPidsOfObjectsByModel(String model) throws RepositoryException, IOException, SolrServerException {
        List<String> pids = new ArrayList<>();
        //TODO: offset, limit
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByTitle(query, (doc) -> {
            Object fieldValue = doc.getFieldValue("source");
            if (fieldValue != null) {
                String valueStr = fieldValue.toString();
                pids.add(valueStr);
            }
        });
        return pids;
    }

    // PI 11
    public List<String> getPidsByCriteria(Map<String, String> filters, String sortField, boolean ascending)
            throws RepositoryException, IOException, SolrServerException {
        List<String> pids = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();

        // Build the query from filters
        filters.forEach((field, value) -> queryBuilder.append(field).append(":").append(value).append(" AND "));
        if (queryBuilder.length() > 0) {
            queryBuilder.setLength(queryBuilder.length() - 5); // Remove trailing " AND "
        }

        // Sort query string
        String sortClause = sortField != null ? String.format("sort:%s %s", sortField, ascending ? "ASC" : "DESC") : "";

        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByTitle(
                queryBuilder.toString(),
                (doc) -> {
                    Object fieldValue = doc.getFieldValue("source");
                    if (fieldValue != null) {
                        String valueStr = fieldValue.toString();
                        pids.add(valueStr);
                    }
                }
        );

        return pids;
    }

    // PI 12
    @Override
    public List<String> getPidsOfAllObjects() throws RepositoryException, IOException, SolrServerException {
        List<String> pids = new ArrayList<>();
        //TODO: offset, limit
        String query = "type:description";
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByPid(query, (doc) -> {
            Object fieldValue = doc.getFieldValue("source");
            if (fieldValue != null) {
                String valueStr = fieldValue.toString();
                pids.add(valueStr);
            }
        });
        return pids;
    }

    //--- UPDATE ------------------------------------------------------------------------------------------------

    @Override
    public void ingestObject(org.dom4j.Document foxmlDoc, String pid) throws RepositoryException, IOException {
        DigitalObject digitalObject = foxmlDocToDigitalObject(foxmlDoc);
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            akubraRepositoryImpl.ingestObject(digitalObject);
            akubraRepositoryImpl.commitTransaction();
        } finally {
            writeLock.unlock();
        }
    }


    @Override
    public void updateInlineXmlDatastream(String pid, String dsId, org.dom4j.Document streamDoc, String formatUri) throws RepositoryException, IOException {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);

            object.deleteStream(dsId);
            object.createStream(dsId, "text/xml", new ByteArrayInputStream(streamDoc.asXML().getBytes(Charset.forName("UTF-8"))));

        } finally {
            writeLock.unlock();
        }
    }

    public void updateBinaryDatastream(String pid, String streamName, String mimeType, byte[] byteArray) throws RepositoryException {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            if (object != null) {
                if (object.streamExists(streamName)) {
                    object.deleteStream(streamName);
                }
                ByteArrayInputStream bos = new ByteArrayInputStream(byteArray);
                object.createManagedStream(streamName, mimeType, bos);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void deleteDatastream(String pid, String streamName) throws RepositoryException {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            if (object != null) {
                if (object.streamExists(streamName)) {
                    object.deleteStream(streamName);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }


    @Override
    public void setDatastreamXml(String pid, String dsId, org.dom4j.Document ds) throws RepositoryException, IOException {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            org.dom4j.Document foxml = getFoxml(pid);
            org.dom4j.Element originalDsEl = (org.dom4j.Element) Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:datastream[@ID='%s']", dsId)).selectSingleNode(foxml);
            if (originalDsEl != null) {
                originalDsEl.detach();
            }
            foxml.getRootElement().add(ds.getRootElement().detach());
            updateLastModifiedTimestamp(foxml);
            DigitalObject updatedDigitalObject = foxmlDocToDigitalObject(foxml);
            akubraRepositoryImpl.deleteObject(pid, false, false);
            akubraRepositoryImpl.ingestObject(updatedDigitalObject);
            akubraRepositoryImpl.commitTransaction();
        } finally {
            writeLock.unlock();
        }
    }

    private void updateLastModifiedTimestamp(org.dom4j.Document foxml) {
        Attribute valueAttr = (Attribute) Dom4jUtils.buildXpath("/foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/view#lastModifiedDate']/@VALUE").selectSingleNode(foxml);
        if (valueAttr != null) {
            valueAttr.setValue(LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        } else {
            org.dom4j.Element objectProperties = (org.dom4j.Element) Dom4jUtils.buildXpath("/foxml:digitalObject/foxml:objectProperties").selectSingleNode(foxml);
            org.dom4j.Element propertyLastModified = objectProperties.addElement(new QName("property", NS_FOXML));
            propertyLastModified.addAttribute("NAME", "info:fedora/fedora-system:def/view#lastModifiedDate");
            propertyLastModified.addAttribute("VALUE", LocalDateTime.now().format(RepositoryApi.TIMESTAMP_FORMATTER));
        }
    }

    private void appendNewInlineXmlDatastreamVersion(org.dom4j.Document foxml, String dsId, org.dom4j.Document streamDoc, String formatUri) {
        org.dom4j.Element datastreamEl = (org.dom4j.Element) Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:datastream[@ID='%s']", dsId)).selectSingleNode(foxml);
        if (datastreamEl != null) {
            int latestDsIdVersion = extractLatestDsIdVersion(datastreamEl);
            int newDsIdVesion = latestDsIdVersion + 1;
            org.dom4j.Element dsVersionEl = datastreamEl.addElement("datastreamVersion", NAMESPACE_FOXML);
            dsVersionEl.addAttribute("ID", dsId + "." + newDsIdVesion);
            dsVersionEl.addAttribute("CREATED", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
            dsVersionEl.addAttribute("MIMETYPE", "application/xml");
            if (formatUri != null) {
                dsVersionEl.addAttribute("FORMAT_URI", formatUri);
            }
            org.dom4j.Element xmlContentEl = dsVersionEl.addElement("xmlContent", NAMESPACE_FOXML);
            xmlContentEl.add(streamDoc.getRootElement().detach());
        }
    }

    private int extractLatestDsIdVersion(org.dom4j.Element datastreamEl) {
        List<org.dom4j.Node> dsVersionEls = Dom4jUtils.buildXpath("foxml:datastreamVersion").selectNodes(datastreamEl);
        int maxVersion = -1;
        for (org.dom4j.Node node : dsVersionEls) {
            org.dom4j.Element versionEl = (org.dom4j.Element) node;
            String ID = Dom4jUtils.stringOrNullFromAttributeByName(versionEl, "ID");
            int versionNumber = Integer.valueOf(ID.split("\\.")[1]);
            if (versionNumber > maxVersion) {
                maxVersion = versionNumber;
            }
        }
        return maxVersion;
    }

    @Override
    public void deleteObject(String pid, boolean deleteDataOfManagedDatastreams) throws RepositoryException, IOException {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            akubraRepositoryImpl.deleteObject(pid, deleteDataOfManagedDatastreams, true);
            akubraRepositoryImpl.commitTransaction();
        } finally {
            writeLock.unlock();
        }
    }


    private DigitalObject foxmlDocToDigitalObject(org.dom4j.Document foxml) throws IOException {
        try {
            return (DigitalObject) digitalObjectUnmarshaller.unmarshal(new StringReader(foxml.asXML()));
        } catch (JAXBException e) {
            throw new IOException(e);
        }
    }


    //-----------------------Former KrameriusRepositoryAPIImpl-----------------------------------

    @Override
    public Pair<RepositoryApi.Triplet, List<RepositoryApi.Triplet>> getParents(String objectPid) throws RepositoryException, IOException, SolrServerException {
        List<RepositoryApi.Triplet> pseudoparentTriplets = repositoryApi.getTripletSources(objectPid);
        RepositoryApi.Triplet ownParentTriplet = null;
        List<RepositoryApi.Triplet> fosterParentTriplets = new ArrayList<>();
        for (RepositoryApi.Triplet triplet : pseudoparentTriplets) {
            if (KrameriusRepositoryApi.isOwnRelation(triplet.relation)) {
                if (ownParentTriplet != null) {
                    throw new RepositoryException(String.format("found multiple own parent relations: %s and %s", ownParentTriplet, triplet));
                } else {
                    ownParentTriplet = triplet;
                }
            } else {
                fosterParentTriplets.add(triplet);
            }
        }
        return new Pair(ownParentTriplet, fosterParentTriplets);
    }

    @Override
    public Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> getChildren(String objectPid) throws RepositoryException, IOException, SolrServerException {
        List<RepositoryApi.Triplet> pseudochildrenTriplets = repositoryApi.getTripletTargets(objectPid);
        List<RepositoryApi.Triplet> ownChildrenTriplets = new ArrayList<>();
        List<RepositoryApi.Triplet> fosterChildrenTriplets = new ArrayList<>();
        for (RepositoryApi.Triplet triplet : pseudochildrenTriplets) {
            if (triplet.target.startsWith("uuid:")) { //ignore hasDonator and other indexed relations, that are not binding two objects in repository
                if (KrameriusRepositoryApi.isOwnRelation(triplet.relation)) {
                    ownChildrenTriplets.add(triplet);
                } else {
                    fosterChildrenTriplets.add(triplet);
                }
            }
        }
        return new Pair(ownChildrenTriplets, fosterChildrenTriplets);
    }

    @Override
    public List<String> getPidsOfItemsInCollection(String collectionPid) throws RepositoryException, IOException, SolrServerException {
        return repositoryApi.getTripletTargets(collectionPid, KnownRelations.CONTAINS.toString());
    }

    @Override
    public List<String> getPidsOfCollectionsContainingItem(String itemPid) throws RepositoryException, IOException, SolrServerException {
        return repositoryApi.getTripletSources(KnownRelations.CONTAINS.toString(), itemPid);
    }

    @Override
    public void updateRelsExt(String pid, org.dom4j.Document relsExtDoc) throws IOException, RepositoryException {
        repositoryApi.updateInlineXmlDatastream(pid, KnownDatastreams.RELS_EXT.toString(), relsExtDoc, KnownXmlFormatUris.RELS_EXT);
    }

    @Override
    public void updateMods(String pid, org.dom4j.Document modsDoc) throws IOException, RepositoryException {
        repositoryApi.updateInlineXmlDatastream(pid, KnownDatastreams.BIBLIO_MODS.toString(), modsDoc, KnownXmlFormatUris.BIBLIO_MODS);
    }

    @Override
    public void updateDublinCore(String pid, org.dom4j.Document dcDoc) throws IOException, RepositoryException {
        repositoryApi.updateInlineXmlDatastream(pid, KnownDatastreams.BIBLIO_DC.toString(), dcDoc, KnownXmlFormatUris.BIBLIO_DC);
    }


    @Override
    public boolean isPidAvailable(String pid) throws IOException, RepositoryException {
        boolean exists = this.repositoryApi.objectExists(pid);
        return exists;
    }

    /* TODO
    @Override
    public boolean isStreamAvailable(String pid, String dsId) throws IOException, RepositoryException {
        boolean exists = this.repositoryApi.datastreamExists(pid, dsId);
        return exists;
    }*/

    /*
    @Override
    public Document getBiblioMods(String pid) throws IOException {
        try {
            return getStream(makeSureObjectPid(pid), FedoraUtils.BIBLIO_MODS_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }*/

    /*
    @Override
    public Document getRelsExt(String pid) throws IOException {
        try {
            // consider to change to metadata
            return getStream(makeSureObjectPid(pid), FedoraUtils.RELS_EXT_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }*/

    /*
    @Override
    public Document getDC(String pid) throws IOException {
        try {
            return getStream(makeSureObjectPid(pid), FedoraUtils.DC_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }*/
    /*
    @Override
    public String getFullThumbnailMimeType(String pid) throws IOException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_PREVIEW_STREAM);
    }*/

    /*
    @Override
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_THUMB_STREAM);
    }*/

    /*
    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        try {
            return getDataStream(makeSureObjectPid(pid), FedoraUtils.IMG_PREVIEW_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }*/

    /*
    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        try {
            return getDataStream(makeSureObjectPid(pid), FedoraUtils.IMG_THUMB_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }*/

    /*
    @Override
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_FULL_STREAM);
    }*/

    /*
    @Override
    public InputStream getImageFULL(String pid) throws IOException {
        return getDataStream(pid, FedoraUtils.IMG_FULL_STREAM);
    }*/
    /*
    @Override
    public boolean isImageFULLAvailable(String pid) throws IOException {
        return super.isImageFULLAvailable(pid);
    }*/

    /*
    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        return this.isStreamAvailable(pid, FedoraUtils.IMG_PREVIEW_STREAM);
    }*/



}
