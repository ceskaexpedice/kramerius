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
        if (this.criterium != null) {
            return criterium.getId();
        } else return -1;
    }

    public String getQName() {
        if (this.criterium != null) {
            return criterium.getQName();
        } else return null;
    }

    public void setId(int id) {
        if (this.criterium != null) {
            criterium.setId(id);
        }
    }

    public RightCriteriumContext getEvaluateContext() {
        if (this.criterium != null) {
            return criterium.getEvaluateContext();
        } else return null;
    }

    public void setEvaluateContext(RightCriteriumContext ctx) {
        criterium.setEvaluateContext(ctx);
    }

    public EvaluatingResult evalute() throws RightCriteriumException {
        if (this.criterium != null) {
            return criterium.evalute();
        } else return null;
    }

    public int getCalculatedPriority() {
        if (this.criterium != null) {
            return criterium.getCalculatedPriority();
        } else return -1;
    }

    public void setCalculatedPriority(int priority) {
        criterium.setCalculatedPriority(priority);
    }

    public void setFixedPriority(int priority) {
        criterium.setFixedPriority(priority);
    }

    public int getFixedPriority() {
        if (this.criterium != null) {
            return criterium.getFixedPriority();
        } else {
            return 0;
        }
    }

    public String getFixedPriorityName() {
        if (criterium.getFixedPriority() == 0) return "";
        else return ""+criterium.getFixedPriority();
    }
    
    public RightCriteriumPriorityHint getPriorityHint() {
        if (this.criterium != null) {
            return criterium.getPriorityHint();
        } else {
            return null;
        }
    }

    public RightCriteriumParams getCriteriumParams() {
        if (this.criterium == null) return null;
        return criterium.getCriteriumParams()!=null ? new CriteriumParamsWrapper(criterium.getCriteriumParams()):null;
    }

    public void setCriteriumParams(RightCriteriumParams params) {
        criterium.setCriteriumParams(params);
    }

    public boolean isParamsNecessary() {
        if (this.criterium == null) return false;
        return criterium.isParamsNecessary();
    }
    
    
    @Override
    public String toString() {
        if (this.criterium == null) return "";
        return getQName();
    }
    
    public static CriteriumWrapper[] wrapCriteriums(List<RightCriterium> criteriums, boolean withNull) {
        CriteriumWrapper[] crits = wrapCriteriums(criteriums.toArray(new RightCriterium[criteriums.size()]));
        CriteriumWrapper[] retvalues = null;
        if (withNull) {
            retvalues = new CriteriumWrapper[crits.length +1];
            System.arraycopy(crits, 0, retvalues, 1, crits.length);
            retvalues[0] = new CriteriumWrapper(null);
        } else {
            retvalues = crits;
        }
        return retvalues;
    }
    
    public static CriteriumWrapper[] wrapCriteriums(RightCriterium...criteriums) {
        CriteriumWrapper[] wrappers = new CriteriumWrapper[criteriums.length];
        for (int i = 0; i < wrappers.length; i++) {
            wrappers[i]= new CriteriumWrapper(criteriums[i]);
        }
        return wrappers;
    }

}
