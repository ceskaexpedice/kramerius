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
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;

public class ClassRightCriterium implements RightCriterium {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ClassRightCriterium.class.getName());
    
    private Class<? extends RightCriterium> clz;
    private RightCriteriumContext ctx;

    private int fixedPriority;
    private int calculatedPriority;
    private int rightId;
    
    private RightCriteriumParams rightCriteriumParams;
    
    public ClassRightCriterium(Class<? extends RightCriterium> class1, int rightId) {
        super();
        this.clz = class1;
        this.rightId = rightId;
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
        long start = System.currentTimeMillis();
        try {
            RightCriterium crit = instanceCriterium(this.clz);
            crit.setCriteriumParams(this.getCriteriumParams());
            crit.setEvaluateContext(getEvaluateContext());

            crit.setCalculatedPriority(this.calculatedPriority);
            crit.setCriteriumParams(getCriteriumParams());
            
            return crit.evalute();
        } catch (InstantiationException e) {
            throw new RightCriteriumException(e);
        } catch (IllegalAccessException e) {
            throw new RightCriteriumException(e);
        }finally {
            long stop = System.currentTimeMillis();
            LOGGER.info("criterium eval takes "+(stop - start)+" ms");
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
    public RightCriteriumPriorityHint getPriorityHint() {
        try {
            RightCriterium crit = instanceCriterium(this.clz);
            return crit.getPriorityHint();
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }
        return RightCriteriumPriorityHint.NORMAL;
    }

    public static RightCriterium instanceCriterium(Class<? extends RightCriterium> clz) throws InstantiationException, IllegalAccessException {
        RightCriterium crit = clz.newInstance();
        return crit;
    }

    
    public Class<? extends RightCriterium> getCriteriumClz() {
        return clz;
    }

//    @Override
//    public String toString() {
//        StringBuffer buffer = new StringBuffer();
//        buffer.append(this.clz.getName()+" "+Arrays.asList(this.objects));
//        return buffer.toString();
//    }


    @Override
    public int getId() {
        return this.rightId;
    }

    
    
    @Override
    public void setId(int id) {
        this.rightId = id;
    }

    @Override
    public RightCriteriumParams getCriteriumParams() {
        return this.rightCriteriumParams;
    }

    @Override
    public void setCriteriumParams(RightCriteriumParams params) {
        this.rightCriteriumParams = params;
    }

    @Override
    public String getQName() {
        return this.clz.getName();
    }

    @Override
    public boolean isParamsNecessary() {
        try {
            RightCriterium crit = instanceCriterium(this.clz);
            return crit.isParamsNecessary();
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }
        return true;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        try {
            RightCriterium crit = instanceCriterium(this.clz);
            return crit.getApplicableActions();
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }
        return new SecuredActions[0];
    }

    @Override
    public boolean validateParams(Object[] vals) {
        try {
            RightCriterium crit = instanceCriterium(this.clz);
            return crit.validateParams(vals);
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean validateParams(String encodedVals) {
        try {
            RightCriterium crit = instanceCriterium(this.clz);
            return crit.validateParams(encodedVals);
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }
        return true;
    }
}