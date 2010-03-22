package cz.incad.kramerius.gwtviewers.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import cz.incad.kramerius.gwtviewers.client.panels.utils.Dimension;

/**
 * Nastrel sluzby, ktera bude vracet stranky z objektu
 * @author pavels
 */
@RemoteServiceRelativePath("page")
public interface PageService extends RemoteService {

	public Integer getNumberOfPages(String masterUuid, String selection);

	public PagesResultSet getPagesSet(String masterUuid, String selection);

	public SimpleImageTO getNoPage();
}
