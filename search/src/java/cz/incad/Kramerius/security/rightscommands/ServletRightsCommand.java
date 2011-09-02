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
package cz.incad.Kramerius.security.rightscommands;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import cz.incad.Kramerius.security.RightsServlet;
import cz.incad.Kramerius.security.ServletCommand;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.RightCriteriumParamsImpl;
import cz.incad.kramerius.security.impl.RightImpl;
import cz.incad.kramerius.utils.IOUtils;

public abstract class ServletRightsCommand extends ServletCommand {

    protected static boolean hasSuperAdminRole(User user) {
        Role[] grps = user.getGroups();
        for (Role grp : grps) {
            if (grp.getPersonalAdminId() == 0) {
                return true;
            }
        }
        return false;
    }
    
    protected static StringTemplateGroup stFormsGroup() throws IOException {
        InputStream stream = RightsServlet.class.getResourceAsStream("rights.stg");
        String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
        StringTemplateGroup group = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        return group;
    }

    protected static StringTemplateGroup stJSDataGroup() throws IOException {
        InputStream stream = RightsServlet.class.getResourceAsStream("rightsData.stg");
        String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
        StringTemplateGroup group = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        return group;
    }

    public RightImpl right(Map data, String pid) {
        RightCriteriumWrapper criterium = this.criteriumWrapperFactory.createCriteriumWrapper((String)data.get("condition"));
        if (criterium !=null)  criterium.setCriteriumParams(param(data));
        Role role = this.userManager.findRoleByName((String) data.get("role"));
        if (role == null) throw new RuntimeException("cannot find role '"+role+"'");

        String indexString = data.get("ident").toString();
        
        RightImpl right = new RightImpl(indexString !=null && !indexString.equals("")  ? Integer.parseInt(indexString) : -1, criterium, pid, (String)data.get("securedAction"), role);
        if ((data.get("priority") != null) && (Integer.parseInt((String)data.get("priority")) >0)) {
            right.setFixedPriority(Integer.parseInt((String)data.get("priority")));
        }
        return right;
    }

    public RightCriteriumParams param(Map data) {
            Map param = (Map) data.get("param");
            
            String id = (String) param.get("ident");
            String shortDsc = (String) param.get("shortDesc");
            List objects = (List)param.get("objects");
            if (objects != null &&  objects.size() > 0) {
                RightCriteriumParams params = null;
                if ((id != null) && (!id.equals("")) && (Integer.parseInt(id) > 0)) {
                    params = rightsManager.findParamById(Integer.parseInt(id));
                    params.setObjects(objects.toArray(new Object[objects.size()]));
                    params.setShortDescription(shortDsc);
                } else {
                    params = new RightCriteriumParamsImpl(-1);
                    params.setObjects(objects.toArray(new Object[objects.size()]));
                    params.setShortDescription(shortDsc);
                }
                return params;
            }  else return null;
            
        }


}
