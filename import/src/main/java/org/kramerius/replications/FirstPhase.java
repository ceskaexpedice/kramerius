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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.ws.rs.core.MediaType;

import org.kramerius.replications.pidlist.PIDsListLexer;
import org.kramerius.replications.pidlist.PIDsListParser;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.IOUtils;

/**
 * Get all pids designated for import
 * @author pavels
 */
public class FirstPhase extends AbstractPhase  {

    @Override
    public void start(String url, String userName, String pswd, String replicationCollections, String replicationImages) throws PhaseException {
        try {
            String prepareURL = K4ReplicationProcess.prepareURL(url,replicationCollections);
            String descriptionURL = K4ReplicationProcess.descriptionURL(url);

            // download description json
            download(createDescriptionFile(), descriptionURL, userName, pswd);

            // download scenario json
            download(createIterateFile(),prepareURL, userName, pswd);
            
            // preparse if scenario is valid
            preparseIterate();
        } catch (IOException e) {
            throw new PhaseException(this,e);
        }
    }


    private void preparseIterate() throws PhaseException {
        try {
            PIDsListLexer lexer = new PIDsListLexer(new FileReader(getIterateFile()));
            PIDsListParser parser = new PIDsListParser(lexer);
            parser.pids();
        } catch (FileNotFoundException e) {
            throw new PhaseException(this,e);
            
        } catch (RecognitionException e) {
            throw new PhaseException(this,e);
        } catch (TokenStreamException e) {
            throw new PhaseException(this,e);
        }
    }


    public void download(File destFile, String surl, String user, String pswd) throws PhaseException, IOException {
        Client c = Client.create();
        WebResource r = c.resource(surl);
        r.addFilter(new BasicAuthenticationClientFilter(user, pswd));
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        IOUtils.saveToFile(t, destFile);
    }

    
    @Override
    public void restart(String previousProcessUUID,File previousProcessRoot, boolean phaseCompleted, String url, String userName, String pswd,
                        String replicationCollections, String replicationImages) throws PhaseException {
        try {
            if (!getDescriptionFile().exists()) {
                File previousDescription = getDescriptionFile(previousProcessRoot);
                FileChannel fiChannel = new FileInputStream(previousDescription).getChannel();
                FileChannel foChannel = new FileOutputStream(createDescriptionFile()).getChannel();

                long size = fiChannel.size();
                fiChannel.transferTo(0, size, foChannel);
            }
            if (!getIterateFile().exists()) {
                File previousIterateFile = getIterateFile(previousProcessRoot);
                FileChannel fiChannel = new FileInputStream(previousIterateFile).getChannel();
                FileChannel foChannel = new FileOutputStream(createIterateFile()).getChannel();

                long size = fiChannel.size();
                fiChannel.transferTo(0, size, foChannel);

                // preparse if scenario is valid
                preparseIterate();
            } else {
                this.start(url, userName, pswd, replicationCollections, replicationImages);
            }
        } catch (IOException e) {
            throw new PhaseException(this,e);
        }
    }
    
}
