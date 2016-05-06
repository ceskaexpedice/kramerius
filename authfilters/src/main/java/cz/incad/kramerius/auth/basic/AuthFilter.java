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
package cz.incad.kramerius.auth.basic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.logging.Level;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import biz.sourcecode.base64Coder.Base64Coder;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.security.jaas.K4LoginModule;
import cz.incad.kramerius.security.jaas.K4User;

/**
 * Supporting basic authetnication filter
 * @author pavels
 */
public class AuthFilter  implements Filter {

    private static final String AUTH_TOKEN_HEADER_KEY = "auth-token";
    private static final String TOKEN_ATTRIBUTE_KEY = "token";

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AuthFilter.class.getName());
    
    private String realm = null;
    
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider = null;

    @Inject
    LRProcessManager lrProcessManager;

    
    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) arg0;
            HttpServletResponse response = (HttpServletResponse) arg1;
            String authToken = request.getHeader(AUTH_TOKEN_HEADER_KEY);
            boolean authTokenDefined = (authToken != null && (!this.lrProcessManager.isAuthTokenClosed(authToken)));
            if (authTokenDefined) {
                // authtoken - forward 
                filterChain.doFilter(request, response);
            } else if (request.getUserPrincipal() == null) {
                // try to basic auth
                basicAuth(filterChain, request, response);
            } else {
                // authenticated user - only forward
                filterChain.doFilter(request, response);
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    public void basicAuth(FilterChain arg2, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, NoSuchAlgorithmException, IOException, ServletException {
        String header = request.getHeader("Authorization");
        if (header!=null && header.trim().startsWith("Basic")) {
            String uname = header.trim().substring("Basic".length()).trim();
            byte[] decoded = Base64Coder.decode(uname.toCharArray());
            String fname = new String(decoded, "UTF-8");
            if (fname.contains(":")) {
                String username = fname.substring(0, fname.indexOf(':'));
                String password = fname.substring(fname.indexOf(':')+1);
                HashMap<String,Object> user = K4LoginModule.findUser(connectionProvider.get(), username);
                if (user != null) {
                    boolean checked = K4LoginModule.checkPswd(username, user.get("pswd").toString(), password.toCharArray());
                    if (checked) {
                        K4User principal = new K4User(username);
                        HttpServletRequest authenticated = BasicAuthenticatedHTTPServletProxy.newInstance(request, principal);
                        arg2.doFilter(authenticated, response);
                    } else {
                        arg2.doFilter(request, response);
                    }
                } else {
                    arg2.doFilter(request, response);
                }
            } else {
            	arg2.doFilter(request, response);
            }
        } else {
            arg2.doFilter(request, response);
        }
    }

    public void sendError(HttpServletResponse response) throws IOException {
        response.setHeader( "WWW-Authenticate", "Basic realm=\"" + this.realm + "\"" );
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public void init(FilterConfig conf) throws ServletException {
        Injector injector = getInjector(conf);
        injector.injectMembers(this);
        this.realm = conf.getInitParameter("realm");
    }
    
    protected Injector getInjector(FilterConfig config) {
        return (Injector) config.getServletContext().getAttribute(Injector.class.getName());
    }

}
