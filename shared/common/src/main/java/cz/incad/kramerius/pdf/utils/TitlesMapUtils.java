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
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import com.google.inject.Key;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.utils.DCUtils;

public class TitlesMapUtils {

    /**
     * Mapuje modely na uuid
     * @param fa FedoraAccess
     * @param path Cesta ke korenyu (brano z indexu)
     * @return Vraci mapu  uuid->model
     * @throws IOException
     * TODO: Zmenit, pouziva  deprecated api
     */
    public static Map<String, String> mapModels(FedoraAccess fa, String[] path) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        for (String u : path) {
            String modelName = fa.getKrameriusModelName(fa.getRelsExt(u));
            map.put(u, modelName);
        }
        return map;
    }

    /**
     * Mapuje tituly na uuid
     * @param fa FedoraAccess
     * @param path Cesta ke korenu
     * @return Vraci mapu uuid->model
     * @throws IOException
     */
    public static Map<String, String> mapTitlesToUUID(FedoraAccess fa, String[] path) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        for (String u : path) {
            Document dc = fa.getDC(u);
            String titleFromDC = DCUtils.titleFromDC(dc);
            map.put(u, titleFromDC);
        }
        return map;
    }
}
