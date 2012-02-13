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
package cz.incad.Kramerius.tags;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.LoggedUsersSingleton;

public class LoggedUsersContentTag extends BodyTagSupport {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LoggedUsersContentTag.class.getName());

    private BodyContent bodyContent;
    
    @Inject
    Provider<HttpServletRequest> provider;
    
    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Inject
    Provider<User> usersProvider;
    
    
    public BodyContent getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(BodyContent bodyContent) {
        this.bodyContent = bodyContent;
    }
    

    public int doStartTag() throws JspException {
        Injector inj = (Injector) pageContext.getServletContext().getAttribute(Injector.class.getName());
        inj.injectMembers(this);
        // store users key to session
        this.usersProvider.get();
        
        if (this.loggedUsersSingleton.isLoggedUser(this.provider)) {
            return EVAL_BODY_INCLUDE;
        } else {
            // No response code:
            //((HttpServletResponse)pageContext.getResponse()).setStatus(HttpServletResponse.SC_FORBIDDEN);
            return SKIP_BODY;
        }
    }

    
    @Override
    public int doAfterBody() throws JspException {
        try {
            if (bodyContent != null) {
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            }
        } catch (IOException pIOEx) {
            throw new JspException("Error: " + pIOEx.getMessage());
        }    
        return SKIP_BODY;
    }
}

