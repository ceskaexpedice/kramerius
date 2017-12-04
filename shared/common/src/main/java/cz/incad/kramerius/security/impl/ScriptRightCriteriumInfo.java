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

import java.io.File;

import javax.script.ScriptEngineManager;

import cz.incad.kramerius.security.RightCriteriumPriorityHint;


public class ScriptRightCriteriumInfo {

    public static String SECUREDACTIONS_GETTER ="getSecuredActions";
    public static String QNAME_GETTER="getQName";
    public static String PARAMS_NECESSARY_GETTER="isParamsNecessary";
    public static String PRIORITY_HINT_GETTER="getPriorityHint";
    public static String EVALUATE="evaluate";
    
    
    private File evalFile;
    private String[] securedActions;
    private String QName;
    private String scriptEngineName;
    private ScriptEngineManager scriptEngineManager;
    private boolean paramsIsNecessary;
    private RightCriteriumPriorityHint priorityHint;
    
    
    public ScriptRightCriteriumInfo(File evalFile, 
                                    String[] securedActions, 
                                    String qName, 
                                    RightCriteriumPriorityHint priorityHint, 
                                    boolean paramsIsNecessary, String scriptEngineName, ScriptEngineManager engineManager) {
        super();
        this.evalFile = evalFile;
        this.securedActions = securedActions;
        this.QName = qName;
        this.priorityHint = priorityHint;
        this.paramsIsNecessary = paramsIsNecessary;
        this.scriptEngineManager = engineManager;
        this.scriptEngineName = scriptEngineName;
    }


    public File getEvalFile() {
        return evalFile;
    }

    public String[] getSecuredActions() {
        return securedActions;
    }

    public String getQName() {
        return QName;
    }

    public RightCriteriumPriorityHint getPriorityHint() {
        return priorityHint;
    }

    public boolean isParamsNecessary() {
        return paramsIsNecessary;
    }

    public String getScriptEngineName() {
        return scriptEngineName;
    }


    public ScriptEngineManager getScriptEngineManager() {
        return scriptEngineManager;
    }
    
}
