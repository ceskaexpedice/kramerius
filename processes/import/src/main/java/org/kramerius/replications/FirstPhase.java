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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import org.kramerius.replications.pidlist.PIDsListLexer;
import org.kramerius.replications.pidlist.PIDsListParser;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import cz.incad.kramerius.utils.IOUtils;

/**
 * Get all pids designated for import
 */
public class FirstPhase extends AbstractPhase {

    @Override
    public void start(String url, String userName, String pswd,
                      String replicationCollections, String replicationImages)
            throws PhaseException {
        // unchanged (TODO block)
    }

    private void preparseIterate() throws PhaseException {
        try {
            PIDsListLexer lexer = new PIDsListLexer(new FileReader(getIterateFile()));
            PIDsListParser parser = new PIDsListParser(lexer);
            parser.pids();

        } catch (IOException | RecognitionException | TokenStreamException e) {
            throw new PhaseException(this, e);
        }
    }

    public void download(File destFile, String surl, String user, String pswd)
            throws PhaseException, IOException {

        Client client = ClientBuilder.newClient();

        // Jersey 3 way of basic auth
        HttpAuthenticationFeature feature =
                HttpAuthenticationFeature.basic(user, pswd);
        client.register(feature);

        WebTarget target = client.target(surl);

        String response = target
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        IOUtils.saveToFile(response, destFile);

        client.close();
    }

    @Override
    public void restart(String previousProcessUUID,
                        File previousProcessRoot,
                        boolean phaseCompleted,
                        String url,
                        String userName,
                        String pswd,
                        String replicationCollections,
                        String replicationImages)
            throws PhaseException {

        try {
            if (!getDescriptionFile().exists()) {
                File previousDescription = getDescriptionFile(previousProcessRoot);

                try (FileChannel fiChannel = new FileInputStream(previousDescription).getChannel();
                     FileChannel foChannel = new FileOutputStream(createDescriptionFile()).getChannel()) {

                    fiChannel.transferTo(0, fiChannel.size(), foChannel);
                }
            }

            if (!getIterateFile().exists()) {
                File previousIterateFile = getIterateFile(previousProcessRoot);

                try (FileChannel fiChannel = new FileInputStream(previousIterateFile).getChannel();
                     FileChannel foChannel = new FileOutputStream(createIterateFile()).getChannel()) {

                    fiChannel.transferTo(0, fiChannel.size(), foChannel);
                }

                preparseIterate();
            } else {
                this.start(url, userName, pswd, replicationCollections, replicationImages);
            }

        } catch (IOException e) {
            throw new PhaseException(this, e);
        }
    }
}