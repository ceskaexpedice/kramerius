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
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.kramerius.Import;
import org.xml.sax.SAXException;

import com.qbizm.kramerius.imptool.poc.Main;
import com.qbizm.kramerius.imptool.poc.valueobj.ServiceException;

import cz.incad.kramerius.processes.annotations.DefaultParameterValue;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ParametrizedConvert {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ParametrizedConvert.class.getName());
    
    @DefaultParameterValue("defaultRights")
    public static final String DEFAULT_VISIBLE = KConfiguration.getInstance().getProperty("convert.defaultRights","false");

    @DefaultParameterValue("convertDirectory")
    public static final File DEFAULT_CONVERT_DIRECTORY = new File(KConfiguration.getInstance().getProperty("convert.directory"));

    @DefaultParameterValue("convertTargetDirectory")
    public static final File DEFAULT_CONVERT_TARGET_DIRECTORY = new File(KConfiguration.getInstance().getProperty("convert.target.directory"));

    @DefaultParameterValue("ingestUrl")
    public static final String DEFAULT_INGEST_URL = KConfiguration.getInstance().getProperty("ingest.url");

    @DefaultParameterValue("ingestUser")
    public static final String DEFAULT_INGEST_USER = KConfiguration.getInstance().getProperty("ingest.user");

    @DefaultParameterValue("ingestPassword")
    public static final String DEFAULT_INGEST_PASSWORD = KConfiguration.getInstance().getProperty("ingest.password");
    
    
    @Process
    public static void process(@ParameterName("defaultRights") String defaultRights, @ParameterName("convertDirectory") File convertDirectory, @ParameterName("convertTargetDirectory") File convertTargetDirectory, @ParameterName("ingestUrl") String ingestUrl, @ParameterName("ingestUser") String ingestUser, @ParameterName("ingestPassword") String ingestPassword
                                
    ) throws FileNotFoundException, InterruptedException, JAXBException, SAXException, ServiceException {
        
        LOGGER.info("defaultRights :"+defaultRights);
        LOGGER.info("convertDirectory :"+convertDirectory.getAbsolutePath());
        LOGGER.info("convertTargetDirectory :"+convertTargetDirectory.getAbsolutePath());
        LOGGER.info("ingestUrl :"+ingestUrl);
        LOGGER.info("ingestUser :"+ingestUser);
        LOGGER.info("ingestPassword :"+ingestPassword);
        
//        String uuid = Main.convert(KConfiguration.getInstance().getProperty("convert.directory"), 
//                    KConfiguration.getInstance().getProperty("convert.target.directory"), false, Boolean.parseBoolean(defaultRights), null);
//        Import.ingest(KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), KConfiguration.getInstance().getProperty("convert.target.directory"));

    }
}
