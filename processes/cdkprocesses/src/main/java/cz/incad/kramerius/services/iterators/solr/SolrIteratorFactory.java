package cz.incad.kramerius.services.iterators.solr;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.ParallelProcessImpl;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.ProcessIteratorFactory;
import cz.incad.kramerius.services.iterators.timestamps.TimestampStore;
import cz.incad.kramerius.services.iterators.timestamps.solr.SolrTimestampChecks;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;

public class SolrIteratorFactory extends ProcessIteratorFactory {

    public enum  TypeOfIteration {
        CURSOR, FILTER, PAGINATION;
    }

    @Override
    public ProcessIterator createProcessIterator(Element iteration, Client client) {

    	TimestampStore timeStampStore = null;
    	
    	
        String masterQuery = "*:*";

        Element urlElm = XMLUtils.findElement(iteration, "url");
        String url = urlElm != null ? urlElm.getTextContent() : "";

        Element fqueryElm = XMLUtils.findElement(iteration, "fquery");
        String filterQuery = fqueryElm != null ? fqueryElm.getTextContent() : "";

        
        Element endpointElm = XMLUtils.findElement(iteration, "endpoint");
        String endpoint = endpointElm != null ? endpointElm.getTextContent() : "";

        Element idElm = XMLUtils.findElement(iteration, "id");
        String id = idElm != null ? idElm.getTextContent() : "";

        Element sortElm = XMLUtils.findElement(iteration, "sort");
        String sort  = sortElm != null ? sortElm.getTextContent() : (id != null ?  id +" ASC"  : "");

        Element rowsElm = XMLUtils.findElement(iteration, "rows");
        int rowSize = rowsElm != null ? Integer.parseInt(rowsElm.getTextContent()) : 100;
        
        Element timestamp = XMLUtils.findElement(iteration, "timestamp");
        if (timestamp != null) {
        	timeStampStore = new SolrTimestampChecks(timestamp);
        }
        
        
        Element typeElm = XMLUtils.findElement(iteration, "type");
        TypeOfIteration typeOfIteration = typeElm != null ? TypeOfIteration.valueOf(typeElm.getTextContent()) : TypeOfIteration.CURSOR;

        Element userElm = XMLUtils.findElement(iteration, "user");
        Element passElm = XMLUtils.findElement(iteration, "pass");
        
        if (userElm != null && passElm !=null) {
            String user = userElm.getTextContent();
            String pass = passElm.getTextContent();

            switch (typeOfIteration) {

                case CURSOR: return new SolrCursorIterator(timeStampStore,  url, masterQuery, filterQuery, endpoint, id, sort,rowSize,user, pass);
                case FILTER: return new SolrFilterQueryIterator(timeStampStore, url, masterQuery, filterQuery, endpoint, id, sort,rowSize, user, pass);
                case PAGINATION: return new SolrPageIterator(timeStampStore, url, masterQuery, filterQuery, endpoint, id, sort,rowSize, user, pass);
            }

        } else {
            switch (typeOfIteration) {
                case CURSOR: return new SolrCursorIterator(timeStampStore, url, masterQuery, filterQuery, endpoint, id, sort,rowSize);
                case FILTER: return new SolrFilterQueryIterator(timeStampStore, url, masterQuery, filterQuery, endpoint, id, sort,rowSize);
                case PAGINATION: return new SolrPageIterator(timeStampStore, url, masterQuery, filterQuery, endpoint, id, sort,rowSize);
            }
        }

        return null;
    }
}
