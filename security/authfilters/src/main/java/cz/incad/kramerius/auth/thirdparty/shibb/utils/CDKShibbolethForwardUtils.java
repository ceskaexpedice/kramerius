package cz.incad.kramerius.auth.thirdparty.shibb.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.HashedMap;

import cz.incad.kramerius.security.impl.http.AbstractLoggedUserProvider;

public class CDKShibbolethForwardUtils {

    // must be controlled by IP or client certificate
    public static final String CDK_HEADER_KEY = "CDK_TOKEN_PARAMETERS";

    private CDKShibbolethForwardUtils() {
    }

    public static boolean isTokenFromCDK(HttpServletRequest httpServletRequest) {
        boolean foundIdentityProvider = false;
        Enumeration headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hname = (String) headerNames.nextElement();
            if (hname.toLowerCase().contains(CDK_HEADER_KEY.toLowerCase())) {
                String headerValue = httpServletRequest.getHeader(hname);
                if ((headerValue != null) && (!headerValue.trim().equals(""))) {
                    foundIdentityProvider = true;
                }
            }
            AbstractLoggedUserProvider.LOGGER
                    .fine("header name '" + hname + "' = " + httpServletRequest.getHeader(hname));
        }
        return foundIdentityProvider;
    }

    public static Map<String, String> tokenHeaders(String tokenValue) {
        Map<String, String> map = new HashMap<>();
        map.put(CDK_HEADER_KEY, tokenValue);
        StringTokenizer tokenizer = new StringTokenizer(tokenValue, "|");
        while (tokenizer.hasMoreTokens()) {
            String part = tokenizer.nextToken();
            String[] nameValue = part.split("=");
            if (nameValue.length > 1) {
                if (nameValue[0].startsWith("header_")) {
                    map.put("cdk_"+nameValue[0].substring("header_".length()), nameValue[1]);
                }
            }
        }
        return map;
    }


}
