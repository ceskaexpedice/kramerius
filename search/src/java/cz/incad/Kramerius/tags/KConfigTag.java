/*
 * Copyright (C) 2011 Alberto Hernandez
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
import cz.incad.kramerius.utils.conf.KConfiguration;

public class KConfigTag extends TagSupport {

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }
    private String var;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    private String defaultValue;

    @Override
    public int doStartTag() throws JspException {
        KConfiguration config = KConfiguration.getInstance();
        if(this.defaultValue!=null){
            pageContext.setAttribute(this.var, config.getProperty(this.key, this.defaultValue));
        }else{
            pageContext.setAttribute(this.var, config.getProperty(this.key));
        }
        return SKIP_BODY;
    }
}
