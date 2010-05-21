package cz.incad.kramerius.ngwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface PageServiceAsync {

	void getNoPage(AsyncCallback<ImageMetadata> callback);

	void getNumberOfPages(String masterUuid, String selection,
			AsyncCallback<Integer> callback);

	void getPagesSet(String masterUuid, String selection,
			AsyncCallback<PagesResultSet> callback);

}
