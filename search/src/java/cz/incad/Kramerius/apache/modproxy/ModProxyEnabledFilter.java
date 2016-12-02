/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.Kramerius.apache.modproxy;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import cz.incad.Kramerius.backend.guice.K4GuiceFilter;
import cz.incad.kramerius.auth.basic.BasicAuthenticatedHTTPServletProxy;

/**
 * @author pavels
 *
 */
public class ModProxyEnabledFilter extends K4GuiceFilter{
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ModProxyEnabledFilter.class.getName());
    
    @Override
    public void destroy() {}


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            String header = httpReq.getHeader("x-forwarded-host");
            if (header != null) {
                String requestUri = httpReq.getRequestURI();
                String protocol = new URL(httpReq.getRequestURL().toString()).getProtocol();
                String createdURL = createURL(header, protocol, requestUri);
                HttpServletRequest proxied = BehindModProxyRequest.newInstance(httpReq, createdURL);
                chain.doFilter(proxied, resp);
            } else {
                // http proxy not defined
                chain.doFilter(httpReq, resp);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }


    private String createURL(String header, String protocol, String requestUri) {
        return protocol+"://"+header+"/"+requestUri;
    }
}
