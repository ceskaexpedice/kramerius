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
package cz.incad.kramerius.users;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Provider;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.impl.LoggedUsersSingletonImpl;

/**
 * Manages logged users 
 * @author pavels
 */
public interface LoggedUsersSingleton {

    /**
     * Register logged user
     * @param user User to register
     * @return Session key
     */
    public String registerLoggedUser(User user);

    
    /**
     * Deregister logged user
     * @param key Session key
     */
    public void deregisterLoggedUser(String key);

    /**
     * Returns true when given key is live session key 
     * @param key Tested key
     * @return
     */
    public boolean isLoggedUser(String key);

    /**
     * Returns true when user associated with given http request is live user
     * @param provider http servlet request provider
     * @return
     */
    public boolean isLoggedUser(Provider<HttpServletRequest> provider);
    
    /**
     * Find database record associated with given session key and returns id. Otherwise returns -1;
     * @param key
     * @return
     */
    public int getSessionKeyId(String key);

    /**
     * Returns logged user assoicated with session key
     * @param key Session key
     * @return
     */
    public User getLoggedUser(String key);
    
    /**
     * Returns user (logged or not logged) assocated with session key
     * @param key Session key
     */
    public User getUser(String key);

}
