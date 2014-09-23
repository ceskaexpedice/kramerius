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

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.processes.BatchStates;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Utility class for processes.  <br>
 * 
 * Can be used from both sides (in server application and also in started process)
 * @author pavels
 */
public class ProcessUtils {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessUtils.class.getName());
    
    /** Lr servlet name.  This coresponds with web.xml  */
    public static final String LR_SERVLET_NAME="lr";
    
    public static String getApiPoint() {
        
        String applicationURL = KConfiguration.getInstance().getApplicationURL();
        if (!applicationURL.endsWith("/")) {
            applicationURL += '/';
        }
        String api = applicationURL +  "api/v4.6/processes";
        return api;
    }
    
    /**
     * Returns URL to LR servlet
     * @return
     */
    public static String getLrServlet() {
        String lrServlet = KConfiguration.getInstance().getApplicationURL() + '/' + LR_SERVLET_NAME;
//        LOGGER.info("APPURL :"+KConfiguration.getInstance().getApplicationURL());
        return lrServlet;
    }

    /**
     * Start new process
     * @param processDef Process definition
     * @param nparams nparams parameter 
     * @throws Exception Any Error has been occured
     */
    public static void startProcess(String processDef, String nparams) throws Exception{
        LOGGER.info(" spawn process '"+processDef+"'");
        String base = ProcessUtils.getLrServlet();    
        String url = base + "?action=start&def="+processDef+"&nparams="+nparams+"&token="+System.getProperty(ProcessStarter.TOKEN_KEY);
        byte[] output = new byte[0];
        try {
            output = ProcessStarter.httpGet(url);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    /**
     * Start new process
     * @param processDef Process definition
     * @param params Process parameters
     */
    public static void startProcess(String processDef, String[] params) {
        LOGGER.info(" spawn process '"+processDef+"'");
        String base = ProcessUtils.getLrServlet();    
        String url = base + "?action=start&def="+processDef+"&nparams="+nparams(params)+"&token="+System.getProperty(ProcessStarter.TOKEN_KEY);
        try {
            ProcessStarter.httpGet(url);
        } catch (Exception e) {
            LOGGER.severe("Error spawning indexer for "+processDef+":"+e);
        }
    }

    /**
     * Close auth token
     * @param processUuid Process uuid
     */
    public static void closeToken(String processUuid) {
        LOGGER.info(" close token for '"+processUuid+"'");
        String base = ProcessUtils.getLrServlet();    
        String url = base + "?action=closeToken&uuid="+processUuid;
        try {
            ProcessStarter.httpGet(url);
        } catch (Exception e) {
            LOGGER.severe("Error closing token for "+processUuid+":"+e);
        }
    }
    
    /**
     * Helper method creates nparams string from given params parameters
     * @param params Params parameters
     * @return crated string
     */
    public static String nparams(String[] params) {
        StringBuffer buffer = new StringBuffer("{");
        for (int i = 0; i < params.length; i++) {
            buffer.append(nparam(params[i]));
            if (i < params.length -1) {
                buffer.append(";");
            }
        }
        buffer.append("}");
        return buffer.toString();
    }
    
    /**
     * Returns parametr with escape sequence
     * @param string String to be escaped
     * @return String with escape sequence
     */
    public static String nparam(String string) {
        String[] escapeChars = {"\\",":",";","{","}"};
        for (String toEscape : escapeChars) {
            if (string.contains(toEscape)) {
                string = string.replace(toEscape, "\\"+toEscape);
            }
        }
        return string;
    }
    
    
}
