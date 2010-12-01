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

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import cz.incad.kramerius.security.RightParamEvaluateContextException;
import cz.incad.kramerius.security.RightParam;
import cz.incad.kramerius.security.RightParamEvaluatingContext;



class ScriptRightParam implements RightParam {
    
    private String scriptPath;
    private RightParamEvaluatingContext context;
    
    @Override
    public RightParamEvaluatingContext getEvaluateContext() {
        return this.context;
    }

    @Override
    public void setEvaluateContext(RightParamEvaluatingContext ctx) {
        this.context = ctx;
    }

    @Override
    public boolean evalute() throws RightParamEvaluateContextException {
        try {
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByExtension("js");
            Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("evelauateContext", getEvaluateContext());
            Object retVal = scriptEngine.eval((Reader)null);
            if (retVal != null) {
               if (retVal instanceof Boolean) {
                   return ((Boolean) retVal).booleanValue();
               } else throw new RightParamEvaluateContextException("script must return boolean value"); 
            } else throw new RightParamEvaluateContextException("no return value from script !");
        } catch (ScriptException e) {
            throw new RightParamEvaluateContextException(e);
        }
    }
}