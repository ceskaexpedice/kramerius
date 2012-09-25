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

import java.io.File;
import java.io.IOException;


import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;

/**
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
    
        System.out.println();
        
    }
}
