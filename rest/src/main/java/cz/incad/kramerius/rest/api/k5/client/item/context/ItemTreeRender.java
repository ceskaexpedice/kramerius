package cz.incad.kramerius.rest.api.k5.client.item.context;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import cz.incad.kramerius.rest.api.k5.client.item.Extensions;

public interface ItemTreeRender extends Extensions {

	public JSON tree(String pid);
}
