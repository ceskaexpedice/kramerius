package cz.incad.kramerius.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import cz.incad.kramerius.service.MigrateSolrIndex;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.BasicAuthenticationFilter;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUtils;

public class MigrateSolrIndexImpl implements MigrateSolrIndex{

    private static final String DEST_SOLR_HOST = ".dest.solrHost";
    public static final String QEURY_POSTFIX = "select?q=*:*";
    public static final int ROWS = 500;
    public static final int START = 0;
    
    public static final Logger LOGGER = Logger.getLogger(MigrateSolrIndexImpl.class.getName());
    
    private Client client;

    public MigrateSolrIndexImpl() {
        this.client = Client.create();
    }

    @Override
    public void migrate() throws MigrateSolrIndexException {
        try {
            long start = System.currentTimeMillis();
            String solrQuery = KConfiguration.getInstance().getSolrHost();
            LOGGER.info("Index migration");
            LOGGER.info(String.format("\t source index :%s", solrQuery));
            LOGGER.info(String.format("\t destination index :%s", destinationServer()));
            solrQuery += (solrQuery.endsWith("/") ? "" : "/")+ QEURY_POSTFIX;
            int max = Integer.MAX_VALUE;
            int cursor = 0;
            while(cursor < max) {
                max = processSource(solrQuery, cursor);
                if (max != Integer.MAX_VALUE) {
                    LOGGER.info("Number of documents is :"+max);
                }
                cursor += ROWS;
                commit();
            }
            commit();
            long stop = System.currentTimeMillis();
            LOGGER.info("Migration has been finished. It took "+(stop - start));
        } catch (UniformInterfaceException e) {
            throw new MigrateSolrIndexException(e);
        } catch (ClientHandlerException e) {
            throw new MigrateSolrIndexException(e);
        } catch (ParserConfigurationException e) {
            throw new MigrateSolrIndexException(e);
        } catch (SAXException e) {
            throw new MigrateSolrIndexException(e);
        } catch (IOException e) {
            throw new MigrateSolrIndexException(e);
        }
    }
    
    private void commit() throws MigrateSolrIndexException {
        try {
            String destSolr = destinationServer()+"?commit=true";
            WebResource r = this.client.resource(destSolr);
            Document document = XMLUtils.crateDocument("add");
            StringWriter strWriter = new StringWriter();
            XMLUtils.print(document, strWriter);
            String t = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(strWriter.toString(), MediaType.TEXT_XML).post(String.class);
        } catch (UniformInterfaceException e) {
            throw new MigrateSolrIndexException(e);
        } catch (ClientHandlerException e) {
            throw new MigrateSolrIndexException(e);
        } catch (ParserConfigurationException e) {
            throw new MigrateSolrIndexException(e);
        } catch (TransformerException e) {
            throw new MigrateSolrIndexException(e);
        }
    }

    private String destinationServer() throws MigrateSolrIndexException {
        String targetDest = KConfiguration.getInstance().getConfiguration().getString(DEST_SOLR_HOST);
        String source = KConfiguration.getInstance().getSolrHost();
        if (targetDest == null || !StringUtils.isAnyString(targetDest)) {
            throw new MigrateSolrIndexException(String.format("missing property %s", DEST_SOLR_HOST));
        }
        if (targetDest.startsWith(source)) {
            throw new MigrateSolrIndexException(String.format("the same index problem %s %s", targetDest,source));
        }
        return targetDest;
    }
    
    private void sendToDest(Element edoc)  throws MigrateSolrIndexException {
        try {
            StringWriter writer = new StringWriter();
            String destSolr = destinationServer();
            WebResource r = this.client.resource(destSolr);
            
            Document ndoc = XMLUtils.crateDocument("add");

            Element docElem = ndoc.createElement("doc");
            ndoc.getDocumentElement().appendChild(docElem);
            
            NodeList childNodes = edoc.getChildNodes();
            for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    List<String> primitiveVals = Arrays.asList("str","int","bool","date");
                    if (primitiveVals.contains(node.getNodeName())) {
                        simpleValue(ndoc,docElem, node,null);
                    } else {
                        arrayValue(ndoc,docElem,node);
                    }
                }
            }
            XMLUtils.print(ndoc, writer);
            ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
            if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
                throw new MigrateSolrIndexException("Exiting with staus:"+resp.getStatus());
            }
        } catch (UniformInterfaceException e) {
            throw new MigrateSolrIndexException(e);
        } catch (ClientHandlerException e) {
            throw new MigrateSolrIndexException(e);
        } catch (TransformerException e) {
            throw new MigrateSolrIndexException(e);
        } catch (ParserConfigurationException e) {
            throw new MigrateSolrIndexException(e);
        }
    }

    public void simpleValue(Document ndoc, Element docElm, Node node, String derivedName) {
        Element strElm = ndoc.createElement("field");
        strElm.setAttribute("name", derivedName != null ? derivedName : ((Element)node).getAttribute("name"));
        docElm.appendChild(strElm);
        strElm.setTextContent(node.getTextContent());
    }

    public void arrayValue(Document ndoc, Element docElm, Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                simpleValue(ndoc,docElm, n, ((Element)node).getAttribute("name"));
            }
        }
    }

    private int processSource(String solrQuery, int cursor)
            throws ParserConfigurationException, SAXException, IOException, MigrateSolrIndexException {
        String formatted = solrQuery+String.format("&rows=%d&start=%d",ROWS, cursor);
        LOGGER.info(String.format("processing %s",formatted));
        
        WebResource r = this.client.resource(formatted);
        String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
        Document parseDocument = XMLUtils.parseDocument(new StringReader(t));
        Element result = XMLUtils.findElement(parseDocument.getDocumentElement(), new XMLUtils.ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                return nodeName.equals("result");
           }
        });
        //numFound
        String attribute = result.getAttribute("numFound");
        int numfound = Integer.parseInt(attribute);
        
        List<Element> docs = XMLUtils.getElements(result,new XMLUtils.ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                return nodeName.equals("doc");
           }
        });
        for (Element doc : docs) {
            sendToDest(doc);
        }
        return numfound;
    }
    
    public static void main(String[] args) throws MigrateSolrIndexException {
        MigrateSolrIndexImpl impl = new MigrateSolrIndexImpl();
        impl.migrate();
    }
}
