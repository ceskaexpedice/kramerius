package cz.incad.kramerius.utils;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;

public class IPAddressUtils {
    
    public static Logger LOGGER = Logger.getLogger(IPAddressUtils.class.getName());
    
    public static final String X_IP_FORWARD = "X_IP_FORWARD";
    public static String[] LOCALHOSTS = {"127.0.0.1","localhost","0:0:0:0:0:0:0:1","::1"};
    static {
        try {
            IPAddressUtils.LOCALHOSTS = NetworkUtils.getLocalhostsAddress();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            IPAddressUtils.LOCALHOSTS = new String[] {"127.0.0.1","localhost","0:0:0:0:0:0:0:1","::1"};
        }
    }
    public static String getRemoteAddress(HttpServletRequest httpReq, Configuration conf) {
        String headerFowraded = httpReq.getHeader(X_IP_FORWARD);
        if (StringUtils.isAnyString(headerFowraded) && IPAddressUtils.matchConfigurationAddress(httpReq, conf)) {
            return headerFowraded;
        } else {
            return httpReq.getRemoteAddr();
        }
    }
    public static boolean matchConfigurationAddress(HttpServletRequest httpReq, Configuration conf) {
        String remoteAddr = httpReq.getRemoteAddr();
        List<String> forwaredEnabled = conf.getList("x_ip_forwared_enabled_for",Arrays.asList(LOCALHOSTS));
        if (!forwaredEnabled.isEmpty()) {
            for (String pattern : forwaredEnabled) {
                if (remoteAddr.matches(pattern)) return true;
            }
        }
        return false;
    }

}
