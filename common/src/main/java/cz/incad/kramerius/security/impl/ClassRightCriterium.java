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

import java.util.logging.Level;

import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.EvaluatingResult;

public class ClassRightCriterium implements RightCriterium {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ClassRightCriterium.class.getName());
    
    private Class<? extends RightCriterium> clz;
    private RightCriteriumContext ctx;
    
    private Object[] objects;
    
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
}