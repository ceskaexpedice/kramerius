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
 * This is internal class for wrapping real right criterium (class or script) 
 * which can have additional informations such as database id, etc..
 * 
 * @author pavels
 */
public interface RightCriteriumWrapper {

    /**
     * Wrapped right criterium
     */
    public RightCriterium getRightCriterium();
    
    /**
     * Returns db id of criterium
     * @return
     */
    public int getId();
    
    /**
     * Sets new id
     * @param id
     */
    public void setId(int id);

    /**
     * Returns real calculated priority
     * @return
     */
    public int getCalculatedPriority();
    
    /**
     * Sets new calculated priority
     * @param priority
     */
    public void setCalculatedPriority(int priority);

    
    /**
     * Returns params for this criterium
     * @see RightCriteriumParams
     * @return
     */
    public RightCriteriumParams getCriteriumParams();
    
    /**
     * Sets params for this criterium
     * @see RightCriteriumParams
     * @param params 
     */
    public void setCriteriumParams(RightCriteriumParams params);

    public boolean isJustCreated();

    public CriteriumType getCriteriumType();
}
