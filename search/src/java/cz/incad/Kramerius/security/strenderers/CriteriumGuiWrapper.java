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

import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.SecuredActions;

public class CriteriumGuiWrapper implements RightCriteriumWrapper{

    private RightCriteriumWrapper criteriumWrapper;

    public CriteriumGuiWrapper(RightCriteriumWrapper criterium) {
        super();
        this.criteriumWrapper = criterium;
    }

    public int getId() {
        if (this.criteriumWrapper != null) {
            return criteriumWrapper.getId();
        } else return -1;
    }


    public void setId(int id) {
        if (this.criteriumWrapper != null) {
            criteriumWrapper.setId(id);
        }
    }


    public int getCalculatedPriority() {
        if (this.criteriumWrapper != null) {
            return criteriumWrapper.getCalculatedPriority();
        } else return -1;
    }

    public void setCalculatedPriority(int priority) {
        criteriumWrapper.setCalculatedPriority(priority);
    }


    

    public RightCriteriumParams getCriteriumParams() {
        if (this.criteriumWrapper == null) return null;
        return criteriumWrapper.getCriteriumParams()!=null ? new CriteriumParamsWrapper(criteriumWrapper.getCriteriumParams()):null;
    }

    public void setCriteriumParams(RightCriteriumParams params) {
        criteriumWrapper.setCriteriumParams(params);
    }

    @Override
    public String toString() {
        if (this.criteriumWrapper == null) return "";
        return this.criteriumWrapper.getRightCriterium().getQName();
    }
    
    public static CriteriumGuiWrapper[] wrapCriteriums(List<RightCriteriumWrapper> criteriums, boolean withNull) {
        CriteriumGuiWrapper[] crits = wrapCriteriums(criteriums.toArray(new RightCriteriumWrapper[criteriums.size()]));
        CriteriumGuiWrapper[] retvalues = null;
        if (withNull) {
            retvalues = new CriteriumGuiWrapper[crits.length +1];
            System.arraycopy(crits, 0, retvalues, 1, crits.length);
            retvalues[0] = new CriteriumGuiWrapper(null);
        } else {
            retvalues = crits;
        }
        return retvalues;
    }
    
    public static CriteriumGuiWrapper[] wrapCriteriums(RightCriteriumWrapper...criteriums) {
        CriteriumGuiWrapper[] wrappers = new CriteriumGuiWrapper[criteriums.length];
        for (int i = 0; i < wrappers.length; i++) {
            wrappers[i]= new CriteriumGuiWrapper(criteriums[i]);
        }
        return wrappers;
    }

    @Override
    public RightCriterium getRightCriterium() {
        if (this.criteriumWrapper != null) {
            return this.criteriumWrapper.getRightCriterium();
        } else return null;
    }

    @Override
    public boolean isJustCreated() {
        if (this.criteriumWrapper != null) {
            return this.criteriumWrapper.isJustCreated();
        } else return true;
    }

    @Override
    public CriteriumType getCriteriumType() {
        return this.criteriumWrapper.getCriteriumType();
    }

    
}
