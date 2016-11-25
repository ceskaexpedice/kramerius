package cz.incad.feedrepo.solr;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

import cz.incad.kramerius.utils.conf.KConfiguration;

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
    
    private Client client;

    public ProcessingIndexFeeder() {
        super();
        this.client = Client.create();
        client.addFilter(new LoggingFilter(LOGGER));
    }
    
    
    public JSONObject feedDescriptionDocument(String pid, String model, String solrHost) {
        JSONObject docObject = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("source", pid);
        jsonObject.put("type", TYPE_DESC);
        jsonObject.put("model",model);
        docObject.put("doc", jsonObject);
        return feedDescriptionDocument(docObject, solrHost);
    }


    public JSONObject feedDescriptionDocument(JSONObject jsonObj, String solrHost) {
        String updateEndpoint = solrHost+"/update";
        JSONObject addOperation = new JSONObject();
        addOperation.put("add", jsonObj);
        WebResource r = this.client.resource(updateEndpoint);
        String string = addOperation.toString();
        String post = r.accept(MediaType.APPLICATION_JSON).entity(string, MediaType.APPLICATION_JSON).post(String.class);
        return new JSONObject(post);
    }

    public JSONObject feedRelationDocument(String sourePid,  String relation, String targetPid, String solrHost) {
        JSONObject docObject = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("source", sourePid);
        jsonObject.put("type", TYPE_RELATION);
        //jsonObject.put("sourceModel", sourceModel);
        jsonObject.put("relation", relation);
        jsonObject.put("targetPid", targetPid);
        docObject.put("doc", jsonObject);
        return feedRelationDocument(docObject, solrHost);
    }

    public JSONObject feedRelationDocument(JSONObject jsonObj, String solrHost) {
        //String solrHost = KConfiguration.getInstance().getConfiguration().getString("processingSolrHost")+"/update";
        String updateEndpoint = solrHost+"/update";
        JSONObject addOperation = new JSONObject();
        addOperation.put("add", jsonObj);
        WebResource r = this.client.resource(updateEndpoint);
        String string = addOperation.toString();
        String post = r.accept(MediaType.APPLICATION_JSON).entity(string, MediaType.APPLICATION_JSON).post(String.class);
        return new JSONObject(post);
    }

    public JSONObject deleteByPid(JSONObject jsonObj, String solrHost) {
        //String solrHost = KConfiguration.getInstance().getConfiguration().getString("processingSolrHost")+"/update";
        String updateEndpoint = solrHost+"/update";
        WebResource r = this.client.resource(updateEndpoint);
        String string = jsonObj.toString();
        String post = r.accept(MediaType.APPLICATION_JSON).entity(string, MediaType.APPLICATION_JSON).post(String.class);
        return new JSONObject(post);
    }

    
    public JSONObject deleteByPid(String pid, String solrHost) throws ClientProtocolException, IOException {
        JSONObject deleteObject = new JSONObject();
        JSONObject queryObject = new JSONObject();
        queryObject.put("query", "source:\""+pid+"\"");
        deleteObject.put("delete", queryObject);
        return deleteByPid(deleteObject, solrHost);
    }    

}
