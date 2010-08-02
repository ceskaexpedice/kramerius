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

		boolean copyDefaults = true;
		File[] listFiles = this.bundleService.bundlesFolder().listFiles();
		if (listFiles != null) {
			for (File file : listFiles) {
				if (file.getName().equals("labels"+".properties")) {
					copyDefaults =  false;
					break;
				}
			}
		}
		try {
			if (copyDefaults) copyDefault();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);;
		}
	}

	private void copyDefault() throws IOException {
		String[] defaults = 
		{
		"labels_en.properties",
		"labels_cs.properties"
		};
		for (String base : defaults) {
			InputStream is = null;
			OutputStream os = null;
			try {
				is = this.getClass().getClassLoader().getResourceAsStream(base);
				os = new FileOutputStream(new File(this.bundleService.bundlesFolder(),base));
				copyStreams(is, os);
			} finally {
				if (os != null) os.close();
				if (is != null) is.close();
			}
		}
		
		// cs locale as default
		InputStream is = null;
		OutputStream os = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream("labels_cs.properties");
			os = new FileOutputStream(new File(this.bundleService.bundlesFolder(),"labels.properties"));
			copyStreams(is, os);
		} finally {
			if (os != null) os.close();
			if (is != null) is.close();
		}

	}

	@Override
	public Locale getLocale() {
		return this.localeProvider.get();
	}

	@Override
	public ResourceBundle getResourceBundle() {
		try {
			ResourceBundle resourceBundle = this.bundleService.getResourceBundle("labels", this.localeProvider.get());
			return resourceBundle;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return super.getResourceBundle();
		}
	}
}
