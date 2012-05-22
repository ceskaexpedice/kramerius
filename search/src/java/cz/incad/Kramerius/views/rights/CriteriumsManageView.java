/*
 * Copyright (C) 2012 Pavel Stastny
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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.security.strenderers.CriteriumParamsWrapper;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.utils.ApplicationURL;


public class CriteriumsManageView extends AbstractRightsView {

    
    @Inject
    UserManager userManager;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    RightsManager rightsManager;

    @Inject
    Provider<HttpServletRequest> requestProvider;


    public _RightCriteriumParams[] getCriteriumParams() {
        RightCriteriumParams[] allParams = this.rightsManager.findAllParams();
        List<_RightCriteriumParams> list = new ArrayList<CriteriumsManageView._RightCriteriumParams>();
        for (RightCriteriumParams param : allParams) {
            List<Map<String, String>> objects = this.rightsManager.findObjectUsingParams(param.getId());
            

            list.add(new _RightCriteriumParams(objects,param));
        }
        return (_RightCriteriumParams[]) list.toArray(new _RightCriteriumParams[list.size()]);
    }
    
    
    public String getApplicationURL() {
        String appURL = ApplicationURL.applicationURL(this.requestProvider.get());
        return appURL;
    }

    
    /** criterium param item */
    public static class _RightCriteriumParams implements RightCriteriumParams {

        private RightCriteriumParams params = null;
        //private String[] usingObjects = null;
        private List<Map<String, String>> objects = null;
        
        public _RightCriteriumParams(List<Map<String, String>> objects, RightCriteriumParams params) {
            super();
            this.params = params;
            this.objects = objects;
        }

        public int getId() {
            return params.getId();
        }

        public Object[] getObjects() {
            return params.getObjects();
        }

        public void setObjects(Object[] objs) {
            params.setObjects(objs);
        }
        
        public String getObjectsString() {
            StringTemplate tmpl = new StringTemplate("$objects;separator=\";\"$");
            tmpl.setAttribute("objects", this.getObjects());
            return tmpl.toString();
        }
        

        public String getLongDescription() {
            return params.getLongDescription();
        }

        public void setLongDescription(String longDesc) {
            params.setLongDescription(longDesc);
        }

        public String getShortDescription() {
            return params.getShortDescription();
        }

        public void setShortDescription(String desc) {
            params.setShortDescription(desc);
        }

        public void setId(int id) {
            params.setId(id);
        }
        

        public List<String> getUsedPids() {
            List<String> pids = new ArrayList<String>();
            for (Map<String, String> str : this.objects) {
                pids.add(str.get("pid"));
            }
            return pids;
        }
        
        
        public String getUsingObjectsAsJSArray() {
            StringTemplate template = new StringTemplate("[ $objs:{obj| \\{'pid':'$obj.pid$', 'action':'$obj.action$'\\} };separator=\",\"$ ]");
            template.setAttribute("objs", this.objects);
            return template.toString();
        }
        
    }
}
