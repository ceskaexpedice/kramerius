package cz.incad.Kramerius.views.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;


import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.views.item.menu.ItemMenuViewObject;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ItemViewObject {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ItemViewObject.class.getName());
	
	@Inject
	ServletContext servletContext;
	@Inject
	MostDesirable mostDesirable;
	@Inject
	HttpServletRequest request;
	@Inject
	Provider<Locale> localeProvider;
	@Inject
	ResourceBundleService resourceBundleService;
	
	
	protected Lock reentrantLock = new ReentrantLock();
	protected List<String> modifiedPids;
	protected List<String> modifiedModels;
	
	
	public ItemViewObject() {
		super();
	}
	
	public void saveMostDesirable() {
		String pidPath = request.getParameter("pid_path");
		StringTokenizer tokenizer = new StringTokenizer(pidPath,"/");
		if (tokenizer.hasMoreTokens()) {
			this.mostDesirable.saveAccess(tokenizer.nextToken(), new Date());	
		}
	}

	public String getFirstPageImageUrl() {
        return "djvu?uuid="+getLastUUID()+"&amp;scaledWidth=650";
	}

	public String getImagePid() {
		return getLastUUID();
//		List<String> pids = getPids();
//		String imagePid = pids.get(pids.size()-1);
//        return imagePid;
	}

	public String getFirstUUID() {
		return (getPids().isEmpty() ? null: getPids().get(0));
	}

	public String getParentUUID() {
		List<String> pids = getPids();
		if (pids.size() >= 2) {
			return pids.get(pids.size() -2);
		} else return null;
	}
	
	public String getLastUUID() {
		return (getPids().isEmpty() ? null: getPids().get(getPids().size()-1));
	}
	public String[] getModelsPath() {
		String[] models = request.getParameter("path").split("/");
		return models;
	}

	public String[] getPidPath() {
		String[] pids = request.getParameter("pid_path").split("/");
		return pids;
	}
	

	public List<String> getPids() {
		modifiedModelsPids();
        return modifiedPids;
	}

	private void modifiedModelsPids() {
		try {
			this.reentrantLock.lock();
			if ((this.modifiedPids == null) || (modifiedModels == null))  {
				modifiedPids =  new ArrayList<String>(Arrays.asList(getPidPath()));
		        modifiedModels = new ArrayList<String>(Arrays.asList(getModelsPath()));
		        FedoraUtils.fillFirstPagePid((ArrayList<String>)modifiedPids, (ArrayList<String>)modifiedModels);
			}
		} finally {
			this.reentrantLock.unlock();
		}
	}
	
	public List<String> getModels() {
		modifiedModelsPids();
		return this.modifiedModels;
	}
	
	
	public List<ItemMenuViewObject> getMenus() {
		try {
			List<String> pids = getPids();
			List<String> models = getModels();
			List<ItemMenuViewObject> menus = new ArrayList<ItemMenuViewObject>();
			for (int i = 0; i < pids.size(); i++) {
				menus.add(new ItemMenuViewObject(this.request, this.servletContext, this.resourceBundleService.getResourceBundle("labels", localeProvider.get()), KConfiguration.getInstance(), this, localeProvider.get(),pids.get(i), models.get(i), i));
			}
			return menus;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return new ArrayList<ItemMenuViewObject>();
		}
	}
	
	public int getLevel() {
		String parameter = this.request.getParameter("level");
		if ((parameter==null) || (parameter.equals(""))) return 0;
		return Integer.parseInt(parameter);
	}
	
}
