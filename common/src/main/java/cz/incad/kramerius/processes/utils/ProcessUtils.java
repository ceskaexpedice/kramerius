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
package cz.incad.kramerius.processes.utils;

import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Utility class for processes.  <br>
 * 
 * Can be used from both sides (in server application and also in started process)
 * @author pavels
 */
public class ProcessUtils {

    /** Lr servlet name.  This coresponds with web.xml  */
    public static final String LR_SERVLET_NAME="lr";
    
    /**
     * Returns URL to LR servlet
     * @return
     */
    public static String getLrServlet() {
        String lrServlet = KConfiguration.getInstance().getApplicationURL() + '/' + LR_SERVLET_NAME;
        return lrServlet;
    }

}
