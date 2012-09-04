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
package cz.incad.kramerius.document.model.utils;

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
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.utils.DCUtils;

/**
 * DC content with simple cache
 * @author pavels
 */
public class DCContentUtils {

    static int _CACHE_SIZE = 500;
    static Map<String, DCConent> _CACHE = new HashMap<String, DCConent>();
    static List<String> _CACHE_HISTORY = new ArrayList<String>();
    
    /**
     * Creates map of {@link DCConent} chains
     * @param fedoraAccess FedoraAccess object
     * @param solrAccess SolrAccess object 
     * @param pids Input pids
     * @return map map of {@link DCConent} chains
     * @throws IOException IO error has been occurred
     */
    public static Map<String, List<DCConent>> getDCS(FedoraAccess fedoraAccess, SolrAccess solrAccess, List<String> pids) throws IOException {
        Map<String, List<DCConent>> maps = new HashMap<String, List<DCConent>>();
        for (String pid : pids) {
            ObjectPidsPath[] paths = solrAccess.getPath(pid);
            if (paths.length > 0) {
                List<DCConent> dcs = new ArrayList<DCConent>();
                String[] pathFromLeaf = paths[0].getPathFromLeafToRoot();
                for (int i = 0; i < pathFromLeaf.length; i++) {
                    String pidFromPath = pathFromLeaf[i];
                    if (!pidFromPath.equals(SpecialObjects.REPOSITORY.getPid()))  {
                        if (!cacheContains(pidFromPath)) {
                            Document dcl = fedoraAccess.getDC(pidFromPath);
                            DCConent content = DCUtils.contentFromDC(dcl);
                            putIntoCache(pidFromPath, content);
                        }
                        dcs.add(getFromCache(pidFromPath));
                    }
                }
                maps.put(pid, dcs);
            }
        }
        return maps;
    }

    static DCConent getFromCache(String pidFromPath) {
        return _CACHE.get(pidFromPath);
    }

    static boolean cacheContains(String pidFromPath) {
        return _CACHE.containsKey(pidFromPath);
    }

    static void putIntoCache(String pidFromPath, DCConent content) {
        if (_CACHE_HISTORY.size() >= _CACHE_SIZE) {
            while(_CACHE_HISTORY.size() >= _CACHE_SIZE) {
                removeOldestFromCache();
            }
        }
        _CACHE_HISTORY.add(pidFromPath);
        _CACHE.put(pidFromPath, content);
    }

    static void removeOldestFromCache() {
        _CACHE.remove(_CACHE_HISTORY.remove(0));
    }

}
