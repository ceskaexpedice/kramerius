package cz.inovatika.kramerius.fedora.impl;

import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.inovatika.kramerius.fedora.*;
import cz.inovatika.kramerius.fedora.om.repository.Repository;
import org.apache.solr.common.SolrDocument;
import org.dom4j.Attribute;
import org.dom4j.QName;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RepositoryAccessImpl implements RepositoryAccess {

    private Repository repository;
    /*
    private AkubraDOManager manager;
    private AkubraRepository repository;
    private ProcessingIndexFeeder feeder;
    private AggregatedAccessLogs accessLog;

     */

    public RepositoryAccessImpl(Repository repository) {
        this.repository = repository;
    }

    /*
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
    }*/

    //-------- Object ------------------------------------------

    @Override
    public ObjectAccessHelper getObjectAccessHelper(){
        return null;
    }

    @Override
    public boolean objectExists(String pid) {
        return this.repository.objectExists(pid);
    }

    @Override
    public RepositoryObjectWrapper getFoxml(String pid) {
        SupportedFormats supportedFormat = determineSupportedFormat(pid);
        // Retrieve content as bytes
        RepositoryObject rawContent = null;
        try {
            rawContent = fetchContentFromStorage(pid);
            return new RepositoryObjectWrapper(rawContent, supportedFormat);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String  getProperty(String pid, String propertyName) {
        org.dom4j.Document objectFoxml = getFoxml(pid).asXml();
        return objectFoxml == null ? null : extractProperty(objectFoxml, propertyName);
    }

    @Override
    public void ingestObject(org.dom4j.Document foxmlDoc, String pid) {
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
    public void deleteObject(String pid, boolean deleteDataOfManagedDatastreams) {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            akubraRepositoryImpl.deleteObject(pid, deleteDataOfManagedDatastreams, true);
            akubraRepositoryImpl.commitTransaction();
        } finally {
            writeLock.unlock();
        }
    }

    // ----- Stream ---------------------------------------------------

    @Override
    public DatastreamAccessHelper getDatastreamAccessHelper(){
        return null;
    }

    @Override
    public String getTypeOfDatastream(String pid, String dsId) {return null;};

    @Override
    public boolean datastreamExists(String pid, KnownDatastreams dsId) {
        boolean exists = this.repositoryApi.datastreamExists(pid, dsId);
        return exists;
    }

    // TODO archive format.. kdo to pouziva a proc
    // TODO versions - nepouziva se, ale je treba zkontrolovat, ze se bere urcite posledni verze
    @Override
    public <T> T getDatastreamFoxmlElement(String pid, KnownDatastreams dsId) {
        return null;
    }

    /**
     * @return part of FOXML that contains definition of the datastream. I.e. root element datastream with subelement(s) datastreamVersion.
     */
    @Override
    public org.dom4j.Document getDatastreamXml(String pid, KnownDatastreams dsId){return null;}

    @Override
    public <T> T getDatastreamProperty(String pid, KnownDatastreams dsId, String propertyName, Class<T> returnType) {
        org.dom4j.Document objectFoxml = getFoxml(pid);
        return objectFoxml == null ? null : extractProperty(objectFoxml, propertyName);
    }

    @Override
    public String getDatastreamMimetype(String pid, KnownDatastreams dsId){return null;}

    // TODO nazev, Triplet, Tuple, ????????
    @Override
    public <T> T getRDFSimpleProperty(String pid, String propertyName, Class<T> returnType) {
        org.dom4j.Document objectFoxml = getFoxml(pid);
        return objectFoxml == null ? null : extractProperty(objectFoxml, propertyName);
    }

    @Override
    public DatastreamContentWrapper getDatastreamContent(String pid, KnownDatastreams dsId) {
        SupportedFormats supportedFormat = determineSupportedFormat(dsId);
        // Retrieve content as bytes
        RepositoryDatastream rawContent = null;
        try {
            rawContent = fetchContentFromStorage(pid, dsId);
            return new DatastreamContentWrapper(rawContent, supportedFormat);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * TODO: Not Used
     * Returns xml containing datastream data
     *
     * @param pid pid of reqested object
     * @param datastreamName datastream name
     * @return datastream xml as stored in Fedora
     * @throws IOException IO error has been occurred
     */
    public InputStream getDataStreamXml(String pid, String datastreamName) {return null;};
    /**
     * Returns xml containing datastream data
     *
     * @param pid pid of reqested object
     * @param datastreamName datastream name
     * @return datastream xml as stored in Fedora
     * @throws IOException IO error has been occurred
     */
    public Document getDataStreamXmlAsDocument(String pid, String datastreamName){return null;};
    InputStream getLatestVersionOfDatastream(String pid, String dsId){return null;};
    org.dom4j.Document getLatestVersionOfInlineXmlDatastream(String pid, String dsId){return null;};
    String getLatestVersionOfManagedTextDatastream(String pid, String dsId){return null;};
    /**
     * Returns data from datastream
     *
     * @param pid pid of reqested object
     * @param datastreamName datastream name
     * @return data
     * @throws IOException IO error has been occurred
     */
    public InputStream getDataStream(String pid, String datastreamName) throws IOException{return null;};

    @Override
    public List<String> getDatastreamNames(String pid) {
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
    public void updateInlineXmlDatastream(String pid, KnownDatastreams dsId, org.dom4j.Document streamDoc, String formatUri) throws RepositoryException, IOException {
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);

            object.deleteStream(dsId);
            object.createStream(dsId, "text/xml", new ByteArrayInputStream(streamDoc.asXML().getBytes(Charset.forName("UTF-8"))));

        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void updateBinaryDatastream(String pid, KnownDatastreams dsId, String mimeType, byte[] byteArray) throws RepositoryException {
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

    @Override
    public void setDatastreamXml(String pid, KnownDatastreams dsId, org.dom4j.Document ds) {
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

    @Override
    public void deleteDatastream(String pid, KnownDatastreams dsId) {
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
    // TODO here we always use AkubraUtils.getStreamContent but we have also AkubraObject.AkubraDatastream for fetching stream content
    /* TODO
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
     */

    //------Processing index----------------------------------------------------
    @Override
    public ProcessingIndexAccessHelper getProcessingIndexAccessHelper(){
        return null;
    }

    @Override
    public <T> T queryProcessingIndex(ProcessingIndexQueryParameters params, ProcessingIndexResultMapper<T> mapper) {
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp =
                akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(
                        params.getQueryString(),
                        params.getRows(),
                        params.getPageIndex(),
                        params.getFieldsToFetch()
                );

        // Use the provided mapper to convert results
        return mapper.map(cp.getRight(), cp.getLeft());
    };

    @Override
    public void shutdown() {
        manager.shutdown();
    }
    @Override
    public String getFedoraVersion() throws IOException {
        return "Akubra";
    }

    private void reportAccess(String pid, String streamName) {
        try {
            this.accessLog.reportAccess(pid, streamName);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid + ", stream name: " + streamName, e);
        }
    }
    private SupportedFormats determineSupportedFormat(KnownDatastreams id) {
        // Example logic to determine supported formats
        if (id == KnownDatastreams.BIBLIO_DC) {
            return new SupportedFormats(false, true, false);
        } else {
            return new SupportedFormats(true, true, true);
        }
    }
    private RepositoryDatastream fetchContentFromStorage(String pid, KnownDatastreams dsId) throws RepositoryException {
        RepositoryObject object = akubraRepositoryImpl.getObject(pid);
        if (object.streamExists(dsId)) {
            RepositoryDatastream stream = object.getStream(dsId);
            return stream;
        } else {
            return null;
        }
        // Mock: Fetch content as bytes from your storage
        //return ("<xml>Content for ID: " + id + "</xml>").getBytes(StandardCharsets.UTF_8);
    }
    private Document parseXml(byte[] content) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(content));
        } catch (Exception e) {
            throw new IOException("Failed to parse XML", e);
        }
    }
    private Map<String, String> createMap(String label) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("dsid", label);
        map.put("label", label);
        return map;
    }
    private DigitalObject foxmlDocToDigitalObject(org.dom4j.Document foxml) throws IOException {
        try {
            return (DigitalObject) digitalObjectUnmarshaller.unmarshal(new StringReader(foxml.asXML()));
        } catch (JAXBException e) {
            throw new IOException(e);
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

    private String extractProperty(org.dom4j.Document foxmlDoc, String name) {
        org.dom4j.Node node = Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='%s']/@VALUE", name)).selectSingleNode(foxmlDoc);
        return node == null ? null : Dom4jUtils.toStringOrNull(node);
    }
}
