package cz.incad.Kramerius.views.adminmenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;


import com.google.inject.Inject;

import cz.incad.Kramerius.security.KrameriusRoles;
import cz.incad.kramerius.security.IsUserInRoleDecision;
import cz.incad.kramerius.service.ResourceBundleService;

public class AdminMenuViewObject {

    public static final Logger LOGGER = Logger.getLogger(AdminMenuViewObject.class.getName());
    @Inject
    ResourceBundleService resourceBundleService;
    @Inject
    HttpServletRequest request;
    @Inject
    Locale locale;
    @Inject
    IsUserInRoleDecision userInRoleDecision;

    public String processes() throws IOException {
        return renderMenuItem(
                "javascript:processes(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.lrprocesses.title");
    }

    public String importMonographs() throws IOException {
        return renderMenuItem(
                "javascript:importMonographs(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.importMonograph.title");
    }

    public String importPeriodicals() throws IOException {
        return renderMenuItem(
                "javascript:importPeriodicals(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.importPeriodical.title");
    }

    public String showIndexerAdmin() throws IOException {
        return renderMenuItem(
                "javascript:showIndexerAdmin(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.indexDocuments.title");
    }

    public String noParamsProcess(String processName) throws IOException {
        return renderMenuItem(
                "javascript:noParamsProcess('" + processName + "'); javascript:hideAdminMenu();",
                "administrator.menu.dialogs." + processName + ".title");
    }

    private String renderMenuItem(String href, String labelKey) throws IOException {
        String label = this.resourceBundleService.getResourceBundle("labels", this.locale).getString(labelKey);
        return String.format("<div align=\"left\"> <a href=\"%s\"> %s </a> </div>",
                href, label);
    }

    public String[] getAdminMenuItems() {
        try {
            List<String> menuItems = new ArrayList<String>();
            if (request.getRemoteUser() != null) {
                if (isUserRole(KrameriusRoles.KRAMERIUS_ADMIN)) {
                    menuItems.add(processes());
                }
                if (isUserRole(KrameriusRoles.REPLIKATOR_MONOGRAPHS)) {
                    menuItems.add(importMonographs());
                }
                if (isUserRole(KrameriusRoles.REPLIKATOR_PERIODICALS)) {
                    menuItems.add(importPeriodicals());
                }
                if (isUserRole(KrameriusRoles.REINDEX)) {
                    menuItems.add(showIndexerAdmin());
                }
                if (isUserRole(KrameriusRoles.ENUMERATOR)) {
                    menuItems.add(noParamsProcess(KrameriusRoles.ENUMERATOR.getRoleName()));
                }
                if (isUserRole(KrameriusRoles.REPLICATIONRIGHTS)) {
                    menuItems.add(noParamsProcess(KrameriusRoles.REPLICATIONRIGHTS.getRoleName()));
                }
                if (isUserRole(KrameriusRoles.CONVERT)) {
                    menuItems.add(noParamsProcess(KrameriusRoles.CONVERT.getRoleName()));
                }
                if (isUserRole(KrameriusRoles.IMPORT)) {
                    menuItems.add(noParamsProcess(KrameriusRoles.IMPORT.getRoleName()));
                }
            }
            return menuItems.toArray(new String[menuItems.size()]);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return new String[0];
        }
    }

    private boolean isUserRole(KrameriusRoles role) {
        return userInRoleDecision.isUserInRole(role.getRoleName());
    }
}
