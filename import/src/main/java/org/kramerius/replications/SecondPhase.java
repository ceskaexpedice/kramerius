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

import antlr.RecognitionException;
import antlr.TokenStreamException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

import org.apache.kahadb.util.ByteArrayInputStream;
import org.kramerius.Import;
import org.kramerius.replications.pidlist.PIDsListLexer;
import org.kramerius.replications.pidlist.PIDsListParser;
import org.kramerius.replications.pidlist.PidsListCollect;
import org.w3c.dom.Document;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.Stack;

public class SecondPhase extends AbstractPhase  {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SecondPhase.class.getName());

    static String DONE_FOLDER_NAME = "DONE";
    static int MAXITEMS=30000;
    
    private DONEController controller = null;
    private boolean findPid = false;
    private String replicationCollections;
    
    @Override
    public void start(String url, String userName, String pswd, String replicationCollections) throws PhaseException {
        this.findPid = false;
        this.controller = new DONEController(new File(DONE_FOLDER_NAME), MAXITEMS);
        this.replicationCollections = replicationCollections;
        this.processIterate(url, userName, pswd);
    }

    public void pidEmitted(String pid, String url, String userName, String pswd) throws PhaseException {
        try {
            LOGGER.info("processing pid '"+pid+"'");
            boolean shouldSkip = (findPid && this.controller.findPid(pid) != null);
            if (!shouldSkip) {
                File foxmlfile = null;
                InputStream inputStream = null;
                try {
                    inputStream = rawFOXMLData(pid, url, userName, pswd);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    IOUtils.copyStreams(inputStream, bos);

                    foxmlfile = foxmlFile(new ByteArrayInputStream(bos.toByteArray()), pid);
                    ingest(foxmlfile);
                    createFOXMLDone(pid);
                } catch (LexerException e) {
                    throw new PhaseException(this,e);
                } catch (IOException e) {
                    throw new PhaseException(this,e);
                } catch(PhaseException e){
                    // forward
                    throw e;
                }catch(RuntimeException e) {
                    if (e.getCause() != null) throw new PhaseException(this,e.getCause());
                    else throw new PhaseException(this,e);
                } catch (Exception e) {
                    if (e.getCause() != null) throw new PhaseException(this,e.getCause());
                    else throw new PhaseException(this,e);
                } finally {
                    if (inputStream != null) IOUtils.tryClose(inputStream);
                    if (foxmlfile != null) foxmlfile.delete();
                }
            } else {
                LOGGER.info("skipping pid '"+pid+"'");
            }
        } catch (LexerException e) {
            throw new PhaseException(this,e);
        }
    }

    /**
     * @param path
     * @param url
     * @param userName
     * @param pswd
     */
    public void pathEmmited(String path, String url, String userName, String pswd) {
        // Not important at the moment
    }


    public void ingest(File foxmlfile) throws PhaseException{
        LOGGER.info("ingesting '"+foxmlfile.getAbsolutePath()+"'");
        Import.initialize(KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"));
        try {
            Import.ingest(foxmlfile, null, null, null, false);  //TODO třetí parametr má být List<String>, inicializovaný na začátku této fáze a předaný třetí fázi, kde se budou třídit vazby
        } catch (RuntimeException e) {
            if (e.getCause() != null) throw new PhaseException(this, e.getCause());
            else throw new PhaseException(this,e);
        }
    }
    
    public File foxmlFile(InputStream foxmlStream, String pid) throws LexerException, IOException, PhaseException {
        FileOutputStream fos = null;
        File foxml = createFOXMLFile(pid);
        try {
            fos = new FileOutputStream(foxml);
            IOUtils.copyStreams(foxmlStream, fos );
            return foxml;
        } finally {
            IOUtils.tryClose(fos);
        }
    }

    public InputStream rawFOXMLData(String pid, String url, String userName, String pswd) throws PhaseException {
        Client c = Client.create();
        WebResource r = c.resource(K4ReplicationProcess.foxmlURL(url, pid, this.replicationCollections));
        r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));
        InputStream t = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        return t;
    }
    
    public File createFOXMLDone(String pid) throws LexerException, IOException, PhaseException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();
        String objectId = pidParser.getObjectId();
        File importDoneFile = new File(controller.getCurrentSubFolder(), objectId+".fo.done");
        if (!importDoneFile.exists()) importDoneFile.createNewFile();
        if (!importDoneFile.exists()) throw new PhaseException(this,"file not exists '"+importDoneFile.getAbsolutePath()+"'");
        return importDoneFile;
        
    }
    
    public File createFOXMLFile(String pid) throws LexerException, IOException, PhaseException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();
        String objectId = pidParser.getObjectId();
        File foxmlFile = new File(objectId+".fo.xml");
        if (!foxmlFile.exists()) foxmlFile.createNewFile();
        if (!foxmlFile.exists()) throw new PhaseException(this,"file not exists '"+foxmlFile.getAbsolutePath()+"'");
        return foxmlFile;
        
    }
    
    

    private void processIterate(String url, String userName, String pswd) throws PhaseException {
        try {
            PIDsListLexer lexer = new PIDsListLexer(new FileReader(getIterateFile()));
            PIDsListParser parser = new PIDsListParser(lexer);
            parser.setPidsListCollect(new Emitter(url, userName, pswd));
            parser.pids();
        } catch (FileNotFoundException e) {
            throw new PhaseException(this,e);
        } catch (RecognitionException e) {
            throw new PhaseException(this,e);
        } catch (TokenStreamException e) {
            throw new PhaseException(this,e);
        } catch (RuntimeException e) {
            Throwable thr = e.getCause();
            if ((thr != null) && (thr instanceof PhaseException)) {
                throw ((PhaseException)thr);
            } else if (thr != null) {
                throw new PhaseException(this,thr);
            } else throw new PhaseException(this,e);
        }
    }



    @Override
    public void restart(String previousProcessUUID,File previousProcessRoot, boolean phaseCompleted, String url, String userName, String pswd, String replicationCollections) throws PhaseException {
        try {
            if (!phaseCompleted) {
                this.findPid = true;
                IOUtils.copyFolders(new File(previousProcessRoot, DONE_FOLDER_NAME),new File(DONE_FOLDER_NAME));
                this.controller = new DONEController(new File(DONE_FOLDER_NAME), MAXITEMS);
                this.replicationCollections = replicationCollections;
                processIterate(url, userName, pswd);
            }
        } catch (IOException e) {
            throw new PhaseException(this,e);
        }
    }
    
    static class DONEController {
        
        private File doneRoot;
        private int max;
        private int counter = 0;
        
        public DONEController(File doneRoot, int max) {
            super();
            this.doneRoot = doneRoot;
            this.max = max;
            makeSureRootExists(doneRoot);
        }

        File makeSureRootExists(File doneRoot) {
            if (!doneRoot.exists()) doneRoot.mkdirs();
            return doneRoot;
        }

        
        public File getCurrentSubFolder() {
            File[] sfiles= subfolder(this.doneRoot).listFiles();
            if ((sfiles != null) && (sfiles.length >= this.max)) {
                this.counter += 1;
            }
            return subfolder(this.doneRoot);
        }


        File subfolder(File f) {
            File sub = new File(f,""+this.counter);
            if (!sub.exists()) sub.mkdirs();
            return sub;
        }
        
        public File findPid(String pid) throws LexerException {
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();
            String objectId = pidParser.getObjectId();

            Stack<File> procStack = new Stack<File>();
            LOGGER.info("finding pid '"+pid+"' ("+objectId+") in '"+this.doneRoot.getAbsolutePath()+"'");
            procStack.push(this.doneRoot);
            while(!procStack.isEmpty()) {
                File poppedFile = procStack.pop();
                if (poppedFile.getName().startsWith(objectId)) {
                    LOGGER.info("found file '"+poppedFile.getAbsolutePath()+"'");
                    return poppedFile;
                }
                File[] subfiles = poppedFile.listFiles();
                if(subfiles != null) {
                    for (File f : subfiles) {
                        procStack.push(f);
                    }
                }
            }
            LOGGER.info("no file  starts with '"+objectId+"'");
            return null;
        }
        
    }
    
    class Emitter implements PidsListCollect {
        
        private String url,userName,pswd;
        
        
        public Emitter(String url, String userName, String pswd) {
            super();
            this.url = url;
            this.userName = userName;
            this.pswd = pswd;
        }


        @Override
        public void pidEmitted(String pid)  {
            try {
                if ((pid.startsWith("'")) || (pid.startsWith("\""))) {
                    pid = pid.substring(1,pid.length()-1);
                }
                SecondPhase.this.pidEmitted(pid, this.url, this.userName, this.pswd);
            } catch (PhaseException e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        public void pathEmitted(String path) {
            if ((path.startsWith("'")) || (path.startsWith("\""))) {
                path = path.substring(1,path.length()-1);
            }
            SecondPhase.this.pathEmmited(path, this.url, this.userName, this.pswd);
        }
        
        
    }
    

}
