package cz.incad.kramerius.client.utils;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;

import cz.incad.kramerius.client.tools.K5Configuration;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;

public class RedirectHelp {

    public static String redirectApplication(HttpServletRequest req) throws ConfigurationException {
        String k4host = K5Configuration.getK5ConfigurationInstance().getConfigurationObject().getString("k4.redirectpoint");
        if (k4host == null) {
            String contextPath = ApplicationURL.applicationContextPath(req);
            k4host = StringUtils.minus(ApplicationURL.applicationURL(req), contextPath);
        }
        if (!k4host.endsWith("/")) {
            k4host += "/";
        }
        return k4host;
    }
    
}
