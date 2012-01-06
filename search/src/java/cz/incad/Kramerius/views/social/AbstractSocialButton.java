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
package cz.incad.Kramerius.views.social;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.utils.ApplicationURL;

public abstract class AbstractSocialButton {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AbstractSocialButton.class.getName());
    
    private static final String I_JSP = "i.jsp";
    private static final String S_JSP = "s.jsp";
    private static final String SEARCH_JSP = "search.jsp";
    
    @Inject
    Provider<HttpServletRequest> requestProvider;

    public abstract boolean isButtonEnabled();

    public boolean emptyString(String str) {
        return (str == null) || (str.trim().equals(""));
    }
    
    public boolean isHomePage() {
        String contextPath = ApplicationURL.applicationContextPath(this.requestProvider.get());
        String requestedURL = this.requestProvider.get().getRequestURL().toString();
        int indexOfContextPath = requestedURL.indexOf(contextPath);
        String queryString = this.requestProvider.get().getQueryString();
        if (emptyString(queryString)) {
            String stringAfterContext = requestedURL.substring(indexOfContextPath+contextPath.length());
            return stringAfterContext.equals("/") || stringAfterContext.equals("/search.jsp");
        } else return false;
    }
    
    public boolean isItemPage() {
        try {
            HttpServletRequest request = this.requestProvider.get();
            String requestedURL = request.getRequestURL().toString();
            URL url = new URL(requestedURL);
            String furl = ApplicationURL.minus(requestedURL, "?"+url.getQuery());
            return furl.endsWith(I_JSP);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return false;
    }
    
    public boolean isSearchPage() {
        try {
            HttpServletRequest request = this.requestProvider.get();
            String requestedURL = request.getRequestURL().toString();
            URL url = new URL(requestedURL);
            String furl = ApplicationURL.minus(requestedURL, "?"+url.getQuery());
            return furl.endsWith(S_JSP);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return false;
    }

    public String getShareURL() {
        HttpServletRequest request = this.requestProvider.get();
        if (isItemPage()) {
            String applicationURL = ApplicationURL.applicationURL(request);
            return applicationURL+"/handle/"+getPidParam(request);
        } else {
            String requestedURL = request.getRequestURL().toString();
            String query = request.getQueryString();
            String returnedShareURL = requestedURL;
            if (!emptyString(query)) {
                returnedShareURL = requestedURL+"?"+query;
            }
            return returnedShareURL;
        }
        
        
    }

    public String getPidParam(HttpServletRequest request) {
        String pid = request.getParameter("pid");
        return pid;
    }
}
