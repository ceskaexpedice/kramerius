package cz.incad.Kramerius.backend.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.service.ResourceBundleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.jstl.fmt.LocalizationContext;

import java.util.Locale;

public class CustomLocalizedContextProvider implements Provider<LocalizationContext>{

	public static final String BUNDLE_KEY = "bundle";


	private Provider<Locale> localeProvider;
	private Provider<HttpServletRequest> requestsProvider;
	private ResourceBundleService bundleService;

	
	@Inject
	public CustomLocalizedContextProvider(Provider<Locale> localeProvider, Provider<HttpServletRequest> requestsProvider,
			ResourceBundleService bundleService) {
		super();
		this.localeProvider = localeProvider;
		this.bundleService = bundleService;
		this.requestsProvider = requestsProvider;
	}



	@Override
	public LocalizationContext get() {
		CustomLocalizationContext customLocalizationContext = new CustomLocalizationContext(this.localeProvider,  this.requestsProvider,this.bundleService);
		LocalizationContext ctxCopy = new LocalizationContext(customLocalizationContext.getResourceBundle(), customLocalizationContext.getLocale());
		this.requestsProvider.get().getSession(true).setAttribute(BUNDLE_KEY, ctxCopy);
		return customLocalizationContext;
	}
	
}


