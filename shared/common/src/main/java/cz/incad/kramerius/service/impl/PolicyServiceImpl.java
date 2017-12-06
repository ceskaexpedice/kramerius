package cz.incad.kramerius.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.FedoraUtils;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.service.PolicyService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class PolicyServiceImpl implements PolicyService {
    public static final Logger LOGGER = Logger.getLogger(PolicyServiceImpl.class.getName());

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    KConfiguration configuration;

    @Override
    public void setPolicy(String pid, String policyName) throws IOException {
        List<String> pids = fedoraAccess.getPids(pid);
        for (String s : pids) {
            String p = s.replace(INFO, "");
            try{
                setPolicyForNode(p, policyName);
            }catch(Exception ex){
                LOGGER.warning("Cannot set policy for object "+p+", skipping: "+ex);
            }
        }
    }

    public void setPolicyForNode(String pid, String policyName) throws RepositoryException {
        LOGGER.info("Set policy pid: "+pid+" policy: "+policyName);
        setPolicyDC(pid, policyName);
        setPolicyRELS_EXT(pid, policyName);
        setPolicyPOLICY(pid, policyName);
    }

    private void setPolicyDC(String pid, String policyName) throws RepositoryException {
        RepositoryDatastream dcStream = fedoraAccess.getInternalAPI().getObject(pid).getStream(FedoraUtils.DC_STREAM);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(dcStream.getContent());
            NodeList nodes = selectPolicyDCNodes(doc);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                node.setTextContent("policy:" + policyName);
            }

            if (((doc.getFeature("Core", "3.0")) == null)
                    || ((doc.getFeature("LS", "3.0")) == null)) {
                throw new UnsupportedOperationException("DOM3 unsupported");
            }
            DOMImplementation domImpl = doc.getImplementation();
            DOMImplementationLS domImplLS = (DOMImplementationLS) domImpl.getFeature("LS", "3.0");

            LSSerializer ser = domImplLS.createLSSerializer();
            DOMConfiguration conf = (DOMConfiguration)ser;
            conf.setParameter("xml-declaration", false);
            LSOutput lso = domImplLS.createLSOutput();
            lso.setEncoding("UTF-8");
            StringWriter swr = new StringWriter();
            lso.setCharacterStream(swr);
            ser.write(doc, lso);
            fedoraAccess.getInternalAPI().getObject(pid).deleteStream(FedoraUtils.DC_STREAM);
            fedoraAccess.getInternalAPI().getObject(pid).createStream(FedoraUtils.DC_STREAM, "text/xml", new ByteArrayInputStream(swr.getBuffer().toString().getBytes("UTF-8")));
        } catch (Throwable t) {
            LOGGER.severe("Error while setting DC policy" + t);
            throw new RuntimeException(t);
        }
    }

    public NodeList selectPolicyDCNodes(Document doc)
            throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {

            @SuppressWarnings("unchecked")
            @Override
            public Iterator getPrefixes(String arg0) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPrefix(String arg0) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix == null)
                    throw new NullPointerException("Null prefix");
                else if ("dc".equals(prefix))
                    return "http://purl.org/dc/elements/1.1/";

                return XMLConstants.XML_NS_URI;
            }
        });
        XPathExpression expr = xpath.compile("//dc:rights");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        return nodes;
    }

    
    public FedoraAccess getFedoraAccess() {
        return fedoraAccess;
    }

    public void setFedoraAccess(FedoraAccess fedoraAccess) {
        this.fedoraAccess = fedoraAccess;
    }

    public KConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(KConfiguration configuration) {
        this.configuration = configuration;
    }


    private static final String POLICY_PREDICATE = "http://www.nsdl.org/ontologies/relationships#policy";
    private static final String INFO = "info:fedora/";

    private void setPolicyRELS_EXT(String pid, String policyName) throws RepositoryException {
        Repository repo = fedoraAccess.getInternalAPI();
        if (repo.getObject(pid).relationsExists("policy", FedoraNamespaces.KRAMERIUS_URI)) {
            repo.getObject(pid).removeRelationsByNameAndNamespace("policy",FedoraNamespaces.KRAMERIUS_URI);
        }

        repo.getObject(pid).addLiteral("policy",FedoraNamespaces.KRAMERIUS_URI, "policy:"+policyName);

    }

    private void setPolicyPOLICY(String pid, String policyName) {
        // We don't need this
        //fedoraAccess.getAPIM().purgeDatastream(pid,"POLICY",null,null,"", false);
        //fedoraAccess.getAPIM().addDatastream(pid, "POLICY", null, null, false,"application/rdf+xml", null, "http://local.fedora.server/fedora/get/policy:" + policyName + "/POLICYDEF", "E", "A", "DISABLED", null, "");
        //fedoraAccess.getAPIM().modifyDatastreamByReference(pid, "POLICY", null, null, null, null, "http://local.fedora.server/fedora/get/policy:" + policyName + "/POLICYDEF", null, null, null, false);
    }

    /**
     * args[1] - uuid of the root item (withou uuid: prefix)
     * args[0] - policy to set (public, private)
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {
        LOGGER.info("PolicyService: "+Arrays.toString(args));
        if (args.length >= 2) {
            //TODO: I18N
            try {
                ProcessStarter.updateName("Priznak  '"+args[0]+" pro titul "+args[1]);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,e.getMessage(),e);
            }
        }
        PolicyServiceImpl inst = new PolicyServiceImpl();

        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        inst.fedoraAccess = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));

        inst.configuration = KConfiguration.getInstance();
        inst.setPolicy(args[1], args[0]);
        try {
            IndexerProcessStarter.spawnIndexer(true, "Reindex policy "+args[1]+":"+args[0], args[1]);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,e.getMessage(),e);
        }
        LOGGER.info("PolicyService finished.");
    }
}
