package cz.incad.feedrepo.impl.jackrabbit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Binary;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.JcrConstants;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.feedrepo.solr.ProcessingIndexFeeder;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class JackRabbitRepoListener {

    private static final String INFO_FEDORA_PREFIX = "info:fedora/";

    public static final Logger LOGGER  = Logger.getLogger(JackRabbitRepoListener.class.getName());
    
    private Session session;

    private ProcessingIndexFeeder processingIndexFeeder;

    public JackRabbitRepoListener(Session session) throws LoginException, RepositoryException {
        super();
        this.session = session;
        this.processingIndexFeeder = new ProcessingIndexFeeder();
    }
    
    protected List<String> getTreePredicates() {
        return Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.treePredicates"));
    }
    
    
    public Document getRelsExtDocument(String path) throws NoSuchElementException, IllegalStateException, Exception {
        Node node = session.getNode(path);
        Property property = node.getProperty(JcrConstants.JCR_DATA);
        Binary binary = property.getBinary();
        InputStream stream = binary.getStream();
        Document relsExt = XMLUtils.parseDocument(stream, true);
        return relsExt;
    }
    

    public void onRELSEXTAdded(String path) throws NoSuchElementException, IllegalStateException, Exception {

        String solrHost = KConfiguration.getInstance().getConfiguration().getString("processingSolrHost");

        Document relsExt = getRelsExtDocument(path);
        Element descEl = XMLUtils.findElement(relsExt.getDocumentElement(), "Description",
                FedoraNamespaces.RDF_NAMESPACE_URI);
        Element modelEl = XMLUtils.findElement(descEl, "hasModel",
                FedoraNamespaces.FEDORA_MODELS_URI);
        String sourcePid = cutCommonPrefix(descEl.getAttribute("rdf:about"));
        String model = cutCommonPrefix((modelEl!= null)  ? modelEl.getAttribute("rdf:resource") : "uknown");
        model = (model.contains("model:")) ? model.substring("model:".length()) : model;        
        this.processingIndexFeeder.feedDescriptionDocument(sourcePid, model,solrHost);
        List<Element> els = XMLUtils.getElements(descEl);
        for (Element el : els) {
            String localName = el.getLocalName();
            if (getTreePredicates().contains(localName)) {
                if (el.hasAttribute("rdf:resource")) {
                    String relation = el.getLocalName();
                    String targetPid = cutCommonPrefix(el.getAttributes().getNamedItem("rdf:resource").getNodeValue());
                    JSONObject feedDocument = this.processingIndexFeeder.feedRelationDocument(sourcePid, relation, targetPid,solrHost);
                    LOGGER.log(Level.INFO,"indexer result "+feedDocument.toString());
                }
            }
        }
    }

    private String cutCommonPrefix(String sourcePid) {
        return sourcePid.contains(INFO_FEDORA_PREFIX) ? sourcePid.substring(INFO_FEDORA_PREFIX.length()) : sourcePid;
    }
}
