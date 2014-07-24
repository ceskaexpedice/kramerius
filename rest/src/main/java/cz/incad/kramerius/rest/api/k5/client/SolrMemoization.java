package cz.incad.kramerius.rest.api.k5.client;

import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * SOLR  memoization support  
 * @author pavels
 */
public interface SolrMemoization {

    /**
     * Remember document from index
     * @param pid PID
     * @param elm W3C Element represents indexed document
     */
    public void rememberIndexedDoc(String pid, Element elm);

    /**
     * Returns remembered document
     * @param pid PID
     */
    public Element getRememberedIndexedDoc(String pid);

    /**
     * Ask SOLR for the index document
     * @param pid PID
     * @return
     * @throws IOException
     */
    public Element askForIndexDocument(String pid) throws IOException;

    /**
     * Clear remembered elements
     */
    public void clearMemo();
}
