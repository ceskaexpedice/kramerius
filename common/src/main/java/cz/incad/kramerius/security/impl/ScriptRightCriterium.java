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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;



public class ScriptRightCriterium implements RightCriterium {
    
    private ScriptRightCriteriumInfo info;
    private RightCriteriumContext context;
    private Object[] params;
    
    public ScriptRightCriterium(ScriptRightCriteriumInfo info) {
        super();
        this.info= info;
    }

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
        try {
            ScriptEngineManager scriptEngineManager = this.info.getScriptEngineManager();
            String scriptEngineName = this.info.getScriptEngineName();
            ScriptEngine engine = scriptEngineManager.getEngineByName(scriptEngineName);

            engine.eval(new FileReader(this.info.getEvalFile()));
            
            Invocable inv = (Invocable) engine;
            Object criteriumObject = engine.get(ScriptCriteriumLoaderImpl.RIGHTCRITERIUM_OBJECT);
            
            Object retVal = inv.invokeMethod(criteriumObject, ScriptRightCriteriumInfo.EVALUATE , new Object[]{getEvaluateContext(), getCriteriumParamValues()});
            if (retVal != null) {
               if (retVal instanceof Number) {
                   EvaluatingResult result = 
                       EvaluatingResult.valueOf(((Number)retVal).intValue());
                   if (result != null) {
                       return result;
                   } else throw new RightCriteriumException("no result ..");
               } else throw new RightCriteriumException("expecting integer value"); 
            } else throw new RightCriteriumException("no return value of method !");
        } catch (ScriptException e) {
            throw new RightCriteriumException(e);
        } catch (NoSuchMethodException e) {
            throw new RightCriteriumException(e);
        } catch (FileNotFoundException e) {
            throw new RightCriteriumException(e);
        }
    }



    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return this.info.getPriorityHint();
    }


    @Override
    public Object[] getCriteriumParamValues() {
        return this.params;
    }

    @Override
    public void setCriteriumParamValues(Object[] params) {
        this.params = params;
    }


    @Override
    public String getQName() {
        return this.info.getQName();
    }

    @Override
    public boolean isParamsNecessary() {
        return this.info.isParamsNecessary();
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        List<SecuredActions> actions = new ArrayList<SecuredActions>();
        for (String act : this.info.getSecuredActions()) {
            actions.add(SecuredActions.findByFormalName(act));
        }
        return (SecuredActions[]) actions.toArray(new SecuredActions[actions.size()]);
    }

    @Override
    public boolean validateParams(Object[] vals) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean validateParams(String encodedVals) {
        // TODO Auto-generated method stub
        return false;
    }
    
    
}