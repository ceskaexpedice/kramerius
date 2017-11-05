package cz.incad.kramerius.resourceindex;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

/**
 * This is the helper. It is dedicated ofr creating supporting index which should replace 
 * resource index in the future. 
 * @author pstastny
 *
 */
public class ProcessingIndexFeeder {

    private static final String TYPE_RELATION = "relation";
    private static final String TYPE_DESC = "description";

    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexFeeder.class.getName());
    
    private SolrClient solrClient;

    @Inject
    public ProcessingIndexFeeder(@Named("processingUpdate") SolrClient solrClient) {
        super();
        this.solrClient =solrClient;

    }
    
    
    public UpdateResponse feedDescriptionDocument(String pid, String model, String title) throws IOException, SolrServerException {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source",pid);
        sdoc.addField("type", TYPE_DESC);
        sdoc.addField("model",model);
        sdoc.addField("dc.title",title);
        return feedDescriptionDocument(sdoc);
    }


    public UpdateResponse feedDescriptionDocument(SolrInputDocument doc) throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.add(doc);
        //this.solrClient.commit();
        return response;
    }

    public UpdateResponse feedRelationDocument(String sourePid,  String relation, String targetPid) throws IOException, SolrServerException {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source",sourePid);
        sdoc.addField("type",TYPE_RELATION);
        sdoc.addField("relation",relation);
        sdoc.addField("targetPid",targetPid);

        return feedRelationDocument(sdoc);
    }


    public UpdateResponse feedRelationDocument(SolrInputDocument sdoc) throws IOException, SolrServerException {
        UpdateResponse resp = this.solrClient.add(sdoc);
        //this.solrClient.commit();
        return resp;
    }


    public UpdateResponse deleteByPid(String pid) throws  IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("source:\"" + pid + "\"");
        //this.solrClient.commit();
        return response;
    }

    public UpdateResponse deleteDescriptionByPid(String pid) throws  IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("source:\"" + pid + "\" AND type:\"description\"");
        //this.solrClient.commit();
        return response;
   }

    public UpdateResponse deleteByRelationsForPid(String pid) throws  IOException, SolrServerException {
        String query = "source:\"" + pid + "\" AND type:\"relation\"";
        UpdateResponse response = this.solrClient.deleteByQuery(query);
        //this.solrClient.commit();
        return response;
    }


    public void commit() throws IOException, SolrServerException {
        this.solrClient.commit();
    }
}
