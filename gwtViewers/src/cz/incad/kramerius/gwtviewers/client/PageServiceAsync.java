package cz.incad.kramerius.gwtviewers.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import cz.incad.kramerius.gwtviewers.client.panels.utils.Dimension;

public interface PageServiceAsync {

	void getNumberOfPages(String uuid, AsyncCallback<Integer> callback);

	void getNoPage(AsyncCallback<SimpleImageTO> callback);

	void getPagesSet(String masterUuid,
			AsyncCallback<PagesResultSet> callback);

}
