package cz.incad.kramerius.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.ItemExistsException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.version.VersionException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class JackRabbitRepoAccessImpl extends AbstractFedoraAccess {

    public static final Logger LOGGER = Logger.getLogger(JackRabbitRepoAccessImpl.class.getName());

    static String JACKRABBIT_FOLDER = System.getProperty("jackrabbit.folder",
            (System.getProperty("user.home") + File.separator + "jck_repo"));

    private Repository repo;

    private ObjectPool<Session> poolOfSession;

    @Inject
    public JackRabbitRepoAccessImpl(KConfiguration configuration, @Nullable StatisticsAccessLog accessLog) throws IOException {
        super(configuration, accessLog);
        try {
            File f = new File(JACKRABBIT_FOLDER);
            if (!f.exists()) {
                f.mkdirs();
            }
            this.repo = JcrUtils.getRepository(new File(JACKRABBIT_FOLDER).toURI().toString());
            this.poolOfSession = new GenericObjectPool(new JackRabbitSessionFactory(this.repo));
            // 3 objects in the pool
            this.poolOfSession.addObject();
            this.poolOfSession.addObject();
            this.poolOfSession.addObject();
            this.open();
        } catch (RepositoryException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    private void open() throws Exception {
        Session session = null;
        try {
            session = poolOfSession.borrowObject();
            // this.session = this.repo.login(new SimpleCredentials("admin",
            // "admin".toCharArray()));
            this.namespaces(session);
        } catch (LoginException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (RepositoryException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (NoSuchElementException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } finally {
            poolOfSession.returnObject(session);
        }
    }

    public static String restPid(String pid) {
        if (pid.startsWith("uuid:")) {
            return pid.substring("uuid:".length());
        } else
            return pid;
    }

    @Override
    public Document getRelsExt(String pid) throws IOException {
        try {

            InputStream dataStream = getDataStream(pid, FedoraUtils.RELS_EXT_STREAM);
            return XMLUtils.parseDocument(dataStream, true);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public Document getBiblioMods(String pid) throws IOException {
        try {
            InputStream dataStream = getDataStream(pid, FedoraUtils.BIBLIO_MODS_STREAM);
            return XMLUtils.parseDocument(dataStream, true);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public Document getDC(String pid) throws IOException {
        try {
            InputStream dataStream = getDataStream(pid, FedoraUtils.DC_STREAM);
            return XMLUtils.parseDocument(dataStream, true);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        return getDataStream(pid, FedoraUtils.IMG_THUMB_STREAM);
    }

    @Override
    public Document getSmallThumbnailProfile(String pid) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_THUMB_STREAM);
    }

    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        return isStreamAvailable(pid, FedoraUtils.IMG_THUMB_STREAM);
    }

    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        return getDataStream(pid, FedoraUtils.IMG_PREVIEW_STREAM);
    }

    @Override
    public String getFullThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_THUMB_STREAM);
    }

    @Override
    public InputStream getImageFULL(String pid) throws IOException {
        return getDataStream(pid, FedoraUtils.IMG_FULL_STREAM);
    }

    @Override
    public Document getImageFULLProfile(String pid) throws IOException {
        throw new UnsupportedOperationException("unsupported operation!");
    }

    @Override
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_THUMB_STREAM);
    }

    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        try {
            Node node = dataStreamNode(pid, streamName);
            return node != null;
        } catch (ItemExistsException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (PathNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (VersionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (ConstraintViolationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LockException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (RepositoryException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public boolean isContentAccessible(String pid) throws IOException {
        Session session = null;
        try {
            session = poolOfSession.borrowObject();
            Node rNode = session.getRootNode();
            if (rNode.hasNode(restPid(pid))) {
                return true;
            } else return false;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } finally {
            try {
                poolOfSession.returnObject(session);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new IOException(e);
            }
        }
    }

    @Override
    public FedoraAPIA getAPIA() {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public FedoraAPIM getAPIM() {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        try {
            Node node = dataStreamNode(pid, datastreamName);
            Property property = node.getProperty(JcrConstants.JCR_DATA);
            Binary binary = property.getBinary();
            InputStream stream = binary.getStream();
            return stream;
        } catch (ItemExistsException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (PathNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (VersionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (ConstraintViolationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LockException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (RepositoryException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    private Node dataStreamNode(String pid, String datastreamName) throws RepositoryException, ItemExistsException,
            PathNotFoundException, VersionException, ConstraintViolationException, LockException, Exception {
        Session session = null;
        try {
            session = poolOfSession.borrowObject();
            Node rNode = session.getRootNode();
            Node kramerius = rNode.getNode(restPid(pid));
            if (kramerius.hasNode(datastreamName)) {
                Node node = kramerius.getNode(datastreamName);
                return node;
            } else
                return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } finally {
            try {
                poolOfSession.returnObject(session);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            }
        }
    }

    @Override
    public void observeStreamHeaders(String pid, String datastreamName, StreamHeadersObserver streamObserver)
            throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public InputStream getDataStreamXml(String pid, String datastreamName) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public Document getDataStreamXmlAsDocument(String pid, String datastreamName) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        try {
            Node node = dataStreamNode(pid, datastreamName);
            Property property = node.getProperty(JcrConstants.JCR_MIMETYPE);
            return property.getString();
        } catch (ItemExistsException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (PathNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (VersionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (ConstraintViolationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LockException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (RepositoryException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public String getFedoraVersion() throws IOException {
        return "4.X";
    }

    @Override
    public Document getStreamProfile(String pid, String stream) throws IOException {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Document getObjectProfile(String pid) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public InputStream getFedoraDataStreamsList(String pid) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public Document getFedoraDataStreamsListAsDocument(String pid) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public Date getStreamLastmodifiedFlag(String pid, String stream) throws IOException {
        try {
            Node node = dataStreamNode(pid, stream);
            Property property = node.getProperty(JcrConstants.JCR_LASTMODIFIED);
            return property.getDate().getTime();
        } catch (ItemExistsException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (PathNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (VersionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (ConstraintViolationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LockException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (RepositoryException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }

    }

    @Override
    public List<Map<String, String>> getStreamsOfObject(String pid)  throws IOException {
        Session session = null;
        try {
            List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            session = poolOfSession.borrowObject();
            Node rNode = session.getRootNode();
            Node kramerius = rNode.getNode(restPid(pid));
            NodeIterator nodes = kramerius.getNodes();
            while(nodes.hasNext()) {
                Node subNode = nodes.nextNode();
                if (subNode.isNodeType("kramerius:resource")) {
                    Map<String, String> m = new HashMap<String, String>();
                    m.put("dsid",subNode.getName());
                    m.put("label",subNode.getName());
                    list.add(m);
                }
            }
            return list;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } finally {
            try {
                poolOfSession.returnObject(session);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new IOException(e);
            }
        }
    }

    private synchronized void namespaces(Session session) throws AccessDeniedException, NamespaceException,
            UnsupportedRepositoryOperationException, RepositoryException {
        NamespaceRegistry registry = session.getWorkspace().getNamespaceRegistry();
        List<String> list = Arrays.asList(registry.getURIs());
        if (!list.contains(FedoraNamespaces.KRAMERIUS_URI)) {
            registry.registerNamespace("kramerius", FedoraNamespaces.KRAMERIUS_URI);
        }
        if (!list.contains(FedoraNamespaces.FEDORA_MODELS_URI)) {
            registry.registerNamespace("fedora-models", FedoraNamespaces.FEDORA_MODELS_URI);
        }

        // kramerius resource
        NodeTypeManager mgr = session.getWorkspace().getNodeTypeManager();
        NodeTypeTemplate krameriusResource = mgr.createNodeTypeTemplate();
        krameriusResource.setName("kramerius:resource");
        krameriusResource.setMixin(true);

        // kramerius resource must have fedora model property
        PropertyDefinitionTemplate propDefn = mgr.createPropertyDefinitionTemplate();
        propDefn.setName("fedora-models:model");
        propDefn.setRequiredType(PropertyType.STRING);

        // TODO: Change it
        propDefn.setMandatory(false);

        krameriusResource.getPropertyDefinitionTemplates().add(propDefn);

        // datastream
        NodeTypeTemplate dataStream = mgr.createNodeTypeTemplate();
        dataStream.setName("kramerius:datastream");
        dataStream.setMixin(true);

        NodeTypeDefinition[] nodeTypes = new NodeTypeDefinition[] { krameriusResource, dataStream };
        mgr.registerNodeTypes(nodeTypes, true);

    }
}
