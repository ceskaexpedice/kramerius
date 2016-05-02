package cz.incad.kramerius.auth.thirdparty.impl;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import cz.incad.kramerius.auth.thirdparty.AuthenticatedUsers;
import cz.incad.kramerius.auth.thirdparty.UsersWrapper;
import cz.incad.kramerius.security.utils.UserUtils;

public abstract class AbstractAuthenticatedUsers<T extends UsersWrapper> implements AuthenticatedUsers {

    protected Map<String, String> credentials = new HashMap<String, String>();

    
    public synchronized HttpServletRequest updateRequest(final HttpServletRequest req) {
        final Object userName = req.getSession().getAttribute(UserUtils.USER_NAME_PARAM);
        final Object password = req.getSession().getAttribute(UserUtils.PSWD_PARAM);
        if (userName != null && password != null) {
            return new HttpServletRequestWrapper(req) {
    
                @Override
                public String getRemoteUser() {
                  return userName.toString();
                }
                
                @Override
                public Principal getUserPrincipal() {
                    Principal userPrincipal = super.getUserPrincipal();
                    if (userPrincipal == null) {
                        userPrincipal = new Principal() {
                            
                            @Override
                            public String getName() {
                                return userName.toString();
                            }
                        };
                    }
                    return userPrincipal;
                }
    
                @Override
                public String getHeader(String name) {
                    HttpSession session = req.getSession();
                    Object value = session.getAttribute(name);
                    if (value != null) {
                        return value.toString();
                    } else {
                        return super.getHeader(name);
                    }
                }
            };
        } else return req;
    }
    
    @Override
    public synchronized void disconnectUser(String userName) {
        this.credentials.remove(userName);
    }
    
    @Override
    public synchronized String getUserPassword(String userName) {
        return this.credentials.get(userName);
    }
    
    protected abstract String updateExistingUser(String userName, T wrapper) throws Exception;
    
    
    protected abstract boolean checkIfUserExists(String userName) throws Exception;
    
    
    protected abstract String createNewUser(String user, T w) throws Exception;

    
    protected abstract T createUserWrapper(HttpServletRequest req, String userName) throws Exception;

    
    public synchronized String storeUserPropertiesToSession(HttpServletRequest req, String userName) throws Exception {
        String password = null;
        T wrapper = createUserWrapper(req, userName);
    
        if (checkIfUserExists(userName)) {
            password = updateExistingUser(userName, wrapper);
        } else {
            password = createNewUser(userName, wrapper);
        }
    
        this.credentials.put(userName, password);
    
        req.getSession().setAttribute(UserUtils.USER_NAME_PARAM, userName);
        req.getSession().setAttribute(UserUtils.PSWD_PARAM, password);
    
        req.getSession().setAttribute(UserUtils.FIRST_NAME_KEY, wrapper.getProperty(UserUtils.FIRST_NAME_KEY));
        req.getSession().setAttribute(UserUtils.LAST_NAME_KEY, wrapper.getProperty(UserUtils.LAST_NAME_KEY));

        return password;
    }

    
}
