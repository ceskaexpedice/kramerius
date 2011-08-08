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
package cz.incad.kramerius.pdf.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;

public class TitlesUtils {

    public static String title(String uuid, SolrAccess solrAccess, FedoraAccess fa) throws IOException {
        return title(uuid, solrAccess, fa, true);
    }
        
    
    public static String title(String pid, SolrAccess solrAccess, FedoraAccess fa, boolean renderModel) throws IOException {
        ObjectPidsPath[] paths = solrAccess.getPath(pid);
        
        String[] path = paths[0].getPathFromRootToLeaf();
        Map<String, String> mapModels = TitlesMapUtils.mapModels(fa, path);
        Map<String, String> mapTitlesToUUID = TitlesMapUtils.mapTitlesToUUID(fa, path);
        List<String> titles = new ArrayList<String>();
        for (int i = 0; i < path.length; i++) {
            String u = path[i];
            String title = mapTitlesToUUID.get(u);
            if (titles.contains(title)) {
                title = "...";
            }
            if (i == path.length -1) {
                title = title + (renderModel ? " ("+mapModels.get(u)+")":"");
            }
            titles.add(title);
        }
        
        StringTemplate template = new StringTemplate("$titles;separator=\"->\"$");
        template.setAttribute("titles", titles);
        String calculatedTitle = template.toString();
        return calculatedTitle;
    }

}
