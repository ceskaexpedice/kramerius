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

import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.EvaluatingResult;

public class RightImpl implements Right {
    
    private int rightId;
    private int fixedPriority;
    private RightCriterium crit;
    private String uuid;
    private String action;
    private AbstractUser user;
    
    public RightImpl(int rightId, RightCriterium crit, String uuid, String action, AbstractUser user) {
        super();
        this.rightId = rightId;
        this.crit = crit;
        this.uuid = uuid;
        this.action = action;
        this.user = user;
    }

    
    
    @Override
    public int getId() {
        return this.rightId;
    }



    @Override
    public String getPid() {
        if (!this.uuid.startsWith("uuid:")) {
            return "uuid:"+this.uuid;
        } else return this.uuid;
    }
    

    @Override
    public String getAction() {
        return this.action;
    }
    
    @Override
    public void setAction(String action) {
        this.action = action;        
    }

    public void setParam(RightCriterium param) {
        this.crit = param;
    }

    @Override
    public RightCriterium getCriterium() {
        return this.crit;
    }
    
    

    @Override
    public void setCriterium(RightCriterium rightCriterium) {
        this.crit = rightCriterium;
    }



    public AbstractUser getUser() {
        return user;
    }
    
    @Override
    public void setUser(AbstractUser user) {
        this.user = user;
    }



    @Override 
    public synchronized EvaluatingResult evaluate(RightCriteriumContext ctx) throws RightCriteriumException {
        if (this.crit != null){
            this.crit.setEvaluateContext(ctx);
            EvaluatingResult result = this.crit.evalute();
            this.crit.setEvaluateContext(null);
            return result;
        // kdyz neni zadne kriterium, pak je akce povolena
        } else return EvaluatingResult.TRUE;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("["+this.uuid+"] "+" ["+this.action+"] "+(this.crit!=null?this.crit.toString():""));
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
}

