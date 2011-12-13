package cz.incad.Kramerius.backend.guice;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.users.ProfilePrepareUtils;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.service.impl.ResourceBundleServiceImpl;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;

public class LocalesProvider implements Provider<Locale>{

    
	public static final String CLIENT_LOCALE = "client_locale";
	
	private Provider<HttpServletRequest> provider;
	private Logger logger;
	private TextsService textsService;
	

	private LoggedUsersSingleton loggedUsersSingleton;
	private UserProfileManager userProfileManager;
	private Provider<User> userProvider;
	
	@Inject
	public LocalesProvider(Provider<HttpServletRequest> provider, Logger logger, TextsService textsService, 
	        LoggedUsersSingleton loggedUsersSingleton, UserProfileManager userProfileManager, Provider<User> userProvider) {
		super();
		this.provider = provider;
		this.logger = logger;
		this.textsService = textsService;
		this.loggedUsersSingleton = loggedUsersSingleton;
		this.userProfileManager = userProfileManager;
		this.userProvider = userProvider;
	}

	
	@Override
	public Locale get() {
        UserProfile profile = this.userProfileManager.getProfile(this.userProvider.get());
	    
	    HttpServletRequest request = this.provider.get();
		HttpSession session = request.getSession(true);
		String parameter = request.getParameter("language");
		if (parameter != null) {
			Locale foundLocale = this.textsService.findLocale(parameter);
			if (foundLocale == null) {
				foundLocale =  getDefault(request);
			}
			storeLocale(session, foundLocale, profile);
			return foundLocale;
        } else if (session.getAttribute(CLIENT_LOCALE) != null) {
            return (Locale) session.getAttribute(CLIENT_LOCALE);
		} else {
			return getDefault(request);
		}
	}


    
	

    public void storeLocale(HttpSession session, Locale foundLocale, UserProfile profile) {
        session.setAttribute(CLIENT_LOCALE,foundLocale);
        // do not store to database but keep only in session
        if (this.loggedUsersSingleton.isLoggedUser(this.provider)) {
            ProfilePrepareUtils.prepareProperty(session, CLIENT_LOCALE,foundLocale.getLanguage());
        }        
    }


	private Locale getDefault(HttpServletRequest request) {
		Locale requestLocale = request.getLocale();
		Locale foundLocale = this.textsService.findLocale(requestLocale.getLanguage());
		if (foundLocale != null) {
			return foundLocale;
		} else return requestLocale;
	}
}
