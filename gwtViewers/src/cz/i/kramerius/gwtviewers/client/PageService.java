package cz.i.kramerius.gwtviewers.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Nastrel sluzby, ktera bude vracet stranky z objektu
 * @author pavels
 */
@RemoteServiceRelativePath("page")
public interface PageService extends RemoteService {

	public Integer getNumberOfPages(String uuid);
	
	public SimpleImageTO getPage(String uuid, int index);
	
	public List<String> getPagesUUIDs(String masterUuid);
	
	public String getUUId(String masterUuid, int index);
	
	public SimpleImageTO getNoPage();
	
	public ArrayList<SimpleImageTO> getPagesSet(String masterUuid);
}
