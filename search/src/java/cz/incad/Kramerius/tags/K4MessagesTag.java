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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.taglibs.standard.tag.common.fmt.MessageSupport;
import org.apache.taglibs.standard.tag.rt.fmt.MessageTag;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class K4MessagesTag extends MessageTag {

    @Inject
    LocalizationContext lctx;
        
    @Override
    public int doStartTag() throws JspException {
        Injector inj = (Injector) pageContext.getServletContext().getAttribute(Injector.class.getName());
        inj.injectMembers(this);

        this.setBundle(lctx);
        
        return super.doStartTag();
    }
}
