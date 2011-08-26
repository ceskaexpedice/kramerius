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
package cz.incad.Kramerius.views.rights;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.views.AbstractViewObject;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;

public class AbstractRightsView extends AbstractViewObject {

    public static final String RIGHT_ID="ids";
    

    public String getRightIdParam() throws RecognitionException, TokenStreamException {
        HttpServletRequest httpServletRequest = this.requestProvider.get();
        String parameter = httpServletRequest.getParameter(RIGHT_ID);
        return parameter;
    }
    
    public String getSecuredAction() {
        String securedActionString = this.requestProvider.get().getParameter("securedaction");
        return securedActionString;
    }

    protected static boolean hasSuperAdminRole(User user) {
        Role[] grps = user.getGroups();
        for (Role grp : grps) {
            if (grp.getPersonalAdminId() == 0) {
                return true;
            }
        }
        return false;
    }

}
