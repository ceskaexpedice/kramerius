/*
 * Copyright (C) 2010 Pavel Stastny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.shib.utils;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.impl.http.AbstractLoggedUserProvider;

public class ShibbolethUtils {

    public static boolean isUnderShibbolethSession(HttpServletRequest httpServletRequest) {
        boolean foundIdentityProvider = false;
        Enumeration headerNames = httpServletRequest.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String hname = (String) headerNames.nextElement();
            if (hname.contains("Shib-Identity-Provider")) {
                String headerValue = httpServletRequest.getHeader(hname);
                if ((headerValue != null) && (!headerValue.trim().equals(""))) {
                    foundIdentityProvider = true;
                }
            }
            AbstractLoggedUserProvider.LOGGER.fine("header name '"+hname+"' = "+httpServletRequest.getHeader(hname));
        }
        return foundIdentityProvider;
    }

}
