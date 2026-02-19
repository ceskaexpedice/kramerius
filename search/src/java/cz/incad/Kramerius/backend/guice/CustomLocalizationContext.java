package cz.incad.Kramerius.backend.guice;

import com.google.inject.Provider;
import cz.incad.kramerius.service.ResourceBundleService;
import jakarta.servlet.http.HttpServletRequest;

// TODO migrationimport javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

// TODO migrationpublic class CustomLocalizationContext extends LocalizationContext {
public class CustomLocalizationContext {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(CustomLocalizationContext.class.getName());


	private Provider<Locale> localeProvider;
	private Provider<HttpServletRequest> requestsProvider;
	private ResourceBundleService bundleService;


	public CustomLocalizationContext(Provider<Locale> localeProvider, Provider<HttpServletRequest> requestProvider,
			ResourceBundleService bundleService) {
		super();
		this.localeProvider = localeProvider;
		this.bundleService = bundleService;
		this.requestsProvider = requestProvider;

	}


	// TODO migration @Override
	public Locale getLocale() {
		return this.localeProvider.get();
	}

	// TODO migration @Override
	public ResourceBundle getResourceBundle() {
		try {
			Locale locale = this.localeProvider.get();
            ResourceBundle resourceBundle = this.bundleService.getResourceBundle("labels", locale);
			return resourceBundle;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			// TODO migration return super.getResourceBundle();
			return null;
		}
	}
}
