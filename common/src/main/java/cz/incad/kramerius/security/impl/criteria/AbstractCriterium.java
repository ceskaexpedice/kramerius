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

public abstract class AbstractCriterium implements RightCriterium {

    protected RightCriteriumContext evalContext;
    protected Object[] objs;
    private int fixedPriority;
    private int calculatedPriority;
    
    @Override
    public RightCriteriumContext getEvaluateContext() {
        return this.evalContext;
    }

    @Override
    public void setEvaluateContext(RightCriteriumContext ctx) {
        this.evalContext = ctx;
    }

    @Override
    public Object[] getObjects() {
        return this.objs;
    }

    @Override
    public void setObjects(Object[] objs) {
        this.objs = objs;
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
    public void setFixedPriority(int priority) {
        this.fixedPriority = priority;
    }

    @Override
    public int getFixedPriority() {
        return this.fixedPriority;
    }
}
