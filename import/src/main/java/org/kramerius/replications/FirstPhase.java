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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.kramerius.replications.pidlist.PIDsListLexer;
import org.kramerius.replications.pidlist.PIDsListParser;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;

public class FirstPhase extends AbstractPhase  {

    @Override
    public void start(String url, String userName, String pswd) throws PhaseException {
        // download scenario json
        downloadIterate(url, userName, pswd);
        // preparse if scenario is valid
        preparseIterate();
    }


    private void preparseIterate() throws PhaseException {
        try {
            PIDsListLexer lexer = new PIDsListLexer(new FileReader(getIterateFile()));
            PIDsListParser parser = new PIDsListParser(lexer);
            parser.pids();
        } catch (FileNotFoundException e) {
            throw new PhaseException(e);
            
        } catch (RecognitionException e) {
            throw new PhaseException(e);
        } catch (TokenStreamException e) {
            throw new PhaseException(e);
        }
    }


    public void downloadIterate(String url, String user, String pswd) throws PhaseException {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = RESTHelper.inputStream(K4ReplicationProcess.prepareURL(url), user, pswd);
            File iterate = createIterateFile();
            fos = new FileOutputStream(iterate);
            IOUtils.copyStreams(is, fos);
        } catch (IOException e) {
            throw new PhaseException(e);
        } finally {
            IOUtils.tryClose(is);
            IOUtils.tryClose(fos);
        }
    }

    
    @Override
    public void restart(String previousProcessUUID,File previousProcessRoot, String url, String userName, String pswd) throws PhaseException {
        this.start(url, userName, pswd);
    }


     
    
}
