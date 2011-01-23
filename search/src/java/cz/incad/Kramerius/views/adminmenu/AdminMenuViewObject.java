package cz.incad.Kramerius.views.adminmenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.security.KrameriusRoles;
import cz.incad.kramerius.security.IsUserInRoleDecision;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class AdminMenuViewObject {

    public static final Logger LOGGER = Logger.getLogger(AdminMenuViewObject.class.getName());
    @Inject
    ResourceBundleService resourceBundleService;
    @Inject
    HttpServletRequest request;
    @Inject
    Locale locale;

    @Inject
    Provider<User> currentUserProvider;
    
    
//    @Inject
//    IsUserInRoleDecision userInRoleDecision;
    @Inject
    KConfiguration kconfig;

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

    public String showActionsAdmin() throws IOException {
        return renderMenuItem(
                "javascript:rightsForRepository('"+SpecialObjects.REPOSITORY.getUuid()+"'); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.actionsAdmin.title");
    }

    public String noParamsProcess(String processName) throws IOException {
        return renderMenuItem(
                "javascript:noParamsProcess('" + processName + "'); javascript:hideAdminMenu();",
                "administrator.menu.dialogs." + processName + ".title");
    }

    public String editor() throws IOException {
        String localeParam = locale == null ? "" : "?locale=" + locale.getLanguage();
        String href = kconfig.getEditorURL() + localeParam;
        return renderMenuItem(href, "administrator.menu.dialogs.editor.title");
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
                if (hasUserAllowedAction(SecuredActions.MANAGE_LR_PROCESS.getFormalName())) {
                    menuItems.add(processes());
                }
                if (hasUserAllowedAction(SecuredActions.REPLIKATOR_MONOGRAPHS.getFormalName())) {
                    menuItems.add(importMonographs());
                }
                if (hasUserAllowedAction(SecuredActions.REPLIKATOR_PERIODICALS.getFormalName())) {
                    menuItems.add(importPeriodicals());
                }
                if (hasUserAllowedAction(SecuredActions.REINDEX.getFormalName())) {
                    menuItems.add(showIndexerAdmin());
                }
                if (hasUserAllowedAction(SecuredActions.ADMINISTRATE.getFormalName())) {
                    menuItems.add(showActionsAdmin());
                }

                if (hasUserAllowedAction(SecuredActions.ENUMERATOR.getFormalName())) {
                    menuItems.add(noParamsProcess(KrameriusRoles.ENUMERATOR.getRoleName()));
                }
                if (hasUserAllowedAction(SecuredActions.REPLICATIONRIGHTS.getFormalName())) {
                    menuItems.add(noParamsProcess(KrameriusRoles.REPLICATIONRIGHTS.getRoleName()));
                }
                if (hasUserAllowedAction(SecuredActions.CONVERT.getFormalName())) {
                    menuItems.add(noParamsProcess(KrameriusRoles.CONVERT.getRoleName()));
                }
                if (hasUserAllowedAction(SecuredActions.IMPORT.getFormalName())) {
                    menuItems.add(noParamsProcess(KrameriusRoles.IMPORT.getRoleName()));
                }
                if (hasUserAllowedAction(SecuredActions.EDITOR.getFormalName())) {
                    menuItems.add(editor());
                }
            }
            return menuItems.toArray(new String[menuItems.size()]);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return new String[0];
        }
    }

    private boolean hasUserAllowedAction(String actionFormalName) {
        User user = currentUserProvider.get();
        HttpSession session = this.request.getSession();
        List<String> actionsForRepository = (List<String>) session.getAttribute("securityForRepository");
        return actionsForRepository != null ? actionsForRepository.contains(actionFormalName) : false;
    }
}
