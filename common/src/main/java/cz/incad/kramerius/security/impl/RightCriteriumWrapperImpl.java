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

import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.SecuredActions;

public class RightCriteriumWrapperImpl implements RightCriteriumWrapper {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(RightCriteriumWrapperImpl.class.getName());

    private RightCriterium wrappedInstance;

    private int calculatedPriority;
    private int critId;
    private CriteriumType criteriumType;
    
    private RightCriteriumParams rightCriteriumParams;
    
    public RightCriteriumWrapperImpl(RightCriterium rightCriterium, int rightId, CriteriumType criteriumType) {
        super();
        this.wrappedInstance = rightCriterium;
        this.critId = rightId;
        this.criteriumType = criteriumType;
    }
    


    @Override
    public RightCriterium getRightCriterium() {
        return this.wrappedInstance;
    }



    @Override
    public int getCalculatedPriority() {
        return this.calculatedPriority;
    }

    @Override
    public void setCalculatedPriority(int priority) {
        this.calculatedPriority = priority;
    }

    public static RightCriterium instanceCriterium(Class<? extends RightCriterium> clz) throws InstantiationException, IllegalAccessException {
        RightCriterium crit = clz.newInstance();
        return crit;
    }

    

    @Override
    public int getId() {
        return this.critId;
    }

    
    
    @Override
    public void setId(int id) {
        this.critId = id;
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
    public boolean isJustCreated() {
        return this.critId == -1;
    }



    @Override
    public CriteriumType getCriteriumType() {
        return this.criteriumType;
    }
}