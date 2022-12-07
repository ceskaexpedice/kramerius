package cz.incad.kramerius.services.iterators.solr;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import cz.incad.kramerius.services.ParallelProcessImpl;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.timestamps.TimestampStore;
import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;

import java.io.IOException;
import java.util.logging.Level;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.w3c.dom.Element;

public class SolrIteratorFactory extends ProcessIteratorFactory {
	
	public static final String DEFAULT_TIMESTAMP_FIELD = "indexed";
	
    public enum  TypeOfIteration {
        CURSOR, FILTER, PAGINATION;
    }

    @Override
    public ProcessIterator createProcessIterator(String timestampUrl,Element iteration, Client client) {
    	
        String masterQuery = "*:*";

        Element timstampElm = XMLUtils.findElement(iteration, "timestamp_field");
        String timestampField = timstampElm != null ? timstampElm.getTextContent() : "";

        Element urlElm = XMLUtils.findElement(iteration, "url");
        String url = urlElm != null ? urlElm.getTextContent() : "";

        Element fqueryElm = XMLUtils.findElement(iteration, "fquery");
        String filterQuery = fqueryElm != null ? fqueryElm.getTextContent() : "";
        try {
            if (timestampUrl != null) {
            	JSONObject timestamp = timestamp(client, timestampUrl);
            	if (timestamp != null &&  timestamp.has("date")) {
            		if (StringUtils.isAnyString(filterQuery)) {
            			filterQuery = filterQuery + "AND "+ timestampField+":["+timestamp.getString("date") + " TO NOW]";
            		} else {
            			filterQuery = timestampField+":["+timestamp.getString("date") + " TO NOW]";
            		}
            	}
            }
        } catch(UniformInterfaceException ex) {
        	LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
        }
        
        
        
        Element endpointElm = XMLUtils.findElement(iteration, "endpoint");
        String endpoint = endpointElm != null ? endpointElm.getTextContent() : "";

        Element idElm = XMLUtils.findElement(iteration, "id");
        String id = idElm != null ? idElm.getTextContent() : "";

        Element sortElm = XMLUtils.findElement(iteration, "sort");
        String sort  = sortElm != null ? sortElm.getTextContent() : (id != null ?  id +" ASC"  : "");

        Element rowsElm = XMLUtils.findElement(iteration, "rows");
        int rowSize = rowsElm != null ? Integer.parseInt(rowsElm.getTextContent()) : 100;

        
        Element typeElm = XMLUtils.findElement(iteration, "type");
        TypeOfIteration typeOfIteration = typeElm != null ? TypeOfIteration.valueOf(typeElm.getTextContent()) : TypeOfIteration.CURSOR;

        Element userElm = XMLUtils.findElement(iteration, "user");
        Element passElm = XMLUtils.findElement(iteration, "pass");
        
        if (userElm != null && passElm !=null) {
            String user = userElm.getTextContent();
            String pass = passElm.getTextContent();

            switch (typeOfIteration) {

                case CURSOR: return new SolrCursorIterator(  url, masterQuery, filterQuery, endpoint, id, sort,rowSize,user, pass);
                case FILTER: return new SolrFilterQueryIterator(url, masterQuery, filterQuery, endpoint, id, sort,rowSize, user, pass);
                case PAGINATION: return new SolrPageIterator( url, masterQuery, filterQuery, endpoint, id, sort,rowSize, user, pass);
            }

        } else {
            switch (typeOfIteration) {
                case CURSOR: return new SolrCursorIterator(url, masterQuery, filterQuery, endpoint, id, sort,rowSize);
                case FILTER: return new SolrFilterQueryIterator( url, masterQuery, filterQuery, endpoint, id, sort,rowSize);
                case PAGINATION: return new SolrPageIterator( url, masterQuery, filterQuery, endpoint, id, sort,rowSize);
            }
        }

        return null;
    }

	private JSONObject timestamp(Client client,String timestampUrl) {
        LOGGER.info(String.format("[" + Thread.currentThread().getName() + "] url %s", timestampUrl));
    	WebResource r = client.resource(timestampUrl);
    	ClientResponse clientResponse = r.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    	if (clientResponse.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
    		String t = clientResponse.getEntity(String.class);
    		return new JSONObject(t);
    	} else if (clientResponse.getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
    		return null;
    	} else {
            throw new UniformInterfaceException(clientResponse);
    	}
        
	}
}
