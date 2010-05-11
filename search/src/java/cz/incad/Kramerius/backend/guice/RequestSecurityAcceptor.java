package cz.incad.Kramerius.backend.guice;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;

import cz.incad.kramerius.security.SecurityAcceptor;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class RequestSecurityAcceptor implements SecurityAcceptor {

	public static String REMOTE_ADDRESS="remoteAddr";
	
	
	private Logger logger;
	private Provider<HttpServletRequest> provider;
	
	@Inject
	public RequestSecurityAcceptor(Provider<HttpServletRequest> provider, Logger logger) {
		super();
		this.provider = provider;
		this.logger = logger;
		this.logger.info("provider is '"+provider+"'");
	}

	@Override
	public boolean privateVisitor() {
		HttpServletRequest httpServletRequest = this.provider.get();
		String remoteAddr = httpServletRequest.getParameter(REMOTE_ADDRESS);
		if (remoteAddr == null) {
			remoteAddr = httpServletRequest.getRemoteAddr();
		}
		KConfiguration kConfiguration = KConfiguration.getKConfiguration();
		List<String> patterns = kConfiguration.getPatterns();
		if (patterns != null) {
			for (String regex : patterns) {
				if (remoteAddr.matches(regex)) return true;
			}
		}
		logger.info("Remote address is == "+remoteAddr);
		return false;
	}
}
