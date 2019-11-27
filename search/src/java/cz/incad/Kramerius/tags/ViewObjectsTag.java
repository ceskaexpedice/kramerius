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
import javax.servlet.jsp.tagext.TagSupport;

import com.google.inject.Injector;

import cz.incad.kramerius.Initializable;

public class ViewObjectsTag extends TagSupport {

    private String clz;
    private String name;
    
    public String getClz() {
        return clz;
    }

    public void setClz(String clz) {
        this.clz = clz;
    }

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            Class clz = Class.forName(this.clz);
            if (clz != null) {
                Object obj = clz.newInstance();
                
                Injector inj = (Injector) pageContext.getServletContext().getAttribute(Injector.class.getName());
                inj.injectMembers(obj);
                if (obj instanceof Initializable) {
                    ((Initializable)obj).init();
                }
                pageContext.setAttribute(this.name, obj);
            } else {
                throw new RuntimeException("cannot find clz '"+clz+"'");
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
        return SKIP_BODY;
    }
    
    
}
