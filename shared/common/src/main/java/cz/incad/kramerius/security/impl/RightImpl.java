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

import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.CriteriaPrecoditionException;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RightImpl implements Right, Serializable {

    private static final Logger LOGGER = Logger.getLogger(RightImpl.class.getName());

    private static final long serialVersionUID = 1L;

    private int rightId;
    private int fixedPriority;
    private RightCriteriumWrapper crit;
    private String pid;
    private String action;
    private Role role;
    
    public RightImpl(int rightId, RightCriteriumWrapper crit, String pid, String action, Role user) {
        super();
        this.rightId = rightId;
        this.crit = crit;
        this.pid = pid;
        this.action = action;
        this.role = user;
    }

    
    
    @Override
    public int getId() {
        return this.rightId;
    }



    @Override
    public String getPid() {
        //TODO: remove this test
        if (!this.pid.startsWith("uuid:") && !this.pid.startsWith("vc:")) {
            return "uuid:"+this.pid;
        } else return this.pid;
    }
    

    @Override
    public String getAction() {
        return this.action;
    }
    
    @Override
    public void setAction(String action) {
        this.action = action;        
    }


    @Override
    public RightCriteriumWrapper getCriteriumWrapper() {
        return this.crit;
    }
    
    

    @Override
    public void setCriteriumWrapper(RightCriteriumWrapper rightCriterium) {
        if (rightCriterium.getRightCriterium().isRootLevelCriterum()) {
            if (!this.pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                throw new IllegalArgumentException("criterium must be associated only with "+SpecialObjects.REPOSITORY);
            }
        }
        this.crit = rightCriterium;
    }



    public Role getRole() {
        return role;
    }
    
    @Override
    public void setRole(Role user) {
        this.role = user;
    }



    @Override 
    public synchronized EvaluatingResultState evaluate(RightCriteriumContext ctx, RightsManager rightsManager) throws RightCriteriumException {
        if (this.crit != null){
            RightCriterium rCrit = this.crit.getRightCriterium();
            rCrit.setEvaluateContext(ctx);
            if (this.crit.getCriteriumParams() != null) {
                rCrit.setCriteriumParamValues(this.crit.getCriteriumParams().getObjects());
            }

            checkPrecondition(rightsManager, rCrit);

            EvaluatingResultState result = rCrit.evalute(this);
            rCrit.setEvaluateContext(null);
            rCrit.setCriteriumParamValues(new Object[] {});
            return result;
        } else return EvaluatingResultState.TRUE;
    }


    @Override
    public EvaluatingResultState mockEvaluate(RightCriteriumContext ctx, RightsManager rightsManager, DataMockExpectation dataExpectation) throws RightCriteriumException {
        if (this.crit != null){
            RightCriterium rCrit = this.crit.getRightCriterium();
            rCrit.setEvaluateContext(ctx);
            if (this.crit.getCriteriumParams() != null) {
                rCrit.setCriteriumParamValues(this.crit.getCriteriumParams().getObjects());
            }

            checkPrecondition(rightsManager, rCrit);

            EvaluatingResultState result = rCrit.mockEvaluate(this, dataExpectation);
            rCrit.setEvaluateContext(null);
            rCrit.setCriteriumParamValues(new Object[] {});
            return result;
        } else return EvaluatingResultState.TRUE;
    }

    private void checkPrecondition(RightsManager rightsManager, RightCriterium rCrit) {
        try {
            // precondition
            rCrit.checkPrecodition(rightsManager);
        } catch (CriteriaPrecoditionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("["+this.pid+"] "+" ["+this.action+"] "+(this.crit!=null?this.crit.toString():""));
        return buffer.toString();
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((crit == null) ? 0 : crit.hashCode());
        result = prime * result + fixedPriority;
        result = prime * result + rightId;
        result = prime * result + ((pid == null) ? 0 : pid.hashCode());
        return result;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RightImpl other = (RightImpl) obj;
        if (action == null) {
            if (other.action != null)
                return false;
        } else if (!action.equals(other.action))
            return false;
        if (crit == null) {
            if (other.crit != null)
                return false;
        } else if (!crit.equals(other.crit))
            return false;
        if (fixedPriority != other.fixedPriority)
            return false;
        if (rightId != other.rightId)
            return false;
        if (pid == null) {
            if (other.pid != null)
                return false;
        } else if (!pid.equals(other.pid))
            return false;
        return true;
    }





    
    
    
}

