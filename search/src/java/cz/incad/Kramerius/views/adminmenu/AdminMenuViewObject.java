package cz.incad.Kramerius.views.adminmenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.NoEndPointException;

import com.google.inject.Inject;

import cz.incad.Kramerius.security.KrameriusRoles;
import cz.incad.kramerius.security.IsUserInRoleDecision;
import cz.incad.kramerius.security.Secured;
import cz.incad.kramerius.service.ResourceBundleService;

public class AdminMenuViewObject {

	private static final String IMPORT = "import";
	private static final String CONVERT = "convert";
	private static final String REPLICATIONRIGHTS = "replicationrights";
	private static final String ENUMERATOR = "enumerator";
	private static final String REINDEX = "reindex";
	private static final String REPLIKATOR_PERIODICALS = "replikator_periodicals";
	private static final String REPLIKATOR_MONOGRAPHS = "replikator_monographs";
	private static final String KRAMERIUS_ADMIN = "krameriusAdmin";

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(AdminMenuViewObject.class.getName());
	
	@Inject
	ResourceBundleService resourceBundleService;
	@Inject
	HttpServletRequest request;
	@Inject
	Locale locale;
	@Inject
	IsUserInRoleDecision userInRoleDecision;

	
	@Secured(roles=KrameriusRoles.LRPROCESS_ADMIN)
	public String processes() throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:processes(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.lrprocesses.title")+" </a> </div>";
	}
	
	@Secured(roles=REPLIKATOR_MONOGRAPHS)
	public String importMonographs() throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:importMonographs(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.importMonograph.title")+" </a> </div>";
	}

	@Secured(roles=REPLIKATOR_PERIODICALS)
	public String importPeriodicals() throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:importPeriodicals(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.importPeriodical.title")+" </a> </div>";
	}

	@Secured(roles=REINDEX)
	public String showIndexerAdmin() throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:showIndexerAdmin(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.indexDocuments.title")+" </a> </div>";
	}

	@Secured(roles={ENUMERATOR,REPLICATIONRIGHTS,CONVERT,IMPORT})
	public String noParamsProcess(String processName) throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:noParamsProcess('"+processName+"'); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs."+processName+".title")+" </a> </div>";
	}
	
	public String[] getAdminMenuItems() {
		try {
			List<String> menuItems = new ArrayList<String>();
			if (request.getRemoteUser() != null) {
				if (userInRoleDecision.isUserInRole(KRAMERIUS_ADMIN)) {
					menuItems.add(processes());
				}
				if (userInRoleDecision.isUserInRole(REPLIKATOR_MONOGRAPHS)) {
					menuItems.add(importMonographs());
				} 
				if (userInRoleDecision.isUserInRole(REPLIKATOR_PERIODICALS)) {
					menuItems.add(importPeriodicals());
				}
				if (userInRoleDecision.isUserInRole(REINDEX)) {
					menuItems.add(showIndexerAdmin());
				}
				if (userInRoleDecision.isUserInRole(ENUMERATOR)) {
					menuItems.add(noParamsProcess(ENUMERATOR));
				}
				if (userInRoleDecision.isUserInRole(REPLICATIONRIGHTS)) {
					menuItems.add(noParamsProcess(REPLICATIONRIGHTS));
				}
				if (userInRoleDecision.isUserInRole(CONVERT)) {
					menuItems.add(noParamsProcess(CONVERT));
				} 
				if (userInRoleDecision.isUserInRole(IMPORT)) {
					menuItems.add(noParamsProcess(IMPORT));
				}
			}
			return (String[]) menuItems.toArray(new String[menuItems.size()]);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return new String[0];
		}
	}
}
