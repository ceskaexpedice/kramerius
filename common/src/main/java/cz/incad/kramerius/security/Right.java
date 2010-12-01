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
package cz.incad.kramerius.security;

/**
 * <p>
 * Represents association between user, action and object. <br>
 * If current user want to do some action, there must exists this association. 
 * </p>
 * 
 * <ul>
 *  <li> If this assocation doesn't exist, the user is not permitted to do this. 
 *  <li> If this assocation exists and doesn't exist any other RightParam, the user is permitted 
 * to do this action.
 *  <li> If this assoctiona exists and contains any other specific RightParam, the resolution is 
 *  delegeted the this param. Param can be script or java class.
 * </ul>  
 * 
 * @author pavels
 */
public interface Right {

    /**
     * UUID of the object
     * @return
     */
    public String getUUID();
    
    /**
     * Action for this right
     * @return
     */
    public String getAction();

    /**
     * Current logged user
     * @return
     */
    public AbstractUser getUser();
    
    /**
     * Returns specific right param
     * @return
     */
    public RightParam getParam();
    
    
    /**
     * Interpreting this right 
     * @param ctx
     * @return
     * @throws RightParamEvaluateContextException
     */
    public boolean evaluate(RightParamEvaluatingContext ctx) throws RightParamEvaluateContextException;

}
