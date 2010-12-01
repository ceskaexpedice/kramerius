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
import cz.incad.kramerius.security.RightParam;
import cz.incad.kramerius.security.RightParamEvaluateContextException;
import cz.incad.kramerius.security.RightParamEvaluatingContext;

public class RightImpl implements Right {
    
    
    private RightParam param;
    private String uuid;
    private String action;
    private AbstractUser user;
    
    public RightImpl(RightParam param, String uuid, String action, AbstractUser user) {
        super();
        this.param = param;
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

    public void setParam(RightParam param) {
        this.param = param;
    }

    @Override
    public RightParam getParam() {
        return this.param;
    }

    public AbstractUser getUser() {
        return user;
    }
    
    
    @Override 
    public synchronized boolean evaluate(RightParamEvaluatingContext ctx) throws RightParamEvaluateContextException {
        if (this.param != null){
            this.param.setEvaluateContext(ctx);
            boolean evaluted = this.param.evalute();
            this.param.setEvaluateContext(null);
            return evaluted;
        } else return true;
    }

}

