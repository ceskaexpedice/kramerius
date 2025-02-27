package cz.incad.kramerius.auth.thirdparty.impl;

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import cz.incad.kramerius.auth.thirdparty.ThirdPartyUser;
import cz.incad.kramerius.auth.thirdparty.ThirdPartyUsersSupport;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;

public abstract class AbstractThirdPartyUsersSupport<T extends ThirdPartyUser> implements ThirdPartyUsersSupport {

    protected Map<String, String> credentials = new ConcurrentHashMap<String, String>();
    private final ConcurrentHashMap<String, ReentrantLock> userLocks = new ConcurrentHashMap<>();
    protected UserManager usersManager;
    //protected LoggedUsersSingleton loggedUsersSingleton;

    public HttpServletRequest updateRequest(final HttpServletRequest req) {
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
    public void disconnectUser(String userName) {
        this.credentials.remove(userName);
        this.userLocks.remove(userName);
    }
    
    @Override
    public String getUserPassword(String userName) {
        return this.credentials.get(userName);
    }

    public UserManager getUserManager() {
        return this.usersManager;
    }

    public void setUserManager(UserManager uMan) {
        this.usersManager = uMan;
    }
    
    /*
    public LoggedUsersSingleton getLoggedUsersSingleton() {
        return loggedUsersSingleton;
    }

    public void setLoggedUsersSingleton(LoggedUsersSingleton loggedUsersSingleton) {
        this.loggedUsersSingleton = loggedUsersSingleton;
    }*/

    protected abstract String updateExistingUser(String userName, T wrapper) throws Exception;
    
    
    protected abstract boolean checkIfUserExists(String userName) throws Exception;
    
    
    protected abstract String createNewUser(String user, T w) throws Exception;

    
    protected abstract T createUserWrapper(HttpServletRequest req, String userName) throws Exception;

    
    public String storeUserPropertiesToSession(HttpServletRequest req, String userName) throws Exception {
        String password = null;
        T wrapper = createUserWrapper(req, userName);

        ReentrantLock lock = userLocks.computeIfAbsent(userName, k -> new ReentrantLock());
        
        lock.lock();
        try {
            if (checkIfUserExists(userName)) {
                password = updateExistingUser(userName, wrapper);
            } else {
                password = createNewUser(userName, wrapper);
            }
        } finally {
            lock.unlock();
        }
    
        this.credentials.put(userName, password);

        req.getSession().setAttribute(UserUtils.USER_NAME_PARAM, userName);
        req.getSession().setAttribute(UserUtils.PSWD_PARAM, password);
    
        req.getSession().setAttribute(UserUtils.FIRST_NAME_KEY, wrapper.getProperty(UserUtils.FIRST_NAME_KEY));
        req.getSession().setAttribute(UserUtils.LAST_NAME_KEY, wrapper.getProperty(UserUtils.LAST_NAME_KEY));

        wrapper.getPropertyKeys().stream().filter(it -> !it.equals(UserUtils.FIRST_NAME_KEY) &&  !it.equals(UserUtils.LAST_NAME_KEY)).forEach(it-> {
            String property = wrapper.getProperty(it);
            req.getSession().setAttribute(UserUtils.THIRD_PARTY_SESSION_PARAMS +it, wrapper.getProperty(it));
        });

        User user = wrapper.toUser(this.usersManager);
        req.getSession().setAttribute(UserUtils.LOGGED_USER_PARAM, user);

//        String key = loggedUsersSingleton.registerLoggedUser(user, req);
//        req.getSession().setAttribute(UserUtils.LOGGED_USER_KEY_PARAM, key);

        req.getSession().setAttribute(UserUtils.THIRD_PARTY_USER, Boolean.TRUE.toString());

        // TODO: move it; change before release
        HttpSession session = req.getSession();
        Enumeration attributeNames = session.getAttributeNames();
        while(attributeNames.hasMoreElements()) {
            String attributeName = (String) attributeNames.nextElement();
            if (attributeName.startsWith(UserUtils.THIRD_PARTY_SESSION_PARAMS)) {
                String rawKey = attributeName.substring(UserUtils.THIRD_PARTY_SESSION_PARAMS.length());
                String value = session.getAttribute(attributeName).toString();
                user.addSessionAttribute(rawKey, value);
            }
        }

        return password;
    }

    
}
