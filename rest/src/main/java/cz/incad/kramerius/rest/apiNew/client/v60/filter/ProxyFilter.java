package cz.incad.kramerius.rest.apiNew.client.v60.filter;

import java.util.List;

import org.json.JSONObject;
import org.w3c.dom.Element;

public interface ProxyFilter {
	
	public String newFilter();
	
	public String enhancedFilter(String f);
	
	public void filterValue(Element rawDoc);
	
	public void filterValue(JSONObject rawDoc);
}
