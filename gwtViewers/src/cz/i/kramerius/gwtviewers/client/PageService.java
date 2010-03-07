package cz.i.kramerius.gwtviewers.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import cz.i.kramerius.gwtviewers.client.panels.utils.Dimension;

/**
 * Nastrel sluzby, ktera bude vracet stranky z objektu
 * @author pavels
 */
@RemoteServiceRelativePath("page")
public interface PageService extends RemoteService {

	/**
	 * Vraci pocet stranek objektu
	 * @param uuid
	 * @return
	 */
	public Integer getNumberOfPages(String uuid);
	
	/**
	 * Vraci vsechny stranky
	 * @param masterUuid
	 * @return
	 */
	public ArrayList<SimpleImageTO> getPagesSet(String masterUuid);

	//public Dimension getMaxDimension(String masterUuid);
	
	public SimpleImageTO getPage(String uuid, int index);
	
	public List<String> getPagesUUIDs(String masterUuid);
	
	public String getUUId(String masterUuid, int index);
	
	public SimpleImageTO getNoPage();
	
}
