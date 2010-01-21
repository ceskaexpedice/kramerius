package cz.i.kramerius.gwtviewers.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PageServiceAsync {

	void getPage(String uuid, int index, AsyncCallback<SimpleImageTO> callback);
	
	void  getPagesUUIDs(String masterUuid, AsyncCallback<List<String>> callback);
	
	void getUUId(String masterUuid, int index, AsyncCallback<String> callback);
	
	void getNumberOfPages(String uuid, AsyncCallback<Integer> callback);

	void getNoPage(AsyncCallback<SimpleImageTO> callback);

	void getPagesSet(String masterUuid,
			AsyncCallback<ArrayList<SimpleImageTO>> callback);

}
