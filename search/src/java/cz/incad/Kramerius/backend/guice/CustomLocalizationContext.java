package cz.incad.Kramerius.backend.guice;

import static cz.incad.kramerius.utils.IOUtils.copyStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.service.ResourceBundleService;

public class CustomLocalizationContext extends LocalizationContext {
	
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


	@Override
	public Locale getLocale() {
		return this.localeProvider.get();
	}

	@Override
	public ResourceBundle getResourceBundle() {
		try {
			Locale locale = this.localeProvider.get();
            ResourceBundle resourceBundle = this.bundleService.getResourceBundle("labels", locale);
			return resourceBundle;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return super.getResourceBundle();
		}
	}
}
