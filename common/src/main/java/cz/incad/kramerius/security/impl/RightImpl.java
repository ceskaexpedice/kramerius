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
    
    private RightCriterium crit;
    private String uuid;
    private String action;
    private AbstractUser user;
    
    public RightImpl(RightCriterium crit, String uuid, String action, AbstractUser user) {
        super();
        this.crit = crit;
        this.uuid = uuid;
        this.action = action;
        this.user = user;
    }

    @Override
    public String getUUID() {
        return this.uuid;
    }

    @Override
    public String getAction() {
        return this.action;
    }

    public void setParam(RightCriterium param) {
        this.crit = param;
    }

    @Override
    public RightCriterium getCriterium() {
        return this.crit;
    }

    public AbstractUser getUser() {
        return user;
    }
    
    
    @Override 
    public synchronized EvaluatingResult evaluate(RightCriteriumContext ctx) throws RightCriteriumException {
        if (this.crit != null){
            this.crit.setEvaluateContext(ctx);
            EvaluatingResult result = this.crit.evalute();
            this.crit.setEvaluateContext(null);
            return result;
        
        // kdyz neni zadne kriterium, pak je akce povolena
        } else return EvaluatingResult.FALSE;
    }

}

