package cz.inovatika.kramerius.services.iterators.factories;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.inovatika.kramerius.services.iterators.ProcessIterator;
import cz.inovatika.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.utils.StringUtils;

import java.util.logging.Level;

import javax.ws.rs.core.MediaType;

import cz.inovatika.kramerius.services.iterators.config.SolrIteratorConfig;
import cz.inovatika.kramerius.services.iterators.config.TypeOfIteration;
import cz.inovatika.kramerius.services.iterators.solr.SolrCursorIterator;
import cz.inovatika.kramerius.services.iterators.solr.SolrFilterQueryIterator;
import cz.inovatika.kramerius.services.iterators.solr.SolrPageIterator;
import org.json.JSONObject;

public class SolrIteratorFactory extends ProcessIteratorFactory {
	
	public static final String DEFAULT_TIMESTAMP_FIELD = "indexed";


    @Override
    public ProcessIterator createProcessIterator(SolrIteratorConfig config, Client client) {
        String masterQuery = config.getMasterQuery();

        String timestampField = config.getTimestampField();
        String url = config.getUrl();
        String filterQuery = config.getFilterQuery();
        try {
            if (config.getTimestampUrl() != null) {
                JSONObject timestamp = timestamp(client, config.getTimestampUrl());
                if (timestamp != null &&  timestamp.has("date")) {
                    if (StringUtils.isAnyString(filterQuery)) {
                        filterQuery = filterQuery + " AND "+ timestampField+":["+timestamp.getString("date") + " TO NOW]";
                    } else {
                        filterQuery = timestampField+":["+timestamp.getString("date") + " TO NOW]";
                    }
                }
            }
        } catch(UniformInterfaceException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
        }

        String[] fieldList = config.getFieldList();

        String endpoint = config.getEndpoint();
        String id = config.getIdField();
        String sort  = config.getSort();
        int rowSize = config.getRows();
        TypeOfIteration typeOfIteration = config.getTypeOfIteration();


        switch (typeOfIteration) {
            case CURSOR: return new SolrCursorIterator(url, masterQuery, filterQuery, endpoint, id, sort,rowSize, fieldList);
            case FILTER: return new SolrFilterQueryIterator( url, masterQuery, filterQuery, endpoint, id, sort,rowSize, fieldList);
            case PAGINATION: return new SolrPageIterator( url, masterQuery, filterQuery, endpoint, id, sort,rowSize,fieldList);
        }

        return null;
    }

//    @Override
//    public ProcessIterator createProcessIterator(String timestampUrl, Element iteration, Client client) {
//
//        String masterQuery = "*:*";
//
//        Element timstampElm = XMLUtils.findElement(iteration, "timestamp_field");
//        String timestampField = timstampElm != null ? timstampElm.getTextContent() : "";
//
//        Element urlElm = XMLUtils.findElement(iteration, "url");
//        String url = urlElm != null ? urlElm.getTextContent() : "";
//
//        Element fqueryElm = XMLUtils.findElement(iteration, "fquery");
//        String filterQuery = fqueryElm != null ? fqueryElm.getTextContent() : "";
//        try {
//            if (timestampUrl != null) {
//            	JSONObject timestamp = timestamp(client, timestampUrl);
//            	if (timestamp != null &&  timestamp.has("date")) {
//            		if (StringUtils.isAnyString(filterQuery)) {
//            			filterQuery = filterQuery + " AND "+ timestampField+":["+timestamp.getString("date") + " TO NOW]";
//            		} else {
//            			filterQuery = timestampField+":["+timestamp.getString("date") + " TO NOW]";
//            		}
//            	}
//            }
//        } catch(UniformInterfaceException ex) {
//        	LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
//        }
//
//        //<fieldlist>
//
//        Element fieldListElm = XMLUtils.findElement(iteration, "fieldlist");
//        String fieldList = fieldListElm != null ? fieldListElm.getTextContent() : "";
//
//
//        Element endpointElm = XMLUtils.findElement(iteration, "endpoint");
//        String endpoint = endpointElm != null ? endpointElm.getTextContent() : "";
//
//        Element idElm = XMLUtils.findElement(iteration, "id");
//        String id = idElm != null ? idElm.getTextContent() : "";
//
//        Element sortElm = XMLUtils.findElement(iteration, "sort");
//        String sort  = sortElm != null ? sortElm.getTextContent() : (id != null ?  id +" ASC"  : "");
//
//        Element rowsElm = XMLUtils.findElement(iteration, "rows");
//        int rowSize = rowsElm != null ? Integer.parseInt(rowsElm.getTextContent()) : 100;
//
//
//        Element typeElm = XMLUtils.findElement(iteration, "type");
//        TypeOfIteration typeOfIteration = typeElm != null ? TypeOfIteration.valueOf(typeElm.getTextContent()) : TypeOfIteration.CURSOR;
//
//
//        switch (typeOfIteration) {
//            case CURSOR: return new SolrCursorIterator(url, masterQuery, filterQuery, endpoint, id, sort,rowSize, StringUtils.isAnyString(fieldList) ?  fieldList.split(",") : new String[] {});
//            case FILTER: return new SolrFilterQueryIterator( url, masterQuery, filterQuery, endpoint, id, sort,rowSize, StringUtils.isAnyString(fieldList) ? fieldList.split(",") : new String[] {});
//            case PAGINATION: return new SolrPageIterator( url, masterQuery, filterQuery, endpoint, id, sort,rowSize,StringUtils.isAnyString(fieldList) ?  fieldList.split(","): new String[] {});
//        }
//
//        return null;
//    }

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
