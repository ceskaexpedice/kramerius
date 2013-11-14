package cz.incad.kramerius.rest.api.k5.client.item.metadata;

import java.util.HashMap;

import net.sf.json.JSONObject;

public class DefaultMetadataImpl implements Metadata {

	public static int SORTING_KEY = 10000;

	@Override
	public JSONObject collect(String pid) {
		JSONObject metadataGen = new JSONObject();
		metadataGen.put("metadatagenereated", true);
		return metadataGen;
	}

	@Override
	public boolean isApplicable(String pid, HashMap<String, Object> options) {
		return true;
	}

	@Override
	public int getSortingKey() {
		return SORTING_KEY;
	}
}
