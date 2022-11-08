package cz.incad.kramerius.services.iterators.timestamps.solr;

import java.net.URLEncoder;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.services.iterators.timestamps.TimestampStore;
import cz.incad.kramerius.services.iterators.timestamps.objects.TimestampType;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class SolrTimestampChecks implements TimestampStore{
	
	public SolrTimestampChecks(Element elm) {
		super();
//		Element urlElm = XMLUtils.findElement(elm, "url");
//		if (urlElm != null) {
//			this.url = urlElm.getTextContent();
//		}
//		
//		Element nameElm = XMLUtils.findElement(elm, "name");
//		if (nameElm != null) {
//			this.name = nameElm.getTextContent();
//		}
	}

	@Override
	public String retrieveTimestamp(Client cl, TimestampType type, String source) {
//        WebResource r = cl.resource(destSolr);
//        ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
        //if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {

		return null;
	}

	@Override
	public void storeTimestamp(Client cl, Date date, TimestampType type, String source) {
		// TODO Auto-generated method stub
		
	}
	
	
}
