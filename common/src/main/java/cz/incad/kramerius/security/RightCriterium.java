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
 * Represents specific right parameter. In most cases it is script or java class. 
 * 
 * This script parameter can have access to runtime variables such as MODS, DC, RELS-EXT etc. 
 * and can allow or disallow access to given object. 
 *
 * @author pavels
 */
public interface RightCriterium {
    
    public int getId();
    
    public String getQName();
    
    
    public void setId(int id);
    
    /**
     * Evaluating context. Context for access to runtime variables (uuid, current user, etc..)
     * @return
     */
    public RightCriteriumContext getEvaluateContext();
    
    /**
     * Set evaluating context
     * @param ctx
     */
    public void setEvaluateContext(RightCriteriumContext ctx);
    
    /**
     * Returns true, if the operation is allowed for current user
     * @return
     * @throws RightCriteriumException 
     */
    public EvaluatingResult evalute() throws RightCriteriumException;
    
    public int getCalculatedPriority();
    
    public void setCalculatedPriority(int priority);
    
    
    
    public RightCriteriumPriorityHint getPriorityHint();
    
    public RightCriteriumParams getCriteriumParams();
    
    public void setCriteriumParams(RightCriteriumParams params);
    
    public boolean isParamsNecessary();
}
