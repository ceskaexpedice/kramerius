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
package org.kramerius.replications;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.logging.Level;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONTokener;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.configuration.SubnodeConfiguration;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.StringUtils;

/**
 * K4 replication process 
 * @author pavels
 */
public class K4ReplicationProcess {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(K4ReplicationProcess.class.getName());

    // all K4 replication phasses
    public static Phase[] PHASES = new Phase[] {
        new FirstPhase(),
        new SecondPhase(),
        new ThirdPhase()
    };
    
    @Process
    public static void replications(@ParameterName("url") String url, @ParameterName("username") String userName, @ParameterName("pswd")String pswd,@ParameterName("previousProcess")String previousProcessUUID) throws IOException {
        LOGGER.info("previousProcessUUID = "+previousProcessUUID);
        // definovane uuid predchoziho procesu => restart
        if ((previousProcessUUID != null) && (!previousProcessUUID.equals(""))) {
            LOGGER.info("restarting ..");
            String muserDir = System.getProperty("user.dir");
            File previousProcessFolder = new File(new File(muserDir).getParentFile(), previousProcessUUID);
            if (previousProcessFolder.exists()) {
                restart(previousProcessUUID, previousProcessFolder, url, userName, pswd);
            } else throw new RuntimeException("expect of existing folder '"+previousProcessFolder.getAbsolutePath()+"'");
        } else {
            // start
            start(url, userName, pswd);
        }
    }

    public static void restart(String processUUID, File previousProcessFolder, String url, String userName, String pswd) throws IOException {
        try {
            for (Phase ph : PHASES) {
                LOGGER.info("RESTARTING PHASE '"+ph.getClass().getName()+"'");
                ph.restart(processUUID, previousProcessFolder, isPhaseCompleted(previousProcessFolder, ph), url, userName, pswd);
                phaseCompleted(ph);
            }
        } catch (PhaseException e) {
            // co udelat pri chybe ??
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    
    
    public static void start(String url, String userName, String pswd) throws IOException {
        try {
            for (Phase ph : PHASES) {
                LOGGER.info("RESTARTING PHASE '"+ph.getClass().getName()+"'");
                ph.start(url, userName, pswd);
                phaseCompleted(ph);
            }
        } catch (PhaseException e) {
            // co udelat pri chybe 
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public static boolean isPhaseCompleted(File previousFolder, Phase phase) {
        File completedFile = phaseCompletedFile(previousFolder,phase);
        boolean flag = completedFile.exists();
        LOGGER.info("checking file '"+completedFile.getAbsolutePath()+"' returning value :"+flag);
        return flag;
    }
    
    public static File phaseCompleted(Phase phase) throws IOException {
        LOGGER.info("PHASE '"+phase.getClass().getName()+"' completed");
        File completedFile = phaseCompletedFile(phase);
        completedFile.createNewFile();
        return completedFile;
    }

    public static File phaseCompletedFile(Phase phase) {
        String clzName = phase.getClass().getName();
        File completedFile = new File(clzName+".completed");
        return completedFile;
    }

    public static File phaseCompletedFile(File rootFolder, Phase phase) {
        String clzName = phase.getClass().getName();
        File completedFile = new File(rootFolder,clzName+".completed");
        return completedFile;
    }

    public static String prepareURL(String url) {
        String pid = pidFrom(url);
        String prepareURL = StringUtils.minus(StringUtils.minus(url, pid),"handle/")+"api/replication/"+pid+"/prepare";
        return prepareURL;
    }

    public static String foxmlURL(String url, String pid) {
        String oldPid = pidFrom(url);
        String prepareURL = StringUtils.minus(StringUtils.minus(url, oldPid),"handle/")+"api/replication/"+pid+"/exportedFOXML";
        return prepareURL;
    }    
    
    public static String pidFrom(String urlPath) {
        int indexOf = urlPath.indexOf("uuid:");
        String subString = urlPath.substring(indexOf);
        if (subString.endsWith("/")) return subString.substring(0, subString.length()-1);
        else return subString;
    }
    
    
}
