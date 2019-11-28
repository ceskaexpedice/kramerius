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
package cz.incad.feedrepo.impl.processes;

import cz.incad.feedrepo.ImportToRepos;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

/**
 * Parametrized import proces
 * @author pavels
 *
 */
public class ParametrizedImport {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ParametrizedImport.class.getName());
    

    
    @Process
    public static void process( @ParameterName("importDirectory") File importDirectory, 
            @ParameterName("startIndexer")Boolean startIndexer,
            @ParameterName("updateExisting")Boolean updateExisting) throws UnsupportedEncodingException, ClassNotFoundException, InstantiationException, IllegalAccessException, RepositoryException {

        System.setProperty("import.directory", importDirectory.getAbsolutePath());
        System.setProperty("ingest.startIndexer", startIndexer.toString());
        System.setProperty("ingest.updateExisting", updateExisting.toString());
        System.setProperty("ingest.skip", "false");   //import se bude vždy spouštět
        
        try {
            //TODO: I18N
            ProcessStarter.updateName("Parametrizovany import z '"+importDirectory.getAbsolutePath()+"'");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }

        ImportToRepos.main(new String[0]);
        //standard import program
        //Import.main(new String[0]);
    }
}
