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

import cz.incad.kramerius.security.RightParamEvaluateContextException;
import cz.incad.kramerius.security.RightParam;
import cz.incad.kramerius.security.RightParamEvaluatingContext;

public class ClassRightParam implements RightParam {

    private Class<? extends RightParam> clz;
    private RightParamEvaluatingContext ctx;
    
    
    public ClassRightParam(Class<? extends RightParam> class1) {
        super();
        this.clz = class1;
    }

    @Override
    public RightParamEvaluatingContext getEvaluateContext() {
        return ctx;
    }

    @Override
    public void setEvaluateContext(RightParamEvaluatingContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean evalute() throws RightParamEvaluateContextException {
        try {
            RightParam param = clz.newInstance();
            param.setEvaluateContext(getEvaluateContext());
            return param.evalute();
        } catch (InstantiationException e) {
            throw new RightParamEvaluateContextException(e);
        } catch (IllegalAccessException e) {
            throw new RightParamEvaluateContextException(e);
        }
    }
}