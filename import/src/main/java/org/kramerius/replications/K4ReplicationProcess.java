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
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * K4 replication process 
 * @author pavels
 */
public class K4ReplicationProcess {

    static String API_VERSION ="v4.6";
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(K4ReplicationProcess.class.getName());

    // all K4 replication phasses
    public static Phase[] PHASES = new Phase[] {
    	new ZeroPhase(),
    	
        new FirstPhase(),
        new SecondPhase(),
        new ThirdPhase()
    };
    
    @Process
    public static void replications(@ParameterName("url") String url, @ParameterName("username") String userName, @ParameterName("pswd") String pswd,
                                    @ParameterName("replicateCollections") String replicateCollections,
                                    @ParameterName("replicateImages") String replicateImages,
                                    @ParameterName("previousProcess")String previousProcessUUID) throws IOException {
        LOGGER.info("previousProcessUUID = "+previousProcessUUID);
        handleValidation(url);
        // definovane uuid predchoziho procesu => restart
        if ((previousProcessUUID != null) && (!previousProcessUUID.equals(""))) {
            LOGGER.info("restarting ..");
            String muserDir = System.getProperty("user.dir");
            File previousProcessFolder = new File(new File(muserDir).getParentFile(), previousProcessUUID);
            if (previousProcessFolder.exists()) {
                restart(previousProcessUUID, previousProcessFolder, url, userName, pswd, replicateCollections, replicateImages);
            } else throw new RuntimeException("expect of existing folder '"+previousProcessFolder.getAbsolutePath()+"'");
        } else {
            // start
            start(url, userName, pswd,replicateCollections, replicateImages);
        }
    }

    public static void handleValidation(String handle)  {
        try {
            // try to parse url
            new URL(handle);
            // find handle context
            if (!handle.contains("handle")) throw new RuntimeException(" '"+handle+"' is not valid hanle url");
            String pidFrom = pidFrom(handle);
            PIDParser parser = new PIDParser(pidFrom);
            parser.objectPid();
            String objectPid = parser.getObjectPid();
            if (objectPid == null)  throw new RuntimeException("cannot determine pid");
        } catch (MalformedURLException e) {
            LOGGER.severe("cannot parse '"+handle+"'");
            throw new RuntimeException(e);
        } catch (LexerException e) {
            LOGGER.severe("cannot parse pid in'"+handle+"'");
            throw new RuntimeException(e);
        }
    }
    
    public static void restart(String processUUID, File previousProcessFolder, String url, String userName, String pswd, String replicateCollections, String replicateImages) throws IOException {
        try {
            for (Phase ph : PHASES) {
                LOGGER.info("RESTARTING PHASE '"+ph.getClass().getName()+"'");
                ph.restart(processUUID, previousProcessFolder, isPhaseCompleted(previousProcessFolder, ph), url, userName, pswd, replicateCollections, replicateImages);
                phaseCompleted(ph);
            }
        } catch (PhaseException e) {
            // co udelat pri chybe ??
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            phaseFailed(e.getPhase());
            throw new RuntimeException(e);
        }
    }

    
    
    public static void start(String url, String userName, String pswd, String replicateCollections, String replicateImages) throws IOException {
        try {
            ProcessStarter.updateName("Replikace titulu '"+url+"'");
            for (Phase ph : PHASES) {
                LOGGER.info("STARTING PHASE '"+ph.getClass().getName()+"'");
                ph.start(url, userName, pswd, replicateCollections, replicateImages);
                phaseCompleted(ph);
            }
        } catch (PhaseException e) {
            // co udelat pri chybe 
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            phaseFailed(e.getPhase());
            throw new RuntimeException(e);
        }
    }

    public static boolean isPhaseCompleted(File previousFolder, Phase phase) {
        File completedFile = phaseCompletedFile(previousFolder,phase);
        boolean flag = completedFile.exists();
        LOGGER.info("checking file '"+completedFile.getAbsolutePath()+"' returning value :"+flag);
        return flag;
    }
    
    static File phaseCompleted(Phase phase) throws IOException {
        LOGGER.info("PHASE '"+phase.getClass().getName()+"' completed");
        File completedFile = phaseCompletedFile(phase);
        completedFile.createNewFile();
        return completedFile;
    }

    static File phaseFailed(Phase phase) throws IOException {
        LOGGER.info("PHASE '"+phase.getClass().getName()+"' completed");
        File completedFile = phaseFailedFile(phase);
        completedFile.createNewFile();
        return completedFile;
    }

    static File phaseFailedFile(Phase phase) {
        String clzName = phase.getClass().getName();
        File completedFile = new File(clzName+".failed");
        return completedFile;
    }

    static File phaseFailedFile(File rootFolder,Phase phase) {
        String clzName = phase.getClass().getName();
        File completedFile = new File(rootFolder,clzName+".failed");
        return completedFile;
    }

    static File phaseCompletedFile(Phase phase) {
        String clzName = phase.getClass().getName();
        File completedFile = new File(clzName+".completed");
        return completedFile;
    }

    static File phaseCompletedFile(File rootFolder, Phase phase) {
        String clzName = phase.getClass().getName();
        File completedFile = new File(rootFolder,clzName+".completed");
        return completedFile;
    }

    public static String prepareURL(String url, String replicationCollections) {
        String pid = pidFrom(url);
        String prepareURL = StringUtils.minus(StringUtils.minus(url, pid),"handle/")+"api/"+API_VERSION+"/replication/"+pid+"/tree";
        boolean flag = Boolean.parseBoolean(replicationCollections);
        if (flag) {
        	prepareURL += "?replicateCollections=true";
        }
        return prepareURL;
    }

    public static String descriptionURL(String url) {
        String pid = pidFrom(url);
        String prepareURL = StringUtils.minus(StringUtils.minus(url, pid),"handle/")+"api/"+API_VERSION+"/replication/"+pid;
        return prepareURL;
    }

    public static String foxmlURL(String url, String pid,String replicationCollections) {
        String oldPid = pidFrom(url);
        String prepareURL = StringUtils.minus(StringUtils.minus(url, oldPid),"handle/")+"api/"+API_VERSION+"/replication/"+pid+"/foxml";
        boolean flag = Boolean.parseBoolean(replicationCollections);
        if (flag) {
        	prepareURL += "?replicateCollections=true";
        }
        return prepareURL;
    }

    public static String imgOriginalURL(String url, String pid) {
        String oldPid = pidFrom(url);
        String imgOriginalURL = StringUtils.minus(StringUtils.minus(url, oldPid), "handle/")
                + "api/" + API_VERSION + "/replication/" + pid + "/img_original";
        return imgOriginalURL;
    }
    
    /**
     * Find pid in given url and returns it
     * @param urlPath URL with pid
     * @return found pid
     */
    public static String pidFrom(String urlPath) {
        if (urlPath ==null) return "";
        int indexOf = urlPath.indexOf("uuid:");
        if (indexOf>=0) {
            String subString = urlPath.substring(indexOf);
            if (subString.endsWith("/")) return subString.substring(0, subString.length()-1);
            else return subString;
        } else return "";
    }
}
