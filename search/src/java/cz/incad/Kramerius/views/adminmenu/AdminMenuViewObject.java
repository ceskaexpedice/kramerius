package cz.incad.Kramerius.views.adminmenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.antlr.stringtemplate.StringTemplate;


import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.security.KrameriusRoles;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.shib.utils.ShibbolethUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class AdminMenuViewObject {

    //private static final String SHIBBOLETH_KEY = "shibboleth";

    public static final Logger LOGGER = Logger.getLogger(AdminMenuViewObject.class.getName());
    @Inject
    ResourceBundleService resourceBundleService;
    @Inject
    HttpServletRequest request;

    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    @Inject
    Locale locale;

    @Inject
    Provider<User> currentUserProvider;

    @Inject
    UserManager userManager;
    
    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    
//    @Inject
//    IsUserInRoleDecision userInRoleDecision;
    @Inject
    KConfiguration kconfig;

    public String processes() throws IOException {
        return renderMenuItem(
                "javascript:processes.processes(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.lrprocesses.title", false);
    }

    public String importMonographs() throws IOException {
        return renderMenuItem(
                "javascript:importMonographs(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.importMonograph.title", false);
    }

    public String importPeriodicals() throws IOException {
        return renderMenuItem(
                "javascript:importPeriodicals(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.importPeriodical.title", false);
    }

    public String showIndexerAdmin() throws IOException {
        return renderMenuItem(
                "javascript:showIndexerAdmin(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.indexDocuments.title", false);
    }

    public String showActionsAdmin() throws IOException {
        return renderMenuItem(
                "javascript:globalActions.globalActions(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.actionsAdmin.title", false);
    }

    public String showVirtualCollectionsAdmin() throws IOException {
        return renderMenuItem(
                "javascript:showVirtualCollectionsAdmin(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.virtualcollections.title", false);
    }

    public String enumerator() throws IOException {
        return renderMenuItem(
                "javascript:enumerator(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.enumerator.title", false);
    }

    public String replicationrights() throws IOException {
        return renderMenuItem(
                "javascript:replicationRights(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.replicationrights.title", false);
    }

    public String openUsersAdmin() throws IOException {
        String href = kconfig.getUsersEditorURL();
        return renderMenuItem(href, "administrator.menu.userseditor",true);
       

    }

    public String convert() throws IOException {
        return renderMenuItem(
                "javascript:convert(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.convert.title", false);
    }

    public String impor() throws IOException {
        return renderMenuItem(
                "javascript:impor(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.import.title", false);
    }

    public String noParamsProcess(String processName) throws IOException {
        return renderMenuItem(
                "javascript:noParamsProcess('" + processName + "'); javascript:hideAdminMenu();",
                "administrator.menu.dialogs." + processName + ".title", false);
    }

    public String editor() throws IOException {
        String localeParam = locale == null ? "" : "?locale=" + locale.getLanguage();
        String href = kconfig.getEditorURL() + localeParam;
        return renderMenuItem(href, "administrator.menu.dialogs.editor.title", true);
    }


    public String rolesEditor() throws IOException {
        return renderMenuItem(
                "javascript:roles.showRoles(); javascript:hideAdminMenu();",
                "rights.rolesedit.title", false);
    }

    private String renderMenuItem(String href, String labelKey, boolean newWindow) throws IOException {
        String label = this.resourceBundleService.getResourceBundle("labels", this.locale).getString(labelKey);
        return String.format("<div align=\"left\"> <a href=\"%s\""+(newWindow?" target=\"_blank\"":"")+"> %s </a> </div>",
                href, label);
    }

    public String[] getAdminMenuItems() {
        try {
            List<String> menuItems = new ArrayList<String>();
            if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
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
                    menuItems.add(enumerator());
                }
                if (hasUserAllowedAction(SecuredActions.REPLICATIONRIGHTS.getFormalName())) {
                    menuItems.add(replicationrights());
                }
                if (hasUserAllowedAction(SecuredActions.CONVERT.getFormalName())) {
                    menuItems.add(convert());
                }
                if (hasUserAllowedAction(SecuredActions.IMPORT.getFormalName())) {
                    menuItems.add(impor());
                }
                if (hasUserAllowedAction(SecuredActions.EDITOR.getFormalName())) {
                    menuItems.add(editor());
                }
                if (hasUserAllowedAction(SecuredActions.EDITOR.getFormalName())) {
                    menuItems.add(rolesEditor());
                }
                //TODO: Should it be in shibboleth ?
                if (!ShibbolethUtils.isUnderShibbolethSession(this.request)) {
                    menuItems.add(openUsersAdmin());
                }

                menuItems.add(showProfile());

                if (!ShibbolethUtils.isUnderShibbolethSession(this.request)) {
                    menuItems.add(changepswd());
                }
                
                //TODO add administrate virtual collections to actions
                //if (hasUserAllowedAction(SecuredActions.VIRTUALCOLLECTION.getFormalName())) {
                    menuItems.add(showVirtualCollectionsAdmin());
                //}

                

            }
            return menuItems.toArray(new String[menuItems.size()]);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return new String[0];
        }
    }
    
    private String showProfile() throws IOException {
        return renderMenuItem(
                "javascript:showSearchHistory.showHistory(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.profile.title", false);
    }

    private String changepswd() throws IOException {
        return renderMenuItem(
                "javascript:(new ChangePswd()).changePassword(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.changePswd.title", false);
    }

    
    
    private boolean hasUserAllowedAction(String actionFormalName) {
        User user = currentUserProvider.get();
        HttpSession session = this.request.getSession();
        List<String> actionsForRepository = (List<String>) session.getAttribute("securityForRepository");
        return actionsForRepository != null ? actionsForRepository.contains(actionFormalName) : false;
    }

    public boolean isLoggedUser() {
        return loggedUsersSingleton.isLoggedUser(this.requestProvider);
    }
    
    public boolean isLoginVisible() {
        return !loggedUsersSingleton.isLoggedUser(this.requestProvider);
    }
    
    public boolean isLogoutVisible() {
        boolean shibSession = ShibbolethUtils.isUnderShibbolethSession(this.request);
        boolean loggedUser = isLoggedUser();
        return !shibSession && loggedUser;
    }
    
    
    
}
