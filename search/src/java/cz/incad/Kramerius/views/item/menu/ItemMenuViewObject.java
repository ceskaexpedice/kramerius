package cz.incad.Kramerius.views.item.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.google.inject.Inject;

import cz.incad.Kramerius.FullImageServlet;
import cz.incad.Kramerius.I18NServlet;
import cz.incad.Kramerius.views.item.ItemViewObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * View helper object for creating one context menu
 * @author pavels
 */
public class ItemMenuViewObject {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ItemMenuViewObject.class.getName());
	
	HttpServletRequest httpServletRequest;
	ServletContext servletContext;
	FedoraAccess fedoraAccess;
	ResourceBundle resourceBundle;
	KConfiguration kConfiguration;
	ItemViewObject itemViewObject;
	Locale locale;
	String uuid;
	String model;
	int index;
	
	
	public ItemMenuViewObject(HttpServletRequest httpServletRequest,
			ServletContext servletContext, FedoraAccess fedoraAccess, ResourceBundle resourceBundle, KConfiguration kConfiguration,
			ItemViewObject itemViewObject,Locale locale, String uuid, String model ,int index) {
		super();
		this.httpServletRequest = httpServletRequest;
		this.servletContext = servletContext;
		this.fedoraAccess = fedoraAccess;
		this.resourceBundle = resourceBundle;
		this.kConfiguration = kConfiguration;
		this.itemViewObject = itemViewObject;
		this.locale = locale;
		this.uuid = uuid;
		this.index = index;
		this.model = model;
	}
	
	public boolean isPageModel() {
		KrameriusModels model = KrameriusModels.parseString(this.itemViewObject.getModels().get(this.index));
		return (model.equals(KrameriusModels.PAGE));
	}

	private String viewMetadataItem() {
		return "<div align=\"left\"><a title=\"View metadata\" href=\"javascript:showMainContent("+(index+1)+", '"+this.itemViewObject.getModels().get(this.index)+"');\">"+this.resourceBundle.getString("administrator.menu.showmetadata")+"</a> </div>";
	}


	private String persistentURL() {
		return "<div align=\"left\"><a title=\"Persistent url\" href=\"javascript:showPersistentURL("+(index+1)+", '"+this.itemViewObject.getModels().get(this.index)+"');\">"+this.resourceBundle.getString("administrator.menu.persistenturl")+"</a> </div>";
	}

	private String dynamicPDF() {
		if (isPageModel()) {
			return "<div align=\"left\"><a title=\"Generate pdf\" href=\"javascript:PDF.url("+(index+1)+");\">"+this.resourceBundle.getString("administrator.menu.generatepdf")+"</a> </div>";
		} else {
			return "<div align=\"left\"><a title=\"Generate pdf\" href=\"javascript:PDF.generatePDF("+(index+1)+", '"+this.itemViewObject.getModels().get(this.index)+"');\">"+this.resourceBundle.getString("administrator.menu.generatepdf")+"</a> </div>";
		}
	}
	private String exportPDF() {
		String imgServlet = FullImageServlet.fullImageServlet(this.httpServletRequest);
		String i18nServlet = I18NServlet.i18nServlet(this.httpServletRequest);
		return "<div align=\"left\"><a title=\"Export to pdf(CD)\" href=\"javascript:generateStatic("+(index+1)+",'static_export_CD','"+imgServlet+"','"+i18nServlet+"','"+this.locale.getISO3Country()+"','"+this.locale.getISO3Language()+");\">"+this.resourceBundle.getString("administrator.menu.generatepdf")+"</a> </div>";
	}

	private String reindex() {
//		<div align="left"><a title="Reindexace" href="javascript:reindex('<c:out value="${status.count}"/>','<c:out value="${models[status.count -1]}"/>');"><fmt:message bundle="${lctx}">administrator.menu.reindex</fmt:message></a></div>
		return "<div align=\"left\"><a title=\"Reindex\" href=\"javascript:reindex('"+index+1+","+this.itemViewObject.getModels().get(this.index)+"');\">"+this.resourceBundle.getString("administrator.menu.reindex")+"</a> </div>";
	}

	private String deleteFromIndex() {
	//  <div align="left"><a title="Delete from index" href="javascript:deletefromindex('<c:out value="${status.count}"/>');"><fmt:message bundle="${lctx}">administrator.menu.deletefromindex</fmt:message></a></div>
		return "<div align=\"left\"><a title=\"Delete from index\" href=\"javascript:deletefromindex("+(index+1)+");\">"+this.resourceBundle.getString("administrator.menu.generatepdf")+"</a> </div>";
	}

	private String deleteFromFedora() {
		//  <div align="left"><a title="Delete from index" href="javascript:deletefromindex('<c:out value="${status.count}"/>');"><fmt:message bundle="${lctx}">administrator.menu.deletefromindex</fmt:message></a></div>
			return "<div align=\"left\"><a title=\"Delete from fedora\" href=\"javascript:deleteUuid("+(index+1)+");\">"+this.resourceBundle.getString("administrator.menu.deleteuuid")+"</a> </div>";
	}

	private String changeVisibility() {
		//  <div align="left"><a title="Delete from index" href="javascript:deletefromindex('<c:out value="${status.count}"/>');"><fmt:message bundle="${lctx}">administrator.menu.deletefromindex</fmt:message></a></div>
			return "<div align=\"left\"><a title=\"Change visibility\" href=\"javascript:changeFlag('"+index+1+","+this.itemViewObject.getModels().get(this.index)+"');\">"+this.resourceBundle.getString("administrator.menu.setpublic")+"</a> </div>";
	}

	private String exportTOFOXML() {
		//  <div align="left"><a title="Delete from index" href="javascript:deletefromindex('<c:out value="${status.count}"/>');"><fmt:message bundle="${lctx}">administrator.menu.deletefromindex</fmt:message></a></div>
			return "<div align=\"left\"><a title=\"Export TO FOXML\" href=\"javascript:exportTOFOXML('"+index+1+","+this.itemViewObject.getModels().get(this.index)+"');\">"+this.resourceBundle.getString("administrator.menu.exportFOXML")+"</a> </div>";
	}

	/**
	 * Create menu items
	 * @return
	 */
	public String[] getContextMenuItems() {
		List<String> items = new ArrayList<String>();
		{
			items.add(viewMetadataItem());
			items.add(persistentURL());
			try {
				if (fedoraAccess.isContentAccessible(uuid)) {
					items.add(dynamicPDF());
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		};
		if (httpServletRequest.getRemoteUser() != null) {
			try {
				if (fedoraAccess.isContentAccessible(uuid)) {
					items.add(exportPDF());
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
			items.add(reindex());
			items.add(deleteFromIndex());
			items.add(deleteFromFedora());
			items.add(changeVisibility());
			items.add(exportTOFOXML());
		}
		return (String[]) items.toArray(new String[items.size()]);
	}
	
	public String getUuid() {
		return this.uuid;
	}
	
	public String getModel() {
		return this.model;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	
	// TODO: Presunout jinam
	public String getBiblioModsURL() {
        //<c:out value="${kconfig.fedoraHost}" />/get/uuid:<c:out value="${uuid}" />/BIBLIO_MODS
		return this.kConfiguration.getFedoraHost()+"/get/uuid:"+this.uuid+"/BIBLIO_MODS";
	}

}
