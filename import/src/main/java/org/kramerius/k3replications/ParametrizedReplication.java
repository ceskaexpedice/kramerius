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

import org.kramerius.Download;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;

/**
 * Parametrized replication process
 * @author pavels
 */
public class ParametrizedReplication {

    
    @Process
    public static void replications(@ParameterName("replicatetype") String replicateType, 
                                    @ParameterName("idlist") String idList, 
                                    @ParameterName("migrationDirectory")File migrationDirectory,
                                    @ParameterName("targetDirectory")File targetDirectory, 
                                    @ParameterName("ingestSkip")Boolean ingestSkip, 
                                    @ParameterName("indexerStart")Boolean startIndexer, 
                                    @ParameterName("defaultRights")Boolean defaultRights ) throws IOException {
    
        if (idList != null && (!idList.trim().equals(""))) {
            if (replicateType.equalsIgnoreCase("monographs")) {
                
                System.setProperty("convert.defaultRights", defaultRights.toString());
                System.setProperty("migration.directory", migrationDirectory.getAbsolutePath());
                System.setProperty("migration.target.directory", targetDirectory.getAbsolutePath());
                System.setProperty("ingest.startIndexer", startIndexer.toString());
                System.setProperty("ingest.skip", defaultRights.toString());
                
                Download.replicateMonographs(new BufferedReader(new StringReader(idList)));
            } else {
                
                System.setProperty("convert.defaultRights", defaultRights.toString());
                System.setProperty("migration.directory", migrationDirectory.getAbsolutePath());
                System.setProperty("migration.target.directory", targetDirectory.getAbsolutePath());
                System.setProperty("ingest.startIndexer", startIndexer.toString());
                System.setProperty("ingest.skip", defaultRights.toString());
                
                Download.replicatePeriodicals(new BufferedReader(new StringReader(idList)));
            }
        } else throw new RuntimeException("no idlist defined !");
    }
    
    
}
