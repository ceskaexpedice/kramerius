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
package cz.incad.Kramerius.views.themes;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * This object is suitable for choosing theme
 * 
 * @author pavels
 */
public class ThemeViewObject {

    private static final String DEFAULT = "smoothness";
    private static final String THEME_PARAM = "theme";
    @Inject
    Provider<HttpServletRequest> requestProvider;

    public String getSelectedTheme() {
        HttpServletRequest request = this.requestProvider.get();
        String par = request.getParameter(THEME_PARAM);
        if (par != null) {
            request.getSession().setAttribute(THEME_PARAM, par);
            return par;
        } else if (request.getSession().getAttribute(THEME_PARAM) != null) {
            return (String) request.getSession().getAttribute(THEME_PARAM);
        } else {
            return DEFAULT;
        }
    }
}
