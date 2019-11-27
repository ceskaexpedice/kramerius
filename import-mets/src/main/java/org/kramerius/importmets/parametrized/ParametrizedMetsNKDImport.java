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
package org.kramerius.importmets.parametrized;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.File;
import org.apache.commons.configuration.Configuration;
import org.kramerius.importmets.MetsConvertor;

/**
 * Parametrized mets NKD import
 * @author pavels
 */
public class ParametrizedMetsNKDImport {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ParametrizedMetsNKDImport.class.getName());
    
    @Process
    public static void process(
                @ParameterName("convertDirectory")File convertDirectory, 
                @ParameterName("convertTargetDirectory")File targetDirectory, 
                @ParameterName("ingestSkip")Boolean ingestSkip,
                @ParameterName("startIndexer")Boolean startIndexer,
                @ParameterName("defaultRights")Boolean defaultRights,
                @ParameterName("imageServerTilesURLPrefix")String isTilesPrefix,
                @ParameterName("imageServerImagesURLPrefix")String isImagesPrefix,
                @ParameterName("imageServerDirectory")String isDirectory
    ) {

        System.setProperty("convert.defaultRights", defaultRights.toString());
        System.setProperty("ingest.startIndexer", startIndexer.toString());
        System.setProperty("ingest.skip", ingestSkip.toString());

        if (isTilesPrefix != null || isImagesPrefix != null || isDirectory != null) {
            if (isTilesPrefix != null && isImagesPrefix != null && isDirectory != null) {
                Configuration config = KConfiguration.getInstance().getConfiguration();

                config.setProperty("convert.imageServerTilesURLPrefix", isTilesPrefix);
                config.setProperty("convert.imageServerImagesURLPrefix", isImagesPrefix);
                config.setProperty("convert.imageServerDirectory", isDirectory);
            } else {
                throw new IllegalArgumentException("Using optional imageserver parameters requires all of them being defined" +
                        isImagesPrefix == null ? ", isImagesPrefix cannot be null" :  "" +
                        isTilesPrefix == null ? ", isTilesPrefix cannot be null" : "" +
                        isDirectory == null ? ", isDirectory cannot be null" : ""
                );
            }
        }

        try {
            //TODO: I18N
            ProcessStarter.updateName("Parametrizovany import NDK METS z '"+convertDirectory.getAbsolutePath()+"'");
            MetsConvertor.main(new String[] {defaultRights.toString(), convertDirectory.getAbsolutePath(), targetDirectory.getAbsolutePath()});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
}
