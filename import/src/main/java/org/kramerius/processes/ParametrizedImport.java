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
package org.kramerius.processes;

import java.io.File;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;

public class ParametrizedImport {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ParametrizedImport.class.getName());
    
//    @DefaultParameterValue("ingestUrl")
//    public static final String INGEST_URL = KConfiguration.getInstance().getProperty("ingest.url");
//
//    @DefaultParameterValue("ingestUser")
//    public static final String INGEST_USER = KConfiguration.getInstance().getProperty("ingest.user");
//
//    @DefaultParameterValue("ingestPassword")
//    public static final String INGEST_PASSWORD = KConfiguration.getInstance().getProperty("ingest.password");
//
//    @DefaultParameterValue("importDirectory")
//    public static final File IMPORT_DIRECTORY = new File(KConfiguration.getInstance().getProperty("import.directory"));

    
    @Process
    public static void process(@ParameterName("ingestUrl") String ingestUrl, @ParameterName("ingestUser") String ingestUser, @ParameterName("ingestPassword")String ingestPassword, @ParameterName("importDirectory") File importDirectory) {
        LOGGER.info("ingestUrl = "+ingestUrl);
        LOGGER.info("ingestUser = "+ingestUser);
        LOGGER.info("ingestPassword = "+ingestPassword);
        LOGGER.info("importDirectory = "+importDirectory);
        //Import.ingest(ingestUrl, ingestUser, ingestUser, importDirectory.getAbsolutePath());
    }
}
