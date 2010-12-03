package cz.incad.Kramerius.backend.guice;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

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
    }

    @Override
    public boolean privateVisitor() {
        KConfiguration kConfiguration = KConfiguration.getInstance();
        List<String> patterns = kConfiguration.getPatterns();
        return checkPatterns(patterns);
    }

    private boolean checkPatterns(List<String> patterns) {
        HttpServletRequest httpServletRequest = this.provider.get();
        String remoteAddr = httpServletRequest.getRemoteAddr();
        if (patterns != null) {
            for (String regex : patterns) {
                if (remoteAddr.matches(regex))
                    return true;
            }
        }
        logger.finer("Remote address is == " + remoteAddr);
        return false;
    }

    @Override
    public boolean localHostVisitor() {
        List<String> lrControllingAddrs = KConfiguration.getInstance().getLRControllingAddresses();
        return checkPatterns(lrControllingAddrs);
    }

}
