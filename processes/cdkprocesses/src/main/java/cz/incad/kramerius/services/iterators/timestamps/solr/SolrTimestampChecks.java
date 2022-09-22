package cz.incad.kramerius.services.iterators.timestamps.solr;

import java.net.URLEncoder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.services.iterators.timestamps.TimestampStore;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class SolrTimestampChecks implements TimestampStore{
	
	private String url;
	private String name;
	
	public SolrTimestampChecks(Element elm) {
		super();
		Element urlElm = XMLUtils.findElement(elm, "url");
		if (urlElm != null) {
			this.url = urlElm.getTextContent();
		}
		
		Element nameElm = XMLUtils.findElement(elm, "name");
		if (nameElm != null) {
			this.name = nameElm.getTextContent();
		}
	}

	@Override
	public String retrieveTimestamp(Client cl) {
        //String query = this.url+ (this.url.endsWith("/") ? "":"/")+  "select?q="+;
//        if (StringUtils.isAnyString(fq)) {
//            fullQuery = "?q="+mq + (cursor!= null ? String.format("&rows=%d&cursorMark=%s", rows, cursor) : String.format("&rows=%d&cursorMark=*", rows))+"&sort=" + URLEncoder.encode(sorting, "UTF-8")+"&fl="+identifierField+"&fq=" + URLEncoder.encode(fq,"UTF-8");
//        } else {
//            fullQuery = "?q="+mq + (cursor!= null ? String.format("&rows=%d&cursorMark=%s", rows, cursor) : String.format("&rows=%d&cursorMark=*", rows))+"&sort=" + URLEncoder.encode(sorting, "UTF-8")+"&fl="+identifierField;
//        }
//        String query = endpoint + fullQuery+"&wt=xml";


//        String query = endpoint + fullQuery+"&wt=xml";
//        
//        return SolrUtils.executeQuery(client, url, query, user, pass);
		return null;
	}

	@Override
	public void storeTimestamp(Client cl, String timestamp) {
		
//        for (Document  batch : batchDocuments) {
//            String s = SolrUtils.sendToDest(this.destinationUrl, this.client, batch);
//            LOGGER.info("Response "+s);
//        }
		
	}
}
