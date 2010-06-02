package cz.incad.Kramerius.backend.guice;

import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class LocalesProvider implements Provider<Locale>{

	
	//private HttpServletRequest request;
	private Provider<HttpServletRequest> provider;
	private Logger logger;
	
	@Inject
	public LocalesProvider(Provider<HttpServletRequest> provider, Logger logger) {
		super();
		this.provider = provider;
		this.logger = logger;
	}

	
	@Override
	public Locale get() {
		HttpServletRequest request = this.provider.get();
		logger.info("Provider "+request.getQueryString()+" 0x"+Integer.toHexString(System.identityHashCode(request)));
		Locale locale = request.getLocale();
		logger.info("client locale "+locale);
		return locale;
	}
}
