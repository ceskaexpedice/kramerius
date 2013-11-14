package cz.incad.kramerius.rest.api.k5.client.item.metadata;

import cz.incad.kramerius.rest.api.k5.client.item.Extensions;
import net.sf.json.JSONObject;

public interface Metadata extends Extensions {
	
	public JSONObject collect(String pid);

}
