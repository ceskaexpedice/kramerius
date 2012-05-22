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
package cz.incad.Kramerius.security.strenderers;

import java.util.Arrays;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightsManager;

public class CriteriumParamsWrapper implements RightCriteriumParams {

    private RightCriteriumParams criteriumParams;
    
    public CriteriumParamsWrapper(RightCriteriumParams criteriumParams) {
        super();
        this.criteriumParams = criteriumParams;
    }


    public int getId() {
        return criteriumParams.getId();
    }


    public void setId(int id) {
        criteriumParams.setId(id);
    }


    public String getObjectsToString() {
        StringTemplate template = new StringTemplate("$objects;separator=\";\"$");
        template.setAttribute("objects", getObjects());
        return template.toString();
    }
    

    public Object[] getObjects() {
        return criteriumParams.getObjects();
    }





    public void setObjects(Object[] objs) {
        criteriumParams.setObjects(objs);
    }





    public String getLongDescription() {
        return criteriumParams.getLongDescription();
    }





    public void setLongDescription(String longDesc) {
        criteriumParams.setLongDescription(longDesc);
    }




    public String getShortDescription() {
        return criteriumParams.getShortDescription();
    }





    public void setShortDescription(String desc) {
        criteriumParams.setShortDescription(desc);
    }


    @Override
    public String toString() {
        if ((criteriumParams != null) && (criteriumParams.getObjects() != null)) {
            return criteriumParams.getShortDescription() +Arrays.asList(criteriumParams.getObjects());
        } else return "";
    }

    
    public static CriteriumParamsWrapper[] wrapCriteriumParams(RightCriteriumParams...params) {
        CriteriumParamsWrapper[] wrappers = new CriteriumParamsWrapper[params.length];
        for (int i = 0; i < params.length; i++) {
            wrappers[i]= new CriteriumParamsWrapper(params[i]);
        }
        return wrappers;
    }

}
