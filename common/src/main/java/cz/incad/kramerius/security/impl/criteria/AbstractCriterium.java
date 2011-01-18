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

public abstract class AbstractCriterium implements RightCriterium {

    protected int id=-1;
    protected int fixedPriority;
    protected int calculatedPriority;
    protected RightCriteriumContext evalContext;
    protected RightCriteriumParams criteriumParams;

    
    
    @Override
    public RightCriteriumContext getEvaluateContext() {
        return this.evalContext;
    }

    @Override
    public void setEvaluateContext(RightCriteriumContext ctx) {
        this.evalContext = ctx;
    }

    @Override
    public int getCalculatedPriority() {
        return this.calculatedPriority;
    }

    @Override
    public void setCalculatedPriority(int priority) {
        this.calculatedPriority = priority;
    }


    @Override
    public RightCriteriumParams getCriteriumParams() {
        return this.criteriumParams;
    }

    @Override
    public void setCriteriumParams(RightCriteriumParams params) {
        this.criteriumParams = params;
    }

    @Override
    public int getId() {
        return this.id;
    }
    
    @Override
    public void setId(int id) {
        this.id = id;
    }

    public Object[] getObjects() {
        return this.criteriumParams != null ? this.criteriumParams.getObjects() : new Object[0];
    }

    @Override
    public String getQName() {
        return this.getClass().getName();
    }
}
