package cz.incad.kramerius.fedora;

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
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.google.inject.name.Named;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.om.impl.AkubraDOManager;
import cz.incad.kramerius.fedora.om.impl.AkubraRepositoryImpl;
import cz.incad.kramerius.repository.utils.NamespaceRemovingVisitor;
import cz.incad.kramerius.repository.utils.Utils;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.ehcache.CacheManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;

import cz.incad.kramerius.RepositoryAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessStackAware;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public abstract class AbstractRepositoryAccess implements RepositoryAccess {
    //----------------------
    public static final Logger LOGGER = Logger.getLogger(RepositoryApi.class.getName());

    private static final Namespace NS_FOXML = new Namespace("foxml", "info:fedora/fedora-system:def/foxml#");
    private final AkubraRepositoryImpl akubraRepositoryImpl;
    private final Unmarshaller digitalObjectUnmarshaller;
    //-------------------------------------------------------------------------

    public static final Logger LOGGER = Logger.getLogger(AbstractRepositoryAccess.class.getName());
    protected XPathFactory xPathFactory;
    protected KConfiguration configuration = KConfiguration.getInstance();

    @Inject
    public AbstractRepositoryAccess(@Nullable StatisticsAccessLog accessLog)
            throws IOException {
        super();
        this.xPathFactory = XPathFactory.newInstance();
    }

    protected String makeSureObjectPid(String pid) throws LexerException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();
        String sureObjectPid = pidParser.isPagePid() ? pidParser.getParentObjectPid() : pidParser.getObjectPid();
        return sureObjectPid;
    }

    @Override
    public String getDonator(Document relsExt) {
        try {
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasDonator",
                    FedoraNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                return pidParser.getObjectId();
            } else {
                return "";
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    protected void changeStack(TreeNodeProcessor processor, Stack<String> pidStack) {
        if (processor instanceof TreeNodeProcessStackAware) {
            TreeNodeProcessStackAware stackAware = (TreeNodeProcessStackAware) processor;
            stackAware.changeProcessingStack(pidStack);
        }
    }

    protected boolean processSubtreeInternal(String pid, Document relsExt, TreeNodeProcessor processor, int level,
            Stack<String> pidStack)
                    throws XPathExpressionException, LexerException, IOException, ProcessSubtreeException {
        processor.process(pid, level);
        boolean breakProcessing = processor.breakProcessing(pid, level);
        if (breakProcessing) {
            return breakProcessing;
        }
        if (relsExt == null) {
            return false;
        }
        XPathFactory factory = this.xPathFactory;
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("/rdf:RDF/rdf:Description/*");
        NodeList nodes = (NodeList) expr.evaluate(relsExt, XPathConstants.NODESET);

        if (pidStack.contains(pid)) {
            LOGGER.log(Level.WARNING, "Cyclic reference on " + pid);
            return breakProcessing;
        }
        pidStack.push(pid);
        changeStack(processor, pidStack);
        for (int i = 0, ll = nodes.getLength(); i < ll; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element iteratingElm = (Element) node;
                String namespaceURI = iteratingElm.getNamespaceURI();
                if (namespaceURI != null && (namespaceURI.equals(FedoraNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI)
                        || namespaceURI.equals(FedoraNamespaces.RDF_NAMESPACE_URI))) {
                    String attVal = iteratingElm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                    if (!attVal.trim().equals("")) {
                        PIDParser pidParser = new PIDParser(attVal);
                        pidParser.disseminationURI();
                        String objectId = pidParser.getObjectPid();
                        if (pidParser.getNamespaceId().equals("uuid")) {
                            if (!processor.skipBranch(objectId, level + 1)) {
                                Document iterationgRelsExt = null;

                                try {
                                    iterationgRelsExt = getRelsExt(objectId);
                                } catch (Exception ex) {
                                    LOGGER.warning("could not read RELS-EXT, skipping branch [" + (level + 1)
                                            + "] and pid (" + objectId + "):" + ex);
                                }
                                breakProcessing = processSubtreeInternal(pidParser.getObjectPid(), iterationgRelsExt,
                                        processor, level + 1, pidStack);

                                if (breakProcessing) {
                                    break;
                                }
                            } else {
                                LOGGER.fine("skipping branch [" + (level + 1) + "] and pid (" + objectId + ")");
                            }
                        }
                    }

                }
            }
        }
        pidStack.pop();
        changeStack(processor, pidStack);
        return breakProcessing;
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
    public List<String> getPids(String pid) throws IOException {
        final List<String> retval = new ArrayList<>();
        try {
            processSubtree(pid, new TreeNodeProcessor() {
                @Override
                public void process(String pid, int level) {
                    retval.add(pid);
                }
    
                @Override
                public boolean breakProcessing(String pid, int level) {
                    return false;
                }
    
                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        }
        return retval;
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
                            if (AbstractRepositoryAccess.this.isImageFULLAvailable(pid)) {
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
    public String getKrameriusModelName(Document relsExt) throws IOException {
        try {
            //TODO: Duplicate code in RelsExt helper -> mn
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasModel", FedoraNamespaces.FEDORA_MODELS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                return pidParser.getObjectId();
            } else {
                throw new IllegalArgumentException("cannot find model of given document");
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getDonator(String pid) throws IOException {
        return getDonator(getRelsExt(pid));
    }

    @Override
    public String getKrameriusModelName(String pid) throws IOException {
        return getKrameriusModelName(getRelsExt(pid));
    }

    @Override
    public List<Element> getPages(String pid, boolean deep) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getPages(pid, relsExt.getDocumentElement());
    }

    @Override
    public String getFirstItemPid(Document relsExt)
            throws IOException {
        try {
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasItem", FedoraNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                String pidItem = "uuid:" + pidParser.getObjectId();
                return pidItem;
            } else {
                return "";
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getFirstItemPid(String pid) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getFirstItemPid(relsExt);
    }

    @Override
    public String getFirstVolumePid(Document relsExt) throws IOException {

        try {
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasVolume", FedoraNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                String pidVolume = "uuid:" + pidParser.getObjectId();
                return pidVolume;
            } else {
                return "";
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getFirstVolumePid(String pid) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getFirstVolumePid(relsExt);
    }

    @Override
    public boolean isImageFULLAvailable(String pid) throws IOException {
        try {
            return isStreamAvailable(makeSureObjectPid(pid), FedoraUtils.IMG_FULL_STREAM);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public List<Element> getPages(String pid, Element rootElementOfRelsExt) throws IOException {
        try {
            ArrayList<Element> elms = new ArrayList<Element>();
            String xPathStr = "/RDF/Description/hasPage";
            XPath xpath = this.xPathFactory.newXPath();
            XPathExpression expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(rootElementOfRelsExt, XPathConstants.NODESET);
            for (int i = 0, lastIndex = nodes.getLength() - 1; i <= lastIndex; i++) {
                Element elm = (Element) nodes.item(i);
                elms.add(elm);
            }
            return elms;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    protected List<String> getTreePredicates() {
        return Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.treePredicates"));
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

    @Override
    public List<String> getModelsOfRel(Document relsExt) {
        try {
            throw new UnsupportedOperationException("still unsupported");
            // Element foundElement =
            // XMLUtils.findElement(relsExt.getDocumentElement(), "hasModel",
            // FedoraNamespaces.FEDORA_MODELS_URI);
            // if (foundElement != null) {
            // String sform =
            // foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI,
            // "resource");
            // PIDParser pidParser = new PIDParser(sform);
            // pidParser.disseminationURI();
            // ArrayList<String> model =
            // RelsExtModelsMap.getModelsOfRelation(pidParser.getObjectId());
            // return model;
            // } else {
            // throw new IllegalArgumentException("cannot find model of ");
            // }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public List<String> getModelsOfRel(String pid) throws IOException {
        return getModelsOfRel(getRelsExt(pid));
    }
// RepoApiImpl-------------------------------------------------------------------------------------------
@Inject
public RepositoryApiImpl(ProcessingIndexFeeder processingIndexFeeder, @Named("akubraCacheManager") CacheManager cacheManager) throws RepositoryException {
    try {
        AkubraDOManager akubraDOManager = new AkubraDOManager(cacheManager);
        this.akubraRepositoryImpl = (AkubraRepositoryImpl) AkubraRepositoryImpl.build(processingIndexFeeder, akubraDOManager);
        this.digitalObjectUnmarshaller = JAXBContext.newInstance(DigitalObject.class).createUnmarshaller();
    } catch (IOException e) {
        throw new RepositoryException(e);
    } catch (JAXBException e) {
        throw new RepositoryException("Error initializing JAXB unmarshaller for " + DigitalObject.class.getName());
    }
}

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
    public boolean objectExists(String pid) throws RepositoryException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            return akubraRepositoryImpl.objectExists(pid);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getProperty(String pid, String propertyName) throws IOException, RepositoryException {
        org.dom4j.Document objectFoxml = getFoxml(pid);
        return objectFoxml == null ? null : extractProperty(objectFoxml, propertyName);
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
    public org.dom4j.Document getFoxml(String pid) throws RepositoryException, IOException {
        Lock readLock = AkubraDOManager.getReadLock(pid);
        try {
            RepositoryObject object = akubraRepositoryImpl.getObject(pid);
            return Utils.inputstreamToDocument(object.getFoxml(), true);
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

    private String extractProperty(org.dom4j.Document foxmlDoc, String name) {
        org.dom4j.Node node = Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='%s']/@VALUE", name)).selectSingleNode(foxmlDoc);
        return node == null ? null : Dom4jUtils.toStringOrNull(node);
    }

    //--------- KraRepositoryAPIIMPl
    @javax.inject.Inject
    private RepositoryApiImpl repositoryApi;

    @javax.inject.Inject
    private AggregatedAccessLogs accessLog;


    @Override
    public RepositoryApi getLowLevelApi() {
        return repositoryApi;
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

    @Override
    public boolean isStreamAvailable(String pid, String dsId) throws IOException, RepositoryException {
        boolean exists = this.repositoryApi.datastreamExists(pid, dsId);
        return exists;
    }

}
