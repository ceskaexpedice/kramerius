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


import cz.incad.kramerius.security.impl.criteria.CriteriaPrecoditionException;

import java.io.Serializable;

/**
 * Represents user defined right criterium ( defined in java in this case ). 
 * 
 * Implementation must resolve user request to secured resource and return one of trhee possible 
 * results TRUE, FALSE, NOT_APPLICABLE {@link EvaluatingResultState}.
 * 
 * @see RightCriteriumLoader
 * @author pavels
 */
public interface RightCriterium extends Serializable  {
    
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
    public EvaluatingResultState evalute() throws RightCriteriumException;


    /**
     * Perfrom evaluation without accessing title
     * @return
     * @throws RightCriteriumException
     * @param dataMockExpectation
     */
    public EvaluatingResultState mockEvaluate(DataMockExpectation dataMockExpectation) throws RightCriteriumException;

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


    /**
     * Validate given params
     * @param vals User defined parameters which should be validated
     * @return returns result of validation
     */
    public boolean validateParams(Object[] vals);

    /**
     * Validate params encoded in one string (used when the kramerius is about the loaded parametres from the database)
     * @param encodedVals
     * @return returns result of the validation
     */
    public boolean validateParams(String encodedVals);

    /**
     * Returns true if this criterium could be applied only on the root object
     * @return
     */
    public boolean isRootLevelCriterum();


    /**
     * Check criterium precondition.
     * This is the point where criterium can check whether certain precondition is applied.
     * For example:
     *      DNNT flag must be set with actions READ and PDF_RESOURCE.  The set with only one of them is not allowed.
     *      In that case the DNNT is responsible for checking this stuff in the implementation of this method
     *
     * @param manager
     * @throws CriteriaPrecoditionException
     */
    // mozna zmenit
    public void checkPrecodition(RightsManager manager) throws CriteriaPrecoditionException;

    //public boolean isBypassed();



}
