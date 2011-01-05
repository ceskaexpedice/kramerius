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
package cz.incad.kramerius.security.impl;

import java.util.Arrays;
import java.util.logging.Level;

import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;

public class ClassRightCriterium implements RightCriterium {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ClassRightCriterium.class.getName());
    
    private Class<? extends RightCriterium> clz;
    private RightCriteriumContext ctx;
    
    private Object[] objects;

    private int fixedPriority;
    private int calculatedPriority;
    
    public ClassRightCriterium(Class<? extends RightCriterium> class1) {
        super();
        this.clz = class1;
    }

    @Override
    public RightCriteriumContext getEvaluateContext() {
        return ctx;
    }

    @Override
    public void setEvaluateContext(RightCriteriumContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        try {
            RightCriterium crit = clz.newInstance();
            crit.setObjects(getObjects());
            crit.setEvaluateContext(getEvaluateContext());
            
            crit.setFixedPriority(this.fixedPriority);
            crit.setCalculatedPriority(this.calculatedPriority);
            
            return crit.evalute();
        } catch (InstantiationException e) {
            throw new RightCriteriumException(e);
        } catch (IllegalAccessException e) {
            throw new RightCriteriumException(e);
        }
    }

    @Override
    public Object[] getObjects() {
        return this.objects;
    }

    @Override
    public void setObjects(Object[] objs) {
        this.objects = objs;
    }

    @Override
    public boolean validate(Object[] objs) {
        try {
            RightCriterium crit = clz.newInstance();
            return crit.validate(objs);
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return false;
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
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

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        try {
            RightCriterium crit = clz.newInstance();
            crit.setObjects(getObjects());
            crit.setEvaluateContext(getEvaluateContext());
            return crit.getPriorityHint();
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }
        return RightCriteriumPriorityHint.NORMAL;
    }

    
    public Class<? extends RightCriterium> getCriteriumClz() {
        return clz;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.clz.getName()+" "+Arrays.asList(this.objects));
        return buffer.toString();
    }
    
    
}