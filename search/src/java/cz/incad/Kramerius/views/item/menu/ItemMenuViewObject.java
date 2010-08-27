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

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;

import cz.incad.Kramerius.FullImageServlet;
import cz.incad.Kramerius.I18NServlet;
import cz.incad.Kramerius.security.KrameriusRoles;
import cz.incad.Kramerius.views.item.ItemViewObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.security.IsUserInRoleDecision;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;

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
	IsUserInRoleDecision userInRoleDecision;

	Locale locale;
	String uuid;
	String model;
	int index;
	
	
	public ItemMenuViewObject(HttpServletRequest httpServletRequest,
			ServletContext servletContext, FedoraAccess fedoraAccess, ResourceBundle resourceBundle, KConfiguration kConfiguration,
			ItemViewObject itemViewObject,Locale locale, String uuid, String model ,int index, IsUserInRoleDecision userInRoleDecision) {
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
		this.userInRoleDecision = userInRoleDecision;
	}
	
	public boolean isPageModel() {
		KrameriusModels model = KrameriusModels.parseString(this.itemViewObject.getModels().get(this.index));
		return (model.equals(KrameriusModels.PAGE));
	}

	private String viewMetadataItem() {
		String key = "administrator.menu.showmetadata";
		String jsmethod = "showMainContent";
		return renderCommonItem(key, jsmethod);
	}


	private String persistentURL() {
		String key = "administrator.menu.persistenturl";
		String jsmethod="showPersistentURL";
		return renderCommonItem(key, jsmethod);
	}
	

	private String dynamicPDF() {
		String key = "administrator.menu.generatepdf";
        if (isPageModel()) {
            return renderCommonItem(key, "PDF.url");
		} else {
            return renderCommonItem(key, "PDF.generatePDF");
		}
	}
	private String exportPDF() {
        StringTemplate template = new StringTemplate("<div align=\"left\"><a title='$tooltip$' " +
        "href=\"javascript:generateStatic($level$,'static_export_CD','$imgServlet$','$i18nServlet$','$iso3Country$','$iso3Language$');\">$title$</a> </div>");
	    String imgServlet = FullImageServlet.fullImageServlet(this.httpServletRequest);
		String i18nServlet = I18NServlet.i18nServlet(this.httpServletRequest);
		template.setAttribute("level", (index+1));
		template.setAttribute("tooltip", this.resourceBundle.getString("administrator.menu.generatepdf"));
        template.setAttribute("title", this.resourceBundle.getString("administrator.menu.generatepdf"));
        template.setAttribute("imgServlet", imgServlet);
        template.setAttribute("i18nServlet", i18nServlet);
        template.setAttribute("iso3Country", this.locale.getISO3Country());
        template.setAttribute("iso3Language", this.locale.getISO3Language());
        return template.toString();
	}

	
	private String exportMETS() {
		String key = "administrator.menu.exportMETS";
		String jsmethod = "showMets";
		return renderCommonItem(key, jsmethod);
	}
	
	private String reindex() {
		String key = "administrator.menu.reindex";
		String jsmethod = "reindex";
		return renderCommonItem(key, jsmethod);
	}

	private String deleteFromIndex() {
	    String jsmethod = "deletefromindex";
		String key = "administrator.menu.deletefromindex";
		return renderCommonItem(key, jsmethod);
	}

	private String deleteFromFedora() {
	    String key = "administrator.menu.deleteuuid";
	    String jsmethod = "deleteUuid";
	    return renderCommonItem(key, jsmethod);
	}

	private String changeVisibility() {
	    String key = "administrator.menu.setpublic";
	    String jsmethod = "changeFlag";
        return renderCommonItem(key, jsmethod);
	}

	private String exportTOFOXML() {
        String key = "administrator.menu.exportFOXML";
        String jsmethod = "exportTOFOXML";
        return renderCommonItem(key, jsmethod);
	}

    private String renderCommonItem(String key, String jsmethod) {
        StringTemplate template = getCommonTemplate();
        jsmethod(template, jsmethod);
	    titleAndTooltip(template, key);
        levelAndModel(template);
        return template.toString();
    }

    private StringTemplate getCommonTemplate() {
        StringTemplate template = new StringTemplate("<div align=\"left\"><a title='$tooltip$' " +
	    		"href=\"javascript:$jsmethod$($level$,'$model$');\">$title$</a> </div>");
        return template;
    }

    private void jsmethod(StringTemplate template, String jsmethod) {
        template.setAttribute("jsmethod", jsmethod);
    }

    private void titleAndTooltip(StringTemplate template, String key) {
        template.setAttribute("tooltip", this.resourceBundle.getString(key));
        template.setAttribute("title", this.resourceBundle.getString(key));
    }

    private void levelAndModel(StringTemplate template) {
        template.setAttribute("model", this.itemViewObject.getModels().get(this.index));
        template.setAttribute("level", (index+1));
    }
    
    
    
	
	/**
	 * Create menu items
	 * @return
	 */
	public String[] getContextMenuItems() {
	    List<String> items = new ArrayList<String>();
	    items.addAll(getCommonMenuItems());
		items.addAll(getAdminMenuItems());
		return (String[]) items.toArray(new String[items.size()]);
	}

    private List<String> getCommonMenuItems() {
        List<String> items = new ArrayList<String>();
		{
			items.add(viewMetadataItem());
			items.add(persistentURL());
			try {
				if (fedoraAccess.isContentAccessible(uuid)) {
			        if (!bornDigital()) {
	                    items.add(dynamicPDF());
			        }
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
			items.add(exportMETS());
		};
        return items;
    }

    
    /**
     * Tests whether document is born digital document (only PDF at this moment) 
     * @return
     * @throws IOException
     */
    private boolean bornDigital() throws IOException {
        // born digital contains IMG_FULL stream and its mimetype is application/pdf
        boolean bornDigital = false;
        boolean imageFullContains = fedoraAccess.isImageFULLAvailable(uuid);
        if (imageFullContains) {
            String smimeType = fedoraAccess.getMimeTypeForStream("uuid:"+uuid, "IMG_FULL");
            ImageMimeType mimeType = ImageMimeType.loadFromMimeType(smimeType);
            if ((mimeType != null) && (mimeType == ImageMimeType.PDF)){
                bornDigital = true;
            }
        }
        return bornDigital;
    }

    public List<String> getAdminMenuItems() {
        List<String> items = new ArrayList<String>();
        if (httpServletRequest.getRemoteUser() != null) {
			if (userInRoleDecision.isUserInRole(KrameriusRoles.REINDEX)) {
				items.add(reindex());
			}
			if (userInRoleDecision.isUserInRole(KrameriusRoles.REINDEX)) {
				items.add(deleteFromIndex());
			}
			if (userInRoleDecision.isUserInRole(KrameriusRoles.DELETE)) {
				items.add(deleteFromFedora());
			}
			if ((userInRoleDecision.isUserInRole(KrameriusRoles.SETPUBLIC)) && 
				(userInRoleDecision.isUserInRole(KrameriusRoles.SETPRIVATE))) {
				items.add(changeVisibility());
			}
			if (userInRoleDecision.isUserInRole(KrameriusRoles.EXPORT)) {
				items.add(exportTOFOXML());
			}
		}
        return items;
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
