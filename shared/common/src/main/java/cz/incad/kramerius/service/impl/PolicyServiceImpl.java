package cz.incad.kramerius.service.impl;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.service.PolicyService;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: remove, but first fix process MovingWall, that uses it through ApplyMWUtils
@Deprecated
public class PolicyServiceImpl implements PolicyService {
    public static final Logger LOGGER = Logger.getLogger(PolicyServiceImpl.class.getName());

    /* TODO AK_NEW
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

     */
    @Inject
    AkubraRepository akubraRepository;

    KConfiguration configuration = KConfiguration.getInstance();

    @Override
    public void setPolicy(String pid, String policyName) throws IOException {
        List<String> pids = RelsExtUtils.getPids(pid, akubraRepository);
        for (String s : pids) {
            String p = s.replace(INFO, "");
            try {
                setPolicyForNode(p, policyName);
            } catch (Exception ex) {
                LOGGER.warning("Cannot set policy for object " + p + ", skipping: " + ex);
            }
        }
    }

    @Override
    public void setPolicy(String pid, String policyName, String level) throws IOException {
        List<String> pids = RelsExtUtils.getPids(pid, akubraRepository);
        if (level != null && level.equals("true")) {
            try {
                setPolicyForNode(pid, policyName);
            } catch (Exception ex) {
                LOGGER.warning("Cannot set policy for object " + pid + ", skipping: " + ex);
            }
        } else {
            for (String s : pids) {
                String p = s.replace(INFO, "");
                try {
                    setPolicyForNode(p, policyName);
                } catch (Exception ex) {
                    LOGGER.warning("Cannot set policy for object " + p + ", skipping: " + ex);
                }
            }
        }
    }

    public void setPolicyForNode(String pid, String policyName)  {
        LOGGER.info("Set policy pid: " + pid + " policy: " + policyName);
        akubraRepository.doWithWriteLock(pid, () -> {
            setPolicyDC(pid, policyName);
            setPolicyRELS_EXT(pid, policyName);
            return null;
        });
    }

    private void setPolicyDC(String pid, String policyName) {
        try {
            InputStream inputStream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC);
            Document doc = DomUtils.streamToDocument(inputStream);
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
            DOMConfiguration conf = (DOMConfiguration) ser;
            conf.setParameter("xml-declaration", false);
            LSOutput lso = domImplLS.createLSOutput();
            lso.setEncoding("UTF-8");
            StringWriter swr = new StringWriter();
            lso.setCharacterStream(swr);
            ser.write(doc, lso);
            ByteArrayInputStream bis = new ByteArrayInputStream(swr.getBuffer().toString().getBytes(StandardCharsets.UTF_8));
            akubraRepository.updateXMLDatastream(pid, KnownDatastreams.BIBLIO_DC.name(), "text/xml", bis);
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


    public AkubraRepository getAkubraRepository() {
        return akubraRepository;
    }

    public void setAkubraRepository(AkubraRepository akubraRepository) {
        this.akubraRepository = akubraRepository;
    }



    private static final String POLICY_PREDICATE = "http://www.nsdl.org/ontologies/relationships#policy";
    private static final String INFO = "info:fedora/";

    private void setPolicyRELS_EXT(String pid, String policyName) {
        if(akubraRepository.relsExtRelationExists(pid, "policy", RepositoryNamespaces.KRAMERIUS_URI)){
            akubraRepository.relsExtRemoveRelationsByNameAndNamespace(pid,"policy", RepositoryNamespaces.KRAMERIUS_URI);
        }
        akubraRepository.relsExtAddLiteral(pid, "policy", RepositoryNamespaces.KRAMERIUS_URI, "policy:" + policyName);
    }

    /**
     * args[1] - uuid of the root item (withou uuid: prefix)
     * args[0] - policy to set (public, private)
     *
     * @throws IOException
     * @see cz.incad.kramerius.processes.SetPolicyProcess
     * @deprecated
     */
    @Deprecated
    public static void main(String[] args) throws IOException {
        LOGGER.info("PolicyService: " + Arrays.toString(args));
        if (args.length >= 2) {
            //TODO: I18N
            try {
                ProcessStarter.updateName("Priznak  '" + args[0] + " pro titul " + args[1]);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        PolicyServiceImpl inst = new PolicyServiceImpl();

        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
        // TODO AK_NEW inst.fedoraAccess = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        inst.akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));

        inst.configuration = KConfiguration.getInstance();
        if (args.length >= 3) {
            inst.setPolicy(args[1], args[0], args[2]);
        } else {
            inst.setPolicy(args[1], args[0]);
        }
        try {
            IndexerProcessStarter.spawnIndexer(true, "Reindex policy " + args[1] + ":" + args[0], args[1]);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        LOGGER.info("PolicyService finished.");
    }
}
