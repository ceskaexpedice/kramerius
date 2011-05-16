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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.aopalliance.intercept.Invocation;

import com.google.inject.Inject;

import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumLoader;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ScriptCriteriumLoaderImpl implements RightCriteriumLoader {

    java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ScriptCriteriumLoaderImpl.class.getName());
    
    private static String INIT_SCRIPT_FILE_PREFIX ="init";
    private static String EVAL_SCRIPT_FILE_PREFIX ="eval";

    public static String RIGHTCRITERIUM_OBJECT = "rightCriterium";
    public static String INITIALIZER_OBJECT = "initializer";
    
    
    private KConfiguration kConfiguration;
    private List<ScriptRightCriteriumInfo> infos = new ArrayList<ScriptRightCriteriumInfo>();
    private ScriptEngineManager  scriptEngineManager =  new ScriptEngineManager();

    @Inject
    public ScriptCriteriumLoaderImpl(KConfiguration configuration) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        super();
        this.kConfiguration = configuration;
        this.initAllEngines();
    }

    private void initAllEngines() throws FileNotFoundException, ScriptException, NoSuchMethodException {
        String critDirName = kConfiguration.getRightsCriteriumScriptsDir();
        File critDir = new File(critDirName);

        File[] listFiles = critDir.listFiles();
        if ((listFiles != null) && (listFiles.length > 0)) {
            for (File dir : listFiles) {
                if (dir.isDirectory()) {
                    initScriptsForOneEngine(dir, dir.getName());
                }
            }
        }
    }

    private void initScriptsForOneCriterium(File criteriumDir, ScriptEngine engine, String engineName) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        
        File[] listFiles = criteriumDir.listFiles();
        if (listFiles != null) {
            File evaluationScript = null;

            for (File scriptFile : listFiles) {
                if (scriptFile.isFile()) {
                    if (scriptFile.getName().startsWith(EVAL_SCRIPT_FILE_PREFIX)) {
                        evaluationScript = scriptFile;
                    }
                }
            }
            if (evaluationScript != null) {
                engine.eval(new FileReader(evaluationScript));
                Invocable inv = (Invocable) engine;

                Object initializerObject = engine.get(INITIALIZER_OBJECT);

                String qName= (String) inv.invokeMethod(initializerObject, ScriptRightCriteriumInfo.QNAME_GETTER , new Object[0]);
                
                String[] actions= (String[]) inv.invokeMethod(initializerObject, ScriptRightCriteriumInfo.SECUREDACTIONS_GETTER , new Object[0]);
                String priorityHint= (String) inv.invokeMethod(initializerObject, ScriptRightCriteriumInfo.PRIORITY_HINT_GETTER , new Object[0]);
                Object object = inv.invokeMethod(initializerObject, ScriptRightCriteriumInfo.PARAMS_NECESSARY_GETTER , new Object[0]);
                Boolean paramsNecessary = (Boolean) object;
                
                ScriptRightCriteriumInfo info = new ScriptRightCriteriumInfo(evaluationScript,  actions, qName, RightCriteriumPriorityHint.valueOf(priorityHint), paramsNecessary, engineName, this.scriptEngineManager);
                this.infos.add(info);
            }
        }
        
    }
    
    private void initScriptsForOneEngine(File dir, String engineName) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        ScriptEngine engine = this.scriptEngineManager.getEngineByName(engineName);
        File[] listFiles = dir.listFiles();
        for (File file : listFiles) {
            if (file.isDirectory()) {
                initScriptsForOneCriterium(file, engine, engineName);
            }
        }
    }

    @Override
    public List<RightCriterium> getCriteriums() {
        List<RightCriterium> crits = new ArrayList<RightCriterium>();
        for (ScriptRightCriteriumInfo info : this.infos) {
            crits.add(new ScriptRightCriterium(info));
        }
        return crits;
    }

    @Override
    public List<RightCriterium> getCriteriums(SecuredActions... applActions) {
        List<SecuredActions> appActionsAsList = Arrays.asList(applActions);
        List<RightCriterium> crits = new ArrayList<RightCriterium>();
        for (ScriptRightCriteriumInfo info : this.infos) {
            String[] securedActions = info.getSecuredActions();
            for (String secActString : securedActions) {
                SecuredActions secAct = SecuredActions.findByFormalName(secActString);
                if (Collections.frequency(appActionsAsList, secAct) > 0) {
                    crits.add(new ScriptRightCriterium(info));
                }
            }
        }

        return new ArrayList<RightCriterium>();
    }


    @Override   
    public RightCriterium createCriterium(String criteriumQName) {
        for (ScriptRightCriteriumInfo info : this.infos) {
            if (info.getQName().equals(criteriumQName)) {
                return new ScriptRightCriterium(info);
            }
            
        }
        return null;
    }


    @Override
    public boolean isDefined(String qname) {
        for (ScriptRightCriteriumInfo info : this.infos) {
            if (info.getQName().equals(qname)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CriteriumType getCriteriumType() {
        CriteriumType returningType = CriteriumType.SCRIPT;
        return returningType;
    }
}
