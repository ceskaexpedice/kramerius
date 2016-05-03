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
package cz.incad.kramerius.client.tools;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.velocity.tools.generic.ValueParser;

import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUtils;
import cz.incad.kramerius.auth.thirdparty.social.utils.OpenIDFlag;
import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.utils.StringUtils;

/**
 * Controls whether current session is authenticated
 * 
 * @author pavels
 */
public class LoggedController {

    protected HttpServletRequest req;

    public void configure(Map props) {
        req = (HttpServletRequest) props.get("request");
    }

    /**
     * Returns true if the current session is authenticated
     * @return
     */
    public boolean isLogged() {
        return req.getSession() != null
                && req.getSession().getAttribute(CallUserController.KEY) != null;
    }
    
    /**
     * Returns true, if session contains information about profile
     * @return
     */
    public boolean isProfileRetrieved() {
        if (isLogged()) {
            CallUserController cuser = (CallUserController) req.getSession().getAttribute(CallUserController.KEY);
            return cuser.getProfileJSONRepresentation() != null;
        } else  return false;
    }
    
    /**
     * Returns logged name
     * @return
     */
    public String getLoggedName() {
        if (!this.isLogged()) return "Login"; //TODO: I18N change it !!
        else { 
            CallUserController controll = (CallUserController) req.getSession().getAttribute(CallUserController.KEY);
            return controll.getClientCaller().getFirstName() +" "+controll.getClientCaller().getSurname();
        }
    }

    /**
     * Returns json informations about user
     * @return
     */
    public String getUserJSONRepresentation() {
        if (!this.isLogged()) return "{}"; //TODO: I18N change it !!
        else {
            CallUserController controll = (CallUserController) req.getSession().getAttribute(CallUserController.KEY);
            if (controll != null && controll.getUserJSONRepresentation() != null) {
                return controll.getUserJSONRepresentation().toString();
            } else return "{}";
        }
    }

    /**
     * Returns json informations about profile
     * @return
     */
    public String getProfileJSONRepresentation() {
        if (!this.isProfileRetrieved()) return "{}"; //TODO: I18N change it !!
        else {
            CallUserController controll = (CallUserController) req.getSession().getAttribute(CallUserController.KEY);
            return controll.getProfileJSONRepresentation().toString();
        }
    }

    public boolean isUnderShibbolethSession() {
        return ShibbolethUtils.isShibbolethSessionIsStored(this.req);
    }

    public boolean isOpenIdSession() {
        OpenIDFlag flag = OpenIDFlag.flagFromRequest(this.req);
        return (!flag.equals(OpenIDFlag.UNTOUCHED));
    }

}
