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
package cz.incad.Kramerius.security.strenderers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class TitlesForObjects {

    public static HashMap<String, String> createModelsForPaths(FedoraAccess fedoraAccess,  ObjectPidsPath path, ResourceBundleService bundleService, Locale locale) throws IOException, LexerException {
        HashMap<String, String> modelsMap = new HashMap<String, String>();
        String[] pathFromRootToLeaf = path.getPathFromRootToLeaf();
        for (int i = 0; i < pathFromRootToLeaf.length; i++) {
            String currentPid = pathFromRootToLeaf[i];
            
            PIDParser pidParser = new PIDParser(currentPid);
            pidParser.objectPid();
            String displayedPid = pidParser.isDatastreamPid() ? pidParser.getParentObjectPid() : currentPid;
            String modelPostfix = pidParser.isDatastreamPid() ? "/"+pidParser.getDataStream() : "";

            
            
            if (SpecialObjects.findSpecialObject(currentPid) != null) {
                modelsMap.put(currentPid, SpecialObjects.findSpecialObject(displayedPid).name()+modelPostfix);
            } else {
                String kramModel = fedoraAccess.getKrameriusModelName(displayedPid);
                String localizedModel = bundleService.getResourceBundle("labels", locale).getString("document.type."+kramModel);
                modelsMap.put(currentPid, localizedModel+modelPostfix);
            }
        }
        return modelsMap;
    }
    
    
    public static HashMap<String, String> createTitlesForPaths(FedoraAccess fedoraAccess,  ObjectPidsPath path) throws IOException, LexerException {
        HashMap<String, String> dctitlesMap = new HashMap<String, String>();
        String[] pathFromRootToLeaf = path.getPathFromRootToLeaf();
        for (int i = 0; i < pathFromRootToLeaf.length; i++) {
            String currentPid = pathFromRootToLeaf[i];
            PIDParser pidParser = new PIDParser(currentPid);
            pidParser.objectPid();
            
            String titlePostfix = pidParser.isDatastreamPid() ? "/"+pidParser.getDataStream() : "";
            
            String pidForTitle = pidParser.isDatastreamPid() ? pidParser.getParentObjectPid() : currentPid;
            if (SpecialObjects.findSpecialObject(currentPid) != null) {
                dctitlesMap.put(currentPid, SpecialObjects.findSpecialObject(pidForTitle).name()+titlePostfix);
            } else {
                String titleFromDC = DCUtils.titleFromDC(fedoraAccess.getDC(pidForTitle));
                /*
                if (titleFromDC.length() > 10) {
                    titleFromDC = titleFromDC.substring(0,10)+"...";
                }*/
                dctitlesMap.put(pathFromRootToLeaf[i], titleFromDC+titlePostfix);
            }
        }
        return dctitlesMap;
    }
    

    
    public static HashMap<String, String> createFinerTitles(FedoraAccess fedoraAccess, RightsManager rightsManager, String uuid, String[] path, String[] models, ResourceBundle bundle) throws IOException {
        List<String> saturatedPath = rightsManager.saturatePathAndCreatesPIDs(uuid, path);
    

        HashMap<String, String> dctitlesMap = new HashMap<String, String>();
        HashMap<String, String> modelsMap = new HashMap<String, String>();
        for (int i = 0,ll=path.length; i < ll; i++) {
            String pathUuid = path[i];
            String model = models[i];
            String titleFromDC = DCUtils.titleFromDC(fedoraAccess.getDC(pathUuid));
            if (titleFromDC.length() > 10) {
                titleFromDC = titleFromDC.substring(0,10)+"...";
            }
            dctitlesMap.put("uuid:"+pathUuid, titleFromDC);
            modelsMap.put("uuid:"+pathUuid, "("+bundle.getString("document.type."+model)+")");
        }
        dctitlesMap.put("uuid:"+SpecialObjects.REPOSITORY.getUuid(), SpecialObjects.REPOSITORY.name());
        modelsMap.put("uuid:"+SpecialObjects.REPOSITORY.getUuid(), "");
        //Collections.reverse(saturatedPath);
        
        
        HashMap<String, String>titlesMap = new HashMap<String, String>();
        for (int i = 0,ll=saturatedPath.size(); i < ll; i++) {
            String pathPid = saturatedPath.get(i);
            
            List<String> subList = new ArrayList<String>(saturatedPath.subList(i, saturatedPath.size()));
            Collections.reverse(subList);
            StringTemplate template = new StringTemplate("$saturatedPath:{uuid|$dctitlesMap.(uuid)$};separator=\"->\"$ $models.(currentPid)$");
            template.setAttribute("dctitlesMap", dctitlesMap);
            template.setAttribute("models", modelsMap);
            
            template.setAttribute("saturatedPath", subList);
            template.setAttribute("currentPid", pathPid);
            String title = template.toString();
            titlesMap.put(pathPid, title);
        }
        return titlesMap;
    }

}
