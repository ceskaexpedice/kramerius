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

	
	public String processes() throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:processes(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.lrprocesses.title")+" </a> </div>";
	}
	
	public String importMonographs() throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:importMonographs(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.importMonograph.title")+" </a> </div>";
	}

	public String importPeriodicals() throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:importPeriodicals(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.importPeriodical.title")+" </a> </div>";
	}

	public String showIndexerAdmin() throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:showIndexerAdmin(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.indexDocuments.title")+" </a> </div>";
	}

	public String noParamsProcess(String processName) throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:noParamsProcess('"+processName+"'); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs."+processName+".title")+" </a> </div>";
	}
	
	public String[] getAdminMenuItems() {
		try {
			List<String> menuItems = new ArrayList<String>();
			if (request.getRemoteUser() != null) {
				if (userInRoleDecision.isUserInRole(KrameriusRoles.KRAMERIUS_ADMIN.getRoleName())) {
					menuItems.add(processes());
				}
				if (userInRoleDecision.isUserInRole(KrameriusRoles.REPLIKATOR_MONOGRAPHS.getRoleName())) {
					menuItems.add(importMonographs());
				} 
				if (userInRoleDecision.isUserInRole(KrameriusRoles.REPLIKATOR_PERIODICALS.getRoleName())) {
					menuItems.add(importPeriodicals());
				}
				if (userInRoleDecision.isUserInRole(KrameriusRoles.REINDEX.getRoleName())) {
					menuItems.add(showIndexerAdmin());
				}
				if (userInRoleDecision.isUserInRole(KrameriusRoles.ENUMERATOR.getRoleName())) {
					menuItems.add(noParamsProcess(KrameriusRoles.ENUMERATOR.getRoleName()));
				}
				if (userInRoleDecision.isUserInRole(KrameriusRoles.REPLICATIONRIGHTS.getRoleName())) {
					menuItems.add(noParamsProcess(KrameriusRoles.REPLICATIONRIGHTS.getRoleName()));
				}
				if (userInRoleDecision.isUserInRole(KrameriusRoles.CONVERT.getRoleName())) {
					menuItems.add(noParamsProcess(KrameriusRoles.CONVERT.getRoleName()));
				} 
				if (userInRoleDecision.isUserInRole(KrameriusRoles.IMPORT.getRoleName())) {
					menuItems.add(noParamsProcess(KrameriusRoles.IMPORT.getRoleName()));
				}
			}
			return (String[]) menuItems.toArray(new String[menuItems.size()]);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return new String[0];
		}
	}
}
