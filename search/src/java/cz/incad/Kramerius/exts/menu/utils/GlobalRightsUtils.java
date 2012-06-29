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
package cz.incad.Kramerius.exts.menu.utils;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class GlobalRightsUtils {

    public static boolean hasUserAllowedAction(String actionFormalName, HttpServletRequest request) {
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<String> actionsForRepository = (List<String>) session.getAttribute("securityForRepository");
        return actionsForRepository != null ? actionsForRepository.contains(actionFormalName) : false;
    }

}
