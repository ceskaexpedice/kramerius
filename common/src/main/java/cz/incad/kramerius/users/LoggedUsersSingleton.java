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

 
public interface LoggedUsersSingleton {
    //TODO: CHANGE IT
    //public LoggedUsersSingleton INSTANCE = new LoggedUsersSingletonImpl();

    public String registerLoggedUser(User user);

    public void deregisterLoggedUser(String key);

    public boolean isLoggedUser(String key);
    
    public boolean isLoggedUser(Provider<HttpServletRequest> provider);

    public int getSessionKeyId(String key);

    public User getLoggedUser(String key);
    public User getUser(String key);

}
