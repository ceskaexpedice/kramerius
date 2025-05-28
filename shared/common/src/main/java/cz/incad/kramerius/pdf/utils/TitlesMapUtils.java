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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.w3c.dom.Document;

import cz.incad.kramerius.utils.DCUtils;

public class TitlesMapUtils {

    /**
     * Mapuje modely na uuid
     * @param path Cesta ke korenyu (brano z indexu)
     * @return Vraci mapu  uuid->model
     * @throws IOException
     * TODO: Zmenit, pouziva  deprecated api
     */
    public static Map<String, String> mapModels(AkubraRepository akubraRepository, String[] path) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        for (String u : path) {
            String modelName = akubraRepository.re().getModel(u);
            map.put(u, modelName);
        }
        return map;
    }

    /**
     * Mapuje tituly na uuid
     * @param path Cesta ke korenu
     * @return Vraci mapu uuid->model
     * @throws IOException
     */
    public static Map<String, String> mapTitlesToUUID(AkubraRepository akubraRepository, String[] path) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        for (String u : path) {
            Document dc = akubraRepository.getDatastreamContent(u, KnownDatastreams.BIBLIO_DC).asDom(false);
            String titleFromDC = DCUtils.titleFromDC(dc);
            map.put(u, titleFromDC);
        }
        return map;
    }
}
