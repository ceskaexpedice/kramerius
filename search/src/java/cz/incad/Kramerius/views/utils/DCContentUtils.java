/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.Kramerius.views.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.utils.DCUtils;

public class DCContentUtils {

    private static Map<String, DCConent> _CACHE = new HashMap<String, DCConent>();

    public static Map<String, List<DCConent>> getDCS(FedoraAccess fedoraAccess, SolrAccess solrAccess, List<String> pids) throws IOException {
        Map<String, List<DCConent>> maps = new HashMap<String, List<DCConent>>();
        for (String pid : pids) {
            ObjectPidsPath[] paths = solrAccess.getPath(pid);
            if (paths.length > 0) {
                List<DCConent> dcs = new ArrayList<DCConent>();
                String[] pathFromLeaf = paths[0].getPathFromLeafToRoot();
                for (int i = 0; i < pathFromLeaf.length; i++) {
                    String pidFromPath = pathFromLeaf[i];
                    if (!_CACHE.containsKey(pidFromPath)) {
                        Document dcl = fedoraAccess.getDC(pidFromPath);
                        DCConent content = DCUtils.contentFromDC(dcl);
                        _CACHE.put(pidFromPath, content);
                    }
                    dcs.add(_CACHE.get(pidFromPath));
                }
                maps.put(pid, dcs);
            }
        }
        return maps;
    }

}
