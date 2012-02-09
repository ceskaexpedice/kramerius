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
package cz.incad.Kramerius.users;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import cz.incad.Kramerius.backend.guice.LocalesProvider;

import net.sf.json.JSONObject;

public class ProfilePrepareUtils {

    public static final String PREPARING_PROFILE_KEY = "PREPARING_PROFILE";

    public static Map<String, String> getPreparedProperties(HttpSession session) {
        return (Map<String, String>) session.getAttribute(PREPARING_PROFILE_KEY);
    }

    public static void prepareProperty(HttpSession session, String key, String fpar) {
        // TODO: synchronizace 
        Map<String, String> preparedMap = (Map<String, String>) session.getAttribute(PREPARING_PROFILE_KEY);
        if (preparedMap == null) {
            preparedMap = new HashMap<String, String>();
            session.setAttribute(PREPARING_PROFILE_KEY, preparedMap);
        }
        preparedMap.put(key, fpar);
    }

    
    public static void prepareProperitesFromProfile(JSONObject jsonObject, HttpSession session) {
        if (jsonObject.containsKey("client_locale")) {
            ProfilePrepareUtils.prepareProperty(session,"client_locale",jsonObject.getString("client_locale"));
        }

        JSONObject jsonResults = jsonObject.getJSONObject("results");
        if (jsonResults != null) {
            if (jsonResults.containsKey("sorting_dir")) {
                String sortingDir = jsonResults.getString("sorting_dir");
                ProfilePrepareUtils.prepareProperty(session,"sorting_dir",sortingDir);
            }
            if (jsonResults.containsKey("columns")) {
                String columns = jsonResults.getString("columns");
                ProfilePrepareUtils.prepareProperty(session,"columns",columns);
            }
            if (jsonResults.containsKey("sorting")) {
                String columns = jsonResults.getString("sorting");
                ProfilePrepareUtils.prepareProperty(session,"sorting",columns);
            }
        }

    }
}
