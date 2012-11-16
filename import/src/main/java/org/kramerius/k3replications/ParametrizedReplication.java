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
/**
 * 
 */
package org.kramerius.k3replications;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;

import org.apache.commons.codec.binary.Base64;
import org.kramerius.Download;


import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;

/**
 * Parametrized replication process
 * @author pavels
 */
public class ParametrizedReplication {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ParametrizedReplication.class.getName());
    
    @Process
    public static void replications(@ParameterName("replicatetype") String replicateType, 
                                    @ParameterName("idlist") String idList, 
                                    @ParameterName("migrationDirectory")File migrationDirectory,
                                    @ParameterName("targetDirectory")File targetDirectory, 
                                    @ParameterName("ingestSkip")Boolean ingestSkip, 
                                    @ParameterName("startIndexer")Boolean startIndexer,
                                    @ParameterName("defaultRights")Boolean defaultRights ) throws IOException {
    
        try {
            if (idList != null) {
                Base64 base64 = new Base64(0);
                byte[] decoded = base64.decode(idList);
                idList =  new String(decoded);
            }
        } catch (Exception e1) {
            LOGGER.log(Level.SEVERE,e1.getMessage(), e1);
        }
        
        try {
            //TODO: I18N
            ProcessStarter.updateName("Parametrizovana replikace z K3 pres '"+migrationDirectory.getAbsolutePath()+"'");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }

    
        LOGGER.info("replicatetype "+replicateType);
        LOGGER.info("idlist :'"+idList+"'");
        LOGGER.info("migrationDirectory "+migrationDirectory.getAbsolutePath());
        LOGGER.info("targetDirectory "+targetDirectory.getAbsolutePath());
        LOGGER.info("ingestSkip "+ingestSkip);
        LOGGER.info("indexerStart "+startIndexer);
        LOGGER.info("defaultRights "+defaultRights);

        if (idList != null && (!idList.trim().equals(""))) {
            if (replicateType.equalsIgnoreCase("monograph")) {
                
                System.setProperty("convert.defaultRights", defaultRights.toString());
                System.setProperty("migration.directory", migrationDirectory.getAbsolutePath());
                System.setProperty("migration.target.directory", targetDirectory.getAbsolutePath());
                System.setProperty("ingest.startIndexer", startIndexer.toString());
                System.setProperty("ingest.skip", ingestSkip.toString());
                
                Download.replicateMonographs(new BufferedReader(new StringReader(idList)));
            } else {
                
                System.setProperty("convert.defaultRights", defaultRights.toString());
                System.setProperty("migration.directory", migrationDirectory.getAbsolutePath());
                System.setProperty("migration.target.directory", targetDirectory.getAbsolutePath());
                System.setProperty("ingest.startIndexer", startIndexer.toString());
                System.setProperty("ingest.skip", ingestSkip.toString());
                
                Download.replicatePeriodicals(new BufferedReader(new StringReader(idList)));
            }
        } else throw new RuntimeException("no idlist defined !");
    }
    
    
}
