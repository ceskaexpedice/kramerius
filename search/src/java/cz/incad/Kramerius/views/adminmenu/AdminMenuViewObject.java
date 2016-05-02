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

import cz.incad.Kramerius.exts.menu.main.MainMenu;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUtils;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class AdminMenuViewObject {


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
    
    
    @Inject
    KConfiguration kconfig;

    @Inject
    MainMenu mainMenu;
    


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
    
    
    public MainMenu getMainMenu() {
        return this.mainMenu;
    }
    
}
