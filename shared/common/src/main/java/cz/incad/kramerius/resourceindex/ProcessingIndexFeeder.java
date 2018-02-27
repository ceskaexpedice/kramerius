package cz.incad.kramerius.resourceindex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 * This is the helper. It is dedicated ofr creating supporting index which should replace 
 * resource index in the future. 
 * @author pstastny
 *
 */
public class ProcessingIndexFeeder {

    public static final String DEFAULT_ITERATE_QUERY = "*:*";

    private static final String TYPE_RELATION = "relation";
    private static final String TYPE_DESC = "description";

    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexFeeder.class.getName());
    
    private SolrClient solrClient;

    @Inject
    public ProcessingIndexFeeder(@Named("processingUpdate") SolrClient solrClient) {
        super();
        this.solrClient =solrClient;

    }
    
    
    public UpdateResponse feedDescriptionDocument(String pid, String model, String title, String ref, Date date) throws IOException, SolrServerException {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source",pid);
        sdoc.addField("type", TYPE_DESC);
        sdoc.addField("model",model);
        sdoc.addField("dc.title",title);
        sdoc.addField("ref",ref);
        sdoc.addField("date", date);
        return feedDescriptionDocument(sdoc);
    }


    public UpdateResponse feedDescriptionDocument(SolrInputDocument doc) throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.add(doc);
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
        return resp;
    }


    public UpdateResponse deleteProcessingIndex() throws  IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("*:*");
        return response;
    }


    public UpdateResponse deleteByPid(String pid) throws  IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("source:\"" + pid + "\"");
        return response;
    }

    public UpdateResponse deleteByTargetPid(String pid) throws  IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("targetPid:\"" + pid + "\"");
        return response;
    }

    public UpdateResponse deleteDescriptionByPid(String pid) throws  IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("source:\"" + pid + "\" AND type:\"description\"");
        return response;
   }

    public UpdateResponse deleteByRelationsForPid(String pid) throws  IOException, SolrServerException {
        String query = "source:\"" + pid + "\" AND type:\"relation\"";
        UpdateResponse response = this.solrClient.deleteByQuery(query);
        return response;
    }

    public void iterateProcessing(String query, Consumer<SolrDocument> action) throws IOException, SolrServerException {
        ///String query = "*:*";

        SolrQuery solrQuery = new SolrQuery(query);
        int offset = 0;
        int rows = 100;
        long numFound = Integer.MAX_VALUE;
        solrQuery.setStart(offset).setRows(rows);
        QueryResponse response = this.solrClient.query(solrQuery);
        while(offset < numFound) {
            response.getResults().forEach((doc)->{
                action.accept(doc);
            });

            offset += rows;
            solrQuery.setStart(offset).setRows(rows);
            response = this.solrClient.query(solrQuery);
            numFound = response.getResults().getNumFound();
        }

    }

    public List<Pair<String,String>> findByTargetPid(String pid) throws IOException, SolrServerException {
        final List<Pair<String,String>> retvals = new ArrayList<>();
        this.iterateProcessing("targetPid:\""+pid+"\"", (doc)->{
            Pair<String,String> pair = new ImmutablePair<>(doc.getFieldValue("source").toString(), doc.getFieldValue("relation").toString());
            retvals.add(pair);
        });
        return retvals;
    }

    // commit to solr
    public void commit() throws IOException, SolrServerException {
        this.solrClient.commit();
    }


}
