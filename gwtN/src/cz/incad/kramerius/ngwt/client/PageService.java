package cz.incad.kramerius.ngwt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


/**
 * Nastrel sluzby, ktera bude vracet stranky z objektu
 * @author pavels
 */
@RemoteServiceRelativePath("page")
public interface PageService extends RemoteService {

	public Integer getNumberOfPages(String masterUuid, String selection);

	public PagesResultSet getPagesSet(String masterUuid, String selection);

	public ImageMetadata getNoPage();
}
