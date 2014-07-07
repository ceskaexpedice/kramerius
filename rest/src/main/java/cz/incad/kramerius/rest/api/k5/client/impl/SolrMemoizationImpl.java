package cz.incad.kramerius.rest.api.k5.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.utils.XMLUtils;

public class SolrMemoizationImpl implements SolrMemoization{

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
                pid = PIDSupport.convertToSOLRType(pid);
            }
            Document doc = solrAccess.getSolrDataDocument(pid);
            Element result = XMLUtils.findElement(doc.getDocumentElement(), "result");
            if (result != null) {
                Element d = XMLUtils.findElement(result, "doc");
                this.elms.put(pid, d);
            }
        }
        return this.elms.get(pid);
    }

    @Override
    public void clearMemo() {
        this.elms.clear();
    }
}
