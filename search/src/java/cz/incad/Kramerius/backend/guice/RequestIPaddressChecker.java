package cz.incad.Kramerius.backend.guice;

import java.security.Principal;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.handlers.http.HTTPActionHandler;

import sun.rmi.transport.proxy.HttpReceiveSocket;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;

import cz.incad.kramerius.security.IPaddressChecker;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class RequestIPaddressChecker implements IPaddressChecker {

	
	private Logger logger;
	private Provider<HttpServletRequest> provider;
	
	@Inject
	public RequestIPaddressChecker(Provider<HttpServletRequest> provider, Logger logger) {
		super();
		this.provider = provider;
		this.logger = logger;
		this.logger.info("provider is '"+provider+"'");
	}

	@Override
	public boolean privateVisitor() {
		HttpServletRequest httpServletRequest = this.provider.get();
		String remoteAddr = httpServletRequest.getRemoteAddr();
		KConfiguration kConfiguration = KConfiguration.getInstance();
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
