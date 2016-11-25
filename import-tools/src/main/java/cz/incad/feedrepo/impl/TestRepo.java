package cz.incad.feedrepo.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.jcr.AccessDeniedException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.kahadb.util.ByteArrayInputStream;

import cz.incad.kramerius.FedoraNamespaces;

public class TestRepo {

    private static synchronized void namespaces(Session currentSession) throws AccessDeniedException,
            NamespaceException, UnsupportedRepositoryOperationException, RepositoryException {
        NamespaceRegistry registry = currentSession.getWorkspace().getNamespaceRegistry();
        List<String> list = Arrays.asList(registry.getURIs());
        if (!list.contains(FedoraNamespaces.KRAMERIUS_URI)) {
            registry.registerNamespace("kramerius", FedoraNamespaces.KRAMERIUS_URI);
        }
        if (!list.contains(FedoraNamespaces.FEDORA_MODELS_URI)) {
            registry.registerNamespace("fedora-models", FedoraNamespaces.FEDORA_MODELS_URI);
        }

        // kramerius resource
        NodeTypeManager mgr = currentSession.getWorkspace().getNodeTypeManager();
        NodeTypeTemplate krameriusResource = mgr.createNodeTypeTemplate();
        krameriusResource.setName("kramerius:resource");
        krameriusResource.setMixin(true);

        // kramerius resource must have fedora model property and property must
        // correspond with model node
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

        // kramerius model; model
        NodeTypeTemplate modelResource = mgr.createNodeTypeTemplate();
        modelResource.setName("kramerius:model");
        modelResource.setMixin(true);

        NodeTypeDefinition[] nodeTypes = new NodeTypeDefinition[] { krameriusResource, dataStream, modelResource };
        mgr.registerNodeTypes(nodeTypes, true);
    }

    public static void main(String[] args) throws Exception {
        Repository repo = JcrUtils.getRepository("file:///C:/Users/pstastny.SEARCH/jck_repo_2");
        Session session = repo.login(new SimpleCredentials("admin","admin".toCharArray()));
        //namespaces(session);
        
        Node rootNode = session.getRootNode();
        NodeDefinition definition = rootNode.getDefinition();
        System.out.println(definition.getDefaultPrimaryTypeName());
        System.out.println(definition.getDeclaringNodeType().getName());
        //Cnd`Importer
        //createTestData(session);
        
        NodeTypeManager mgr = session.getWorkspace().getNodeTypeManager();
        NodeTypeIterator allNodeTypes = mgr.getAllNodeTypes();
        while(allNodeTypes.hasNext()) {
            NodeType nextNodeType = allNodeTypes.nextNodeType();
            System.out.println("\t"+nextNodeType.getName());
            PropertyDefinition[] propertyDefinitions = nextNodeType.getPropertyDefinitions();
            for (PropertyDefinition propertyDefinition : propertyDefinitions) {
                System.out.println("\t\t"+propertyDefinition.getName());
            }
        }
        
        System.out.println(rootNode);
        System.out.println(Arrays.asList(repo.getDescriptorKeys()));
        session.logout();
    }
    
    
    protected static void createTestData(Session session) throws Exception {
        JackrabbitNodeTypeManager manager = (JackrabbitNodeTypeManager)
            session.getWorkspace().getNodeTypeManager();
        String cnd =
            "<nt='http://www.jcp.org/jcr/nt/1.0'>\n"
            + "<mix='http://www.jcp.org/jcr/mix/1.0'>\n" 
            + "[nt:myversionable] > nt:unstructured, mix:versionable\n";
        manager.registerNodeTypes(
                new ByteArrayInputStream(cnd.getBytes("UTF-8")),
                JackrabbitNodeTypeManager.TEXT_X_JCR_CND);

        Node root = session.getRootNode();
        Node test = root.addNode("test", "nt:unstructured");
        root.save();

        Node versionable = createVersionable(test);
        createProperties(test, versionable);
        createLock(test);
        createUsers(session);
    }
    
    
    protected static void createLock(Node parent) throws RepositoryException {
        Node lock = parent.addNode("lock", "nt:unstructured");
        lock.addMixin("mix:lockable");
        parent.save();

        lock.lock(true, false);
    }

    protected static void createUsers(Session session) throws RepositoryException {
    }

    protected static Node createVersionable(Node parent) throws RepositoryException {
        Node versionable = parent.addNode("versionable", "nt:myversionable");
        versionable.setProperty("foo", "A");
        parent.save();

        VersionHistory history = versionable.getVersionHistory();
        Version versionA = versionable.checkin();
        history.addVersionLabel(versionA.getName(), "labelA", false);
        versionable.checkout();
        versionable.setProperty("foo", "B");
        parent.save();
        Version versionB = versionable.checkin();
        history.addVersionLabel(versionB.getName(), "labelB", false);
        return versionable;
    }

    
    protected static void createProperties(Node parent, Node reference)
            throws RepositoryException {
        Node properties = parent.addNode("properties", "nt:unstructured");
        properties.setProperty("boolean", true);
        properties.setProperty("double", 0.123456789);
        properties.setProperty("long", 1234567890);
        properties.setProperty("reference", reference);
        properties.setProperty("string", "test");

        properties.setProperty("multiple", new String[] { "a", "b", "c" });

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1234567890);
        properties.setProperty("date", calendar);

        byte[] binary = new byte[100 * 1000];
        new Random(1234567890).nextBytes(binary);
        properties.setProperty("binary", new ByteArrayInputStream(binary));

        parent.save();
    }
}
