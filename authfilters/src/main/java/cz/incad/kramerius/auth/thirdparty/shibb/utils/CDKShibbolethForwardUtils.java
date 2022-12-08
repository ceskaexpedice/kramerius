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
	
	private CDKShibbolethForwardUtils() {}

    public static boolean isTokenFromCDK(HttpServletRequest httpServletRequest) {
        boolean foundIdentityProvider = false;
        Enumeration headerNames = httpServletRequest.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String hname = (String) headerNames.nextElement();
            if (hname.toLowerCase().contains(CDK_HEADER_KEY.toLowerCase())) {
                String headerValue = httpServletRequest.getHeader(hname);
                if ((headerValue != null) && (!headerValue.trim().equals(""))) {
                    foundIdentityProvider = true;
                }
            }
            AbstractLoggedUserProvider.LOGGER.fine("header name '"+hname+"' = "+httpServletRequest.getHeader(hname));
        }
        return foundIdentityProvider;
    }
    
    
    public static Map<String, String> tokenHeaders(String tokenValue) {
    	Map<String, String> map = new HashMap<>();
    	StringTokenizer tokenizer = new StringTokenizer(tokenValue, "|");
    	while(tokenizer.hasMoreTokens()) {
    		String part = tokenizer.nextToken();
			String[] nameValue = part.split("=");
			if (nameValue.length > 1) {
				if (nameValue[0].startsWith("header_")) {
					map.put(nameValue[0].substring("header_".length()), nameValue[1]);
				}  
			}
    	}
    	return map;
    }
    
    // principalName=,
    // ip address = 
    /*
     * header_test=
     * header_test2=
     * header_test3=
     * s
     */

//    shib-session-id=_dd68cbd66641c9b647b05509ac0241fa
//    		shib-session-index=_36e3755e67acdeaf1b8b6f7ebebecdeb3abd6ddc9a
//    		shib-session-expires=1592847906
//    		shib-identity-provider=https://shibboleth.mzk.cz/simplesaml/metadata.xml
//    		shib-authentication-method=urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport
//    		shib-handler=https://dnnt.mzk.cz/Shibboleth.sso
//    		remote_user=all_users@mzk.cz
//    		affilation=all_access@mzk.cz;member@mzk.cz
//    		edupersonuniqueid=user@mzk.cz
//			_remote_ip=    
    public static void main(String[] args) {
    	String token = "header_shib-session-id=_dd68cbd66641c9b647b05509ac0241fa|header_shib-session-expires=1592847906|header_shib-identity-provider=https://shibboleth.mzk.cz/simplesaml/metadata.xml|header_shib-authentication-method=urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport|header_shib-handler=https://dnnt.mzk.cz/Shibboleth.sso|header_remote_user=all_users@mzk.cz|header_affilation=all_access@mzk.cz;member@mzk.cz|header_edupersonuniqueid=user@mzk.cz";	
    	Map<String, String> tokenHeaders = tokenHeaders(token);
    	System.out.println(tokenHeaders);
    }
    
}
