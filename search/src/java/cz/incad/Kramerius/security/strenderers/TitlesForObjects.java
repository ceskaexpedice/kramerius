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
import java.util.ResourceBundle;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.utils.DCUtils;

public class TitlesForObjects {

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
            System.out.println(pathPid);
            
            List<String> subList = new ArrayList<String>(saturatedPath.subList(i, saturatedPath.size()));
            Collections.reverse(subList);
            for (String suuid : subList) {
                System.out.println("uuid is "+suuid);
                
            }
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
