package cz.incad.kramerius.resourceindex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.derby.impl.tools.sysinfo.Main;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.icu.text.SimpleDateFormat;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * SOLR implemenation of the resource index; 
 * Stores relation and information from RELS-EXT
 * @author pstastny
 */
public class SolrResourceIndex implements IResourceIndex {

    public static final SimpleDateFormat XSD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final String DATE_FORMAT_TYPE = "http://www.w3.org/2001/XMLSchema#dateTime";
    
    private SolrClient solrClient;

    @Inject
    public SolrResourceIndex(@Named("processingQuery") SolrClient solrClient) {
        super();
        this.solrClient = solrClient;
    }

    public List<Map<String,String>> getSubjects(String pid) throws ResourceIndexException {
        try {
            int limit = 1000;
            int start = 0;
            List<Map<String,String>> retvals = new ArrayList<>();

            QueryResponse response = this.solrClient.query(new SolrQuery("targetPid:\""+pid+"\" AND type:description").setRows(limit).setStart(start));
            long found = response.getResults().getNumFound();
            while(start < found) {
                for (SolrDocument sDoc : response.getResults()) {
                    Map<String,String> map = new HashMap<>();
                    map.put("source",sDoc.getFieldValue("source").toString());
                    map.put("relation",sDoc.getFieldValue("relation").toString());
                    map.put("targetPid",sDoc.getFieldValue("targetPid").toString());
                    retvals.add(map);
                }

                response = this.solrClient.query(new SolrQuery("targetPid:\""+pid+"\" AND type:description").setRows(limit).setStart(start));
                start += limit;
            }
            return retvals;
        } catch (SolrServerException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        }
    }

    @Override
    public List<String> getObjectsByModel(String model, int limit, int offset, String orderby, String orderDir) throws ResourceIndexException {
        try {
            List<String> retvals = new ArrayList<>();
            QueryResponse response = this.solrClient.query(new SolrQuery("model:\""+model+"\" AND type:description").setRows(limit).setStart(offset));
            SolrDocumentList results = response.getResults();
            for (SolrDocument sDoc : results) {
                retvals.add(sDoc.getFieldValue("source").toString());
            }
            return retvals;
        } catch (SolrServerException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        }
    }


    @Override
    public Document getFedoraModels() throws ResourceIndexException {
        try {
            QueryResponse response = this.solrClient.query(new SolrQuery("type:description").setRows(0).setFacet(true).addFacetField("model"));
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element rootElement = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI, "sparql");
            rootElement.appendChild(createHeader(doc, "object","title"));
            rootElement.appendChild(createModelResults(doc, response.getFacetField("model").getValues()));
            doc.appendChild(rootElement);
            return doc;
        } catch (ParserConfigurationException e) {
            throw new ResourceIndexException(e);
        } catch (SolrServerException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        }
    }

    private Element createHeader(Document doc, String ... variables) {
        Element head = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI,"head");
        for (String var : variables) {
            Element variable = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI, "variable");
            variable.setAttribute("name", var);
            head.appendChild(variable);
        }
        return head;
    }

    private Element createModelResults(Document doc, List<Count> values) {
        Element results = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI,"results");
        for (Count count : values) {
            Element result = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI,"result");
            Element object = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI,"object");
            //page info:fedora/model:page
            object.setAttribute("uri", "info:fedora/model:"+count.getName());
            result.appendChild(object);
            

            Element title = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI,"title");
            title.setTextContent(count.getName());
            result.appendChild(title);
            
            results.appendChild(result);
        }
        return results;
    }
    


    @Override
    public List<String> getParentsPids(String pid) throws ResourceIndexException {
        try {
            List<String> retvals = new ArrayList<>();
            QueryResponse response = this.solrClient.query(new SolrQuery("targetPid:\""+pid+"\" AND type:relation").setRows(1000));
            SolrDocumentList results = response.getResults();
            for (SolrDocument sDoc : results) {
                retvals.add(sDoc.getFieldValue("source").toString());
            }
            return retvals;
        } catch (SolrServerException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        }
        
    }

    @Override
    public List<String> getPidPaths(String pid) throws ResourceIndexException {
        return ResourceIndexUtils.getPidPaths(pid, this);
    }

    @Override
    public List<String> getObjectsInCollection(String collection, int limit, int offset) throws ResourceIndexException {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public boolean existsPid(String pid) throws ResourceIndexException {
        return false;
    }

    @Override
    public Document getVirtualCollections() throws ResourceIndexException {
        throw new UnsupportedOperationException("unsupported");
    }



    @Override
    public List<String> getFedoraPidsFromModel(String model, int limit, int offset) throws ResourceIndexException {
        throw new UnsupportedOperationException("unsupported");
    }
    

    
    
    @Override
    public Document getFedoraObjectsFromModelExt(String model, int limit, int offset, String orderby, String orderDir)
            throws ResourceIndexException {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element rootElement = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI, "sparql");
            rootElement.appendChild(createHeader(doc, "object","title","date"));
            
            QueryResponse response = this.solrClient.query(new SolrQuery("model:\""+model+"\" AND type:description").setRows(limit).setStart(offset));
            rootElement.appendChild(createHeader(doc, "object","title"));
            rootElement.appendChild(createDocumentResults(doc, response.getResults()));

            doc.appendChild(rootElement);
            return doc;
        } catch (ParserConfigurationException e) {
            throw new ResourceIndexException(e);
        } catch (SolrServerException e) {
            throw new ResourceIndexException(e);
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        }
    }



    private Element createDocumentResults(Document doc, SolrDocumentList doclist) {
        Element results = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI,"results");
        for (SolrDocument solrDocument : doclist) {
            Element result = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI,"result");
            
            Element title = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI,"title");
            title.setTextContent(solrDocument.get("source").toString());
            result.appendChild(title);
            
            Element object = doc.createElementNS(FedoraNamespaces.SPARQL_NAMESPACE_URI,"object");
            object.setTextContent("info:fedora/"+solrDocument.get("source").toString());
            result.appendChild(object);
            
            results.appendChild(result);
        }
        return results;
    }
}
