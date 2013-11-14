package cz.incad.kramerius.rest.api.k5.client.item;

import java.util.HashMap;


public interface Extensions {

	public boolean isApplicable(String pid, HashMap<String, Object> options);
	
	public int getSortingKey();
}
