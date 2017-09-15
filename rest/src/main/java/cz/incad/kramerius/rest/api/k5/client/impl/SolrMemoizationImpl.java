package cz.incad.kramerius.rest.api.k5.client.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.solr.SolrUtils;

public class SolrMemoizationImpl implements SolrMemoization{

    public static final Logger LOGGER = Logger.getLogger(SolrMemoization.class.getName());
    
    @Inject
    SolrAccess solrAccess;
    
    private Map<String, Element> elms = new HashMap<String, Element>();
    
    @Override
    public void rememberIndexedDoc(String pid, Element elm) {
        this.elms.put(pid, elm);
    }

    @Override
    public Element getRememberedIndexedDoc(String pid) {
        return elms.get(pid);
    }

    @Override
    public synchronized Element askForIndexDocument(String pid) throws IOException {
        if (!this.elms.containsKey(pid)) {
            if (PIDSupport.isComposedPID(pid)) {
                String parentPid = PIDSupport.first(pid);
                String convertedPid = PIDSupport.convertToSOLRType(pid);
                
                int offset = 0;
                int numFound = Integer.MAX_VALUE;
                List<Element> foundElements = new ArrayList<Element>();
                while(offset < numFound) {
                    Document resultsDocs = solrAccess.getSolrDataDocmentsByParentPid(parentPid, ""+offset);
                    Element result = XMLUtils.findElement(resultsDocs.getDocumentElement(), "result");
                    if (result != null) {
                        String snumFound = result.getAttribute("numFound");
                        numFound = Integer.parseInt(snumFound);
                        
                        List<Element> elements = XMLUtils.getElements(result, new XMLUtils.ElementsFilter() {
                                @Override
                                public boolean acceptElement(Element element) {
                                    return (element.getNodeName().equals("doc"));
                                }
                            });
                        foundElements.addAll(elements);
                        offset += elements.size();
                    }
                }
                
                for (Element doc : foundElements) {
                    try {
                        String disectPid = SolrUtils.disectPid(doc);
                        disectPid = PIDSupport.convertToK4Type(disectPid);
                        this.elms.put(disectPid, doc);
                    } catch (XPathExpressionException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            } else {
                Document doc = solrAccess.getSolrDataDocument(pid);
                if (doc !=  null) {
                    Element result = XMLUtils.findElement(doc.getDocumentElement(), "result");
                    if (result != null) {
                        Element d = XMLUtils.findElement(result, "doc");
                        this.elms.put(pid, d);
                    }
                }
            }
        }
        return this.elms.get(pid);
    }

    @Override
    public void clearMemo() {
        this.elms.clear();
    }

    public SolrAccess getSolrAccess() {
        return solrAccess;
    }

    public void setSolrAccess(SolrAccess solrAccess) {
        this.solrAccess = solrAccess;
    }
}
