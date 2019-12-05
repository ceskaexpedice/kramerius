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
 * User defined parameters (raw values) for criterium. 
 */
public interface RightCriteriumParams {

    /**
     * Returns id of criterium
     * @return
     */
    public int getId();
    
    /**
     * Returns array of raw values
     * @return
     */
    public Object[] getObjects();
    
    /**
     * Sets new array of raw values
     * @param objs
     */
    public void setObjects(Object[] objs);

    /**
     * Returns long description of parameters
     * @return
     */
    public String getLongDescription();
    
    /**
     * Sets long description of parameters
     * @param longDesc
     */
    public void setLongDescription(String longDesc);
    
    /**
     * Returns short description of parameters
     * @return
     */
    public String getShortDescription();
    
    /**
     * Sets short description of parameters
     * @param desc
     */
    public void setShortDescription(String desc);
    
    //TODO: Change it
    public void setId(int id);

}
