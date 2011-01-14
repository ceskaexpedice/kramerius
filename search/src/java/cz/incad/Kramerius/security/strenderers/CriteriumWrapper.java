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

import java.util.List;

import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;

public class CriteriumWrapper implements RightCriterium{

    private RightCriterium criterium;

    public CriteriumWrapper(RightCriterium criterium) {
        super();
        this.criterium = criterium;
    }

    public int getId() {
        return criterium.getId();
    }

    public String getQName() {
        return criterium.getQName();
    }

    public void setId(int id) {
        criterium.setId(id);
    }

    public RightCriteriumContext getEvaluateContext() {
        return criterium.getEvaluateContext();
    }

    public void setEvaluateContext(RightCriteriumContext ctx) {
        criterium.setEvaluateContext(ctx);
    }

    public EvaluatingResult evalute() throws RightCriteriumException {
        return criterium.evalute();
    }

    public int getCalculatedPriority() {
        return criterium.getCalculatedPriority();
    }

    public void setCalculatedPriority(int priority) {
        criterium.setCalculatedPriority(priority);
    }

    public void setFixedPriority(int priority) {
        criterium.setFixedPriority(priority);
    }

    public int getFixedPriority() {
        return criterium.getFixedPriority();
    }

    public String getFixedPriorityName() {
        if (criterium.getFixedPriority() == 0) return "";
        else return ""+criterium.getFixedPriority();
    }
    
    public RightCriteriumPriorityHint getPriorityHint() {
        return criterium.getPriorityHint();
    }

    public RightCriteriumParams getCriteriumParams() {
        return criterium.getCriteriumParams()!=null ? new CriteriumParamsWrapper(criterium.getCriteriumParams()):null;
    }

    public void setCriteriumParams(RightCriteriumParams params) {
        criterium.setCriteriumParams(params);
    }

    public boolean isParamsNecessary() {
        return criterium.isParamsNecessary();
    }
    
    
    @Override
    public String toString() {
        if (this.criterium == null) return "";
        return getQName();
    }
    
    public static CriteriumWrapper[] wrapCriteriums(List<RightCriterium> criteriums) {
        return wrapCriteriums(criteriums.toArray(new RightCriterium[criteriums.size()]));
    }
    
    public static CriteriumWrapper[] wrapCriteriums(RightCriterium...criteriums) {
        CriteriumWrapper[] wrappers = new CriteriumWrapper[criteriums.length];
        for (int i = 0; i < wrappers.length; i++) {
            wrappers[i]= new CriteriumWrapper(criteriums[i]);
        }
        return wrappers;
    }

}
