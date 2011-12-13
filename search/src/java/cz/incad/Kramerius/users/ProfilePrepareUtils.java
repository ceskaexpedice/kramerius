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

}
