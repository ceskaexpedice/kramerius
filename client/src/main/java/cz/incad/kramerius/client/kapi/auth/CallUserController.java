/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.client.kapi.auth;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import cz.incad.kramerius.security.utils.PasswordDigest;

/**
 * Authentication controller
 * @author pavels
 */
public abstract class CallUserController {

    public static final Logger LOGGER = Logger.getLogger(CallUserController.class.getName());
    
    public static final String KEY = "usersController";

    public abstract void createCaller(String name, String pswd, Class<? extends User> user);

    public abstract void createCaller(String name, String pswd, Class<? extends User> user, User.UserProvider provider);

    public abstract AdminUser getAdminCaller();
        
    public abstract ClientUser getClientCaller();
        
    public abstract ProfileDelegator getProfileDelegator();
        
    public abstract JSONObject getUserJSONRepresentation();
        
    public abstract JSONObject getProfileJSONRepresentation();


    public static final Map<String, String>  ACTIVATED_CREDENTIALS = new HashMap<String, String>();

    public static synchronized void credentialsTable(String u, String p) {
        try {
            String hashed = PasswordDigest.messageDigest(p);
            ACTIVATED_CREDENTIALS.put(u, hashed);
        } catch (NoSuchAlgorithmException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }
    
    public static synchronized boolean check(String u, String p) {
        try {
            String hashed = PasswordDigest.messageDigest(p);
            String fromTable = ACTIVATED_CREDENTIALS.get(u);
            return hashed.equals(fromTable);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return false;
        } catch (NoSuchAlgorithmException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return false;
        }
    }
    
    public static synchronized void clearCredentials(String u) {
        ACTIVATED_CREDENTIALS.remove(u);
    }
}
