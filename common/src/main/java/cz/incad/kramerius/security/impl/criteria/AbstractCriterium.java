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
package cz.incad.kramerius.security.impl.criteria;

import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.utils.RightsDBUtils;

public abstract class AbstractCriterium implements RightCriterium {

    protected RightCriteriumContext evalContext;
    protected Object[] params;

    
    @Override
    public RightCriteriumContext getEvaluateContext() {
        return this.evalContext;
    }

    @Override
    public void setEvaluateContext(RightCriteriumContext ctx) {
        this.evalContext = ctx;
    }


    @Override
    public Object[] getCriteriumParamValues() {
        return this.params;
    }

    @Override
    public void setCriteriumParamValues(Object[] params) {
        this.params = params;
    }

    public Object[] getObjects() {
        return this.params != null ? this.params : new Object[0];
    }

    @Override
    public String getQName() {
        return this.getClass().getName();
    }

    @Override
    public boolean validateParams(Object[] vals) {
        return true;
    }

    @Override
    public boolean validateParams(String encodedVals) {
        return validateParams(RightsDBUtils.valsFromString(encodedVals));
    }
}

