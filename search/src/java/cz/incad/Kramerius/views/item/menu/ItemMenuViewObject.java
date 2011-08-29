package cz.incad.Kramerius.views.item.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.Kramerius.FullImageServlet;
import cz.incad.Kramerius.I18NServlet;
import cz.incad.Kramerius.views.item.ItemViewObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;

/**
 * View helper object for creating one context menu
 * TODO: Remove
 * @author pavels
 */
@Deprecated
public class ItemMenuViewObject {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ItemMenuViewObject.class.getName());

    private HttpServletRequest httpServletRequest;
    private ServletContext servletContext;
    private FedoraAccess fedoraAccess;
    private UserManager userManager;
    
    private ResourceBundle resourceBundle;
    private KConfiguration kConfiguration;
    private ItemViewObject itemViewObject;
    private Locale locale;
    private String uuid;
    private String model;
    private int index;
    
    public ItemMenuViewObject(HttpServletRequest httpServletRequest,
            ServletContext servletContext, FedoraAccess fedoraAccess, UserManager userManager, ResourceBundle resourceBundle, KConfiguration kConfiguration,
            ItemViewObject itemViewObject, Locale locale, String uuid, String model, int index) {
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
        this.userManager = userManager;
    }

    public boolean isDisplayable() {
        try {
            return this.fedoraAccess.isImageFULLAvailable(uuid);
        }catch(Exception e){
            LOGGER.log(Level.INFO, e.getMessage(), e);
            return false;
        }
    }

    private String viewMetadataItem() {
        String key = "administrator.menu.showmetadata";
        String jsmethod = "showMetadata";
        return renderCommonItem(key, "none", "", jsmethod);
    }

    private String persistentURL() {
        String key = "administrator.menu.persistenturl";
        String jsmethod = "showPersistentURL";
        return renderCommonItem(key, "none", "", jsmethod);
    }

    private String dynamicPDF() {
        String key = "administrator.menu.generatepdf";
        if (isDisplayable()) {
            return renderCommonItem(key, "_data_x_role", "read", "printOnePage");
        } else {
            return renderCommonItem(key, "_data_x_role", "read", "printMorePages");
        }
    }

    private String downloadOriginal() throws IOException {
        String key = "administrator.menu.downloadOriginal";
        if ((isDisplayable()) || (bornDigital())) {
            return renderCommonItem(key, "_data_x_role", "read", "downloadOriginal");
        } else {
            return null;
        }
    }

    private String exportCD_PDF() {
        StringTemplate template = new StringTemplate("<div align=\"left\"><a title='$tooltip$' "
                + "href=\"javascript:generateStatic($level$,'static_export_CD','$imgServlet$','$i18nServlet$','$iso3Country$','$iso3Language$');\">$title$</a> <div style=\"display:none\">$role$</div></div>");
        String imgServlet = cz.incad.kramerius.utils.ApplicationURL.urlOfPath(this.httpServletRequest, "img");

        String i18nServlet = I18NServlet.i18nServlet(this.httpServletRequest);
        template.setAttribute("level", (index + 1));
        template.setAttribute("tooltip", this.resourceBundle.getString("administrator.menu.exportcd"));
        template.setAttribute("title", this.resourceBundle.getString("administrator.menu.exportcd"));
        template.setAttribute("imgServlet", imgServlet);
        template.setAttribute("i18nServlet", i18nServlet);
        template.setAttribute("iso3Country", this.locale.getISO3Country());
        template.setAttribute("iso3Language", this.locale.getISO3Language());
        return template.toString();
    }

    private String exportDVD_PDF() {
        StringTemplate template = new StringTemplate("<div align=\"left\"><a title='$tooltip$' "
                + "href=\"javascript:generateStatic($level$,'static_export_DVD','$imgServlet$','$i18nServlet$','$iso3Country$','$iso3Language$');\">$title$</a> <div style=\"display:none\">$role$</div></div>");
        String imgServlet = FullImageServlet.fullImageServlet(this.httpServletRequest);
        String i18nServlet = I18NServlet.i18nServlet(this.httpServletRequest);
        template.setAttribute("level", (index + 1));
        template.setAttribute("tooltip", this.resourceBundle.getString("administrator.menu.exportdvd"));
        template.setAttribute("title", this.resourceBundle.getString("administrator.menu.exportdvd"));
        template.setAttribute("imgServlet", imgServlet);
        template.setAttribute("i18nServlet", i18nServlet);
        template.setAttribute("iso3Country", this.locale.getISO3Country());
        template.setAttribute("iso3Language", this.locale.getISO3Language());
        return template.toString();
    }

    private String exportMETS() {
        String key = "administrator.menu.exportMETS";
        String jsmethod = "showMets";
        return renderCommonItem(key, "none", "", jsmethod);
    }

    private String reindex() {
        String key = "administrator.menu.reindex";
        String jsmethod = "reindex";
        return renderCommonItem(key, "_data_x_role", "reindex", jsmethod);
    }

    private String deleteFromIndex() {
        String jsmethod = "deletefromindex";
        String key = "administrator.menu.deletefromindex";
        return renderCommonItem(key, "_data_x_role", "reindex", jsmethod);
    }

    private String generateDeepZoomTiles() {
        String jsmethod = "generateDeepZoomTiles";
        String key = "administrator.menu.generateDeepZoomTiles";
        return renderCommonItem(key, "_data_x_role", "reindex", jsmethod);
    }

    private String deleteFromFedora() {
        String key = "administrator.menu.deleteuuid";
        String jsmethod = "deleteUuid";
        return renderCommonItem(key, "_data_x_role", "delete", jsmethod);
    }

    private String changeVisibility() {
        String key = "administrator.menu.setpublic";
        String jsmethod = "changeFlag";
        return renderCommonItem(key, "_data_x_role", "setprivate", jsmethod);
    }

    private String exportTOFOXML() {
        String key = "administrator.menu.exportFOXML";
        String jsmethod = "exportTOFOXML";
        return renderCommonItem(key, "_data_x_role", "export", jsmethod);
    }

    private String showRights() {
        String key = "administrator.menu.showrights";
        //String jsmethod = "adminRights";
        //rightsForRepository('uuid',actions
        StringTemplate template = new StringTemplate("<div align=\"left\"><a title='$tooltip$' "
                + "href=\"javascript:securedActionsObject.securedActionsTableForCtxMenu($level$,'$uuid$','$actions;separator=\",\"$');\">$title$</a> "
                + "<div class=\"$datatype$\" style=\"display:none\">$value$</div>"
                + "<div class=\"_data_x_level\" style=\"display:none\">$level$</div>"
                + "<div class=\"_data_x_uuid\" style=\"display:none\">$uuid$</div></div>");

        
        titleAndTooltip(template, key);
        levelAndModel(template);
        template.setAttribute("datatype", "_data_x_role");
        template.setAttribute("value", "administrate");
        template.setAttribute("uuid", uuid);
        template.setAttribute("actions", new String[] {
                SecuredActions.READ.getFormalName(), SecuredActions.ADMINISTRATE.getFormalName(),
                SecuredActions.EXPORT_K4_REPLICATIONS.getFormalName(),
                SecuredActions.IMPORT_K4_REPLICATIONS.getFormalName(),
        });
        
        return template.toString();
    }

    
    
    private String renderCommonItem(String key, String dataType, String value, String jsmethod) {
        StringTemplate template = getCommonTemplate();
        jsmethod(template, jsmethod);
        titleAndTooltip(template, key);
        levelAndModel(template);
        template.setAttribute("datatype", dataType);
        template.setAttribute("value", value);
        template.setAttribute("uuid", this.uuid);
        return template.toString();
    }

    private StringTemplate getCommonTemplate() {
        StringTemplate template = new StringTemplate("<div align=\"left\"><a title='$tooltip$' "
                + "href=\"javascript:$jsmethod$($level$,'$model$');\">$title$</a> "
                + "<div class=\"$datatype$\" style=\"display:none\">$value$</div>"
                + "<div class=\"_data_x_uuid\" style=\"display:none\">$uuid$</div></div>");
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
        template.setAttribute("level", (index + 1));
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
        items.add(viewMetadataItem());
        items.add(persistentURL());

        try {
            if (!bornDigital()) {
                items.add(dynamicPDF());
            }
            if ((isDisplayable()) || (bornDigital())) {
                items.add(downloadOriginal());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        items.add(exportMETS());
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
            String smimeType = fedoraAccess.getMimeTypeForStream("uuid:" + uuid, "IMG_FULL");
            ImageMimeType mimeType = ImageMimeType.loadFromMimeType(smimeType);
            if ((mimeType != null) && (mimeType == ImageMimeType.PDF)) {
                bornDigital = true;
            }
        }
        return bornDigital;
    }

    public List<String> getAdminMenuItems() {
        List<String> items = new ArrayList<String>();
        if (httpServletRequest.getRemoteUser() != null) {
            items.add(reindex());
            items.add(deleteFromIndex());
            items.add(deleteFromFedora());
            items.add(changeVisibility());
            items.add(exportTOFOXML());
            items.add(exportCD_PDF());
            items.add(exportDVD_PDF());
            items.add(generateDeepZoomTiles());
            items.add(deleteGenerateDeepZoomTiles());
            items.add(showRights());

            if (!isDisplayable()) {
                items.add(editor());
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

    private String deleteGenerateDeepZoomTiles() {
        String jsmethod = "deleteGeneratedDeepZoomTiles";
        String key = "administrator.menu.deleteGeneratedDeepZoomTiles";
        return renderCommonItem(key, "_data_x_role", "reindex", jsmethod);
    }
    private String editor() {
        String key = "administrator.menu.editor";
        String editor = kConfiguration.getEditorURL() + "?pids=uuid:" + uuid
                + "&locale=" + locale.getLanguage();
        StringTemplate template = new StringTemplate("<div align=\"left\"><a title='$tooltip$' "
                + "href=\"$editor$\"  target=\"_blank\">$title$</a> </div>");
        template.setAttribute("editor", editor);
        titleAndTooltip(template, key);

        return template.toString();
    }

    // TODO: Presunout jinam
    public String getBiblioModsURL() {
        //<c:out value="${kconfig.fedoraHost}" />/get/uuid:<c:out value="${uuid}" />/BIBLIO_MODS
        return this.kConfiguration.getFedoraHost() + "/get/uuid:" + this.uuid + "/BIBLIO_MODS";
    }

    public String getUpdateSelection() {
        StringTemplate template = new StringTemplate("setSelection($level$,'$model$','$uuid$');");
        template.setAttribute("level", (index + 1));
        template.setAttribute("model", model);
        template.setAttribute("uuid", uuid);
        return template.toString();
    }
}
