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

import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;



class ScriptRightCriterium implements RightCriterium {
    
    private String scriptPath;
    private RightCriteriumContext context;
    
    @Override
    public RightCriteriumContext getEvaluateContext() {
        return this.context;
    }

    @Override
    public void setEvaluateContext(RightCriteriumContext ctx) {
        this.context = ctx;
    }

    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
//        try {
//            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
//            ScriptEngine scriptEngine = scriptEngineManager.getEngineByExtension("js");
//            Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
//            bindings.put("evelauateContext", getEvaluateContext());
//            Object retVal = scriptEngine.eval((Reader)null);
//            if (retVal != null) {
//               if (retVal instanceof Boolean) {
//                   return ((Boolean) retVal).booleanValue();
//               } else throw new RightCriteriumException("script must return boolean value"); 
//            } else throw new RightCriteriumException("no return value from script !");
//        } catch (ScriptException e) {
//            throw new RightCriteriumException(e);
//        }
        throw new UnsupportedOperationException("");
    }

    @Override
    public int getCalculatedPriority() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setCalculatedPriority(int priority) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RightCriteriumParams getCriteriumParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCriteriumParams(RightCriteriumParams params) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setId(int id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getQName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isParamsNecessary() {
        // TODO Auto-generated method stub
        return false;
    }
    
    
    
}