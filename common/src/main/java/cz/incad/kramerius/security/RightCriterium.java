/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * `
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.security;

import java.util.Map;


/**
 * Represents user defined right criterium ( defined in java in this case ). 
 * 
 * Implementation must resolve user request to secured resource and return one of trhee possible 
 * results TRUE, FALSE, NOT_APPLICABLE {@link EvaluatingResult}.  
 * 
 * @see RightCriteriumLoader
 * @author pavels
 */
public interface RightCriterium {
    
    /**
     * Returns unique name of criterium.  
     * @return
     */
    public String getQName();
    /**
     * Evaluating context. 
     * Context for access to runtime variables (uuid, current user, etc..)
     * @return
     */
    public RightCriteriumContext getEvaluateContext();
    
    /**
     * Set evaluating context
     * @param ctx
     */
    public void setEvaluateContext(RightCriteriumContext ctx);
    
    /**
     * Answer the question whether given user can access to requesting resource
     * @return Result of evaluation
     * @throws RightCriteriumException Something happen during evaluate
     */
    public EvaluatingResult evalute() throws RightCriteriumException;
    

    /**
     * Returns criterium hint. 
     * @see RightCriteriumPriorityHint
     */
    public RightCriteriumPriorityHint getPriorityHint();

    
    
    /**
     * Returns params for this criterium
     * @see RightCriteriumParams
     * @return
     */
    public Object[] getCriteriumParamValues();
    
    /**
     * Sets params for this criterium
     * @see RightCriteriumParams
     * @param params 
     */
    public void setCriteriumParamValues(Object[] params);
    
    /**
     * Returns true if params is necessary
     * @return
     */
    public boolean isParamsNecessary();
    
    /**
     * Returns all applicaable actions
     * @return
     */
    public SecuredActions[] getApplicableActions();

    
    public boolean validateParams(Object[] vals);

    public boolean validateParams(String encodedVals);
}
