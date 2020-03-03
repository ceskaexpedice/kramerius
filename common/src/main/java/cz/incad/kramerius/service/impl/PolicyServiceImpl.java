package cz.incad.kramerius.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
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

import org.fedora.api.MIMETypedStream;
import org.fedora.api.RelationshipTuple;
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
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.processes.impl.ProcessStarter;
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
        Set<String> pids = fedoraAccess.getPids(pid);
        for (String s : pids) {
            String p = s.replace(INFO, "");
            try{
                setPolicyForNode(p, policyName);
            }catch(Exception ex){
                LOGGER.warning("Cannot set policy for object "+p+", skipping: "+ex);
            }
        }
    }
    
    @Override
    public void setPolicy(String pid, String policyName, String level) throws IOException {
        Set<String> pids = fedoraAccess.getPids(pid);
        if (level != null && level.equals("true")) {
            setPolicyForNode(pid, policyName);
        }
        else {
            for (String s : pids) {
                String p = s.replace(INFO, "");
                try{
                    setPolicyForNode(p, policyName);
                }catch(Exception ex){
                    LOGGER.warning("Cannot set policy for object "+p+", skipping: "+ex);
                }
            }
        }
    }

    public void setPolicyForNode(String pid, String policyName) {
        LOGGER.info("Set policy pid: "+pid+" policy: "+policyName);
        setPolicyDC(pid, policyName);
        setPolicyRELS_EXT(pid, policyName);
        setPolicyPOLICY(pid, policyName);
    }

    private void setPolicyDC(String pid, String policyName) {
        MIMETypedStream stream = fedoraAccess.getAPIA().getDatastreamDissemination(pid, "DC", null);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(stream.getStream()));
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
            fedoraAccess.getAPIM().modifyDatastreamByValue(pid, "DC", null, null, null, null, swr.getBuffer().toString().getBytes("UTF-8"), null, null, "", false);
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

    private void setPolicyRELS_EXT(String pid, String policyName) {
        for (RelationshipTuple t:fedoraAccess.getAPIM().getRelationships(INFO+pid, POLICY_PREDICATE)){
            fedoraAccess.getAPIM().purgeRelationship(INFO+pid, POLICY_PREDICATE, t.getObject(), true, null);
        }
        fedoraAccess.getAPIM().addRelationship(INFO+pid, POLICY_PREDICATE, "policy:"+policyName, true, null);
    }

    private void setPolicyPOLICY(String pid, String policyName) {
        fedoraAccess.getAPIM().purgeDatastream(pid,"POLICY",null,null,"", false);
        fedoraAccess.getAPIM().addDatastream(pid, "POLICY", null, null, false,"application/rdf+xml", null, "http://local.fedora.server/fedora/get/policy:" + policyName + "/POLICYDEF", "E", "A", "DISABLED", null, "");
        //fedoraAccess.getAPIM().modifyDatastreamByReference(pid, "POLICY", null, null, null, null, "http://local.fedora.server/fedora/get/policy:" + policyName + "/POLICYDEF", null, null, null, false);
    }

    /**
     * args[0] - policy to set (public, private)
     * args[1] - uuid of the root item
     * args[2] - if this process is only for the selected level
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {
        LOGGER.info("PolicyService: "+Arrays.toString(args));
        if (args.length >= 2) {
            //TODO: I18N
            ProcessStarter.updateName("Priznak  '"+args[0]+" pro titul "+args[1]);
        }
        PolicyServiceImpl inst = new PolicyServiceImpl();
        inst.fedoraAccess = new FedoraAccessImpl(null, null);
        inst.configuration = KConfiguration.getInstance();
        inst.setPolicy(args[1], args[0], args[2]);
        IndexerProcessStarter.spawnIndexer(true, "Reindex policy "+args[1]+":"+args[0], args[1]);
        LOGGER.info("PolicyService finished.");
    }
}
