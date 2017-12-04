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
 *  <li> If this assocation exists and doesn't exist any other Criterium, the user is permitted 
 * to do this action.
 *  <li> If this assoctiona exists and contains criterium, the resolution is delegated to this
 *  criterium
 * </ul>  
 * 
 */
public interface Right {

	/**
	 * 	Returns ID of right
	 * @return
	 */
    public int getId();
    
    /**
     * UUID of the object
     * @return
     */
    public String getPid();
    
    /**
     * Action for this right
     * @return
     */
    public String getAction();

    /**
     * Associate right to action
     * @param action
     */
    public void setAction(String action);
    
    /**
     * Returns assocaited user. It can be user or group.
     * @return
     */
    public AbstractUser getUser();
    
    /**
     * Associate user with right.
     * @param user
     */
    public void setUser(AbstractUser user);
    
    /**
     * Returns associated criterium.
     * @return
     */
    public RightCriteriumWrapper getCriteriumWrapper();
    
    /**
     * Associate new criterium with this right 
     * @param rightCriterium
     */
    public void setCriteriumWrapper(RightCriteriumWrapper rightCriterium);
    
    
    /**
     * Evaluate this right
     * @param ctx Evaluating context
     * @return
     * @throws RightCriteriumException
     */
    public EvaluatingResult evaluate(RightCriteriumContext ctx) throws RightCriteriumException;
    
    
    /**
     * Sets fixed priority
     * @param priority Priority of right
     */
    public void setFixedPriority(int priority);
    
    /**
     * Returns fixed priority
     * @return
     */
    public int getFixedPriority();

}
