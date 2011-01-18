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
 * @author pavels
 */
public interface Right {

    
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

    public void setAction(String action);
    
    /**
     * Current logged user
     * @return
     */
    public AbstractUser getUser();
    
    public void setUser(AbstractUser user);
    
    /**
     * Returns specific criterium
     * @return
     */
    public RightCriterium getCriterium();
    
    public void setCriterium(RightCriterium rightCriterium);
    
    
    /**
     * Interpret this right
     * 
     * @param ctx
     * @return
     * @throws RightCriteriumException
     */
    public EvaluatingResult evaluate(RightCriteriumContext ctx) throws RightCriteriumException;
    
    
    public void setFixedPriority(int priority);
    
    public int getFixedPriority();

}
