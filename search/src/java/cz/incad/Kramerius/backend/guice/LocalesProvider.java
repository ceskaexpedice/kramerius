package cz.incad.Kramerius.backend.guice;

import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.service.impl.ResourceBundleServiceImpl;

public class LocalesProvider implements Provider<Locale>{

	public static final String CLIENT_LOCALE = "client_locale";
	
	private Provider<HttpServletRequest> provider;
	private Logger logger;
	private TextsService textsService;
	
	@Inject
	public LocalesProvider(Provider<HttpServletRequest> provider, Logger logger, TextsService textsService) {
		super();
		this.provider = provider;
		this.logger = logger;
		this.textsService = textsService;
	}

	
	@Override
	public Locale get() {
		HttpServletRequest request = this.provider.get();
		HttpSession session = request.getSession(true);
		String parameter = request.getParameter("language");
		if (parameter != null) {
			Locale foundLocale = this.textsService.findLocale(parameter);
			if (foundLocale == null) {
				foundLocale =  getDefault(request);
			}
			session.setAttribute(CLIENT_LOCALE,foundLocale);
			return foundLocale;
		} else if (session.getAttribute(CLIENT_LOCALE) != null) {
			return (Locale) session.getAttribute(CLIENT_LOCALE);
		} else {
			return getDefault(request);
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
