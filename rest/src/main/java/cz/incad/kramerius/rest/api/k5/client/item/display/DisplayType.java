package cz.incad.kramerius.rest.api.k5.client.item.display;

import java.util.HashMap;

import net.sf.json.JSONObject;
import cz.incad.kramerius.rest.api.k5.client.item.Extensions;

public interface DisplayType extends Extensions {

	public JSONObject getDisplay(String pid, HashMap<String, Object> options);
	
}
