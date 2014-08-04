/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.k5indexer;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author alberto
 */
public class Server {
    String url;
    SolrServer server;
    public Server(String url){
        this.url = url;
        server = new HttpSolrServer(url);
    }
    public void commit() throws SolrServerException, IOException{
        server.commit();
    }
    
    public SolrDocumentList getDocs(String query) throws SolrServerException{
        SolrQuery squery = new SolrQuery();
        squery.setQuery(query);
        squery.addSort("price", SolrQuery.ORDER.asc );
        QueryResponse rsp = server.query(squery);
        return rsp.getResults();
    }
}
