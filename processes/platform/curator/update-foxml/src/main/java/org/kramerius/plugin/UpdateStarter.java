package org.kramerius.plugin;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.kramerius.UpdateStreams;

import java.io.File;
import java.util.logging.Logger;

public class UpdateStarter {

    public static final Logger LOGGER = Logger.getLogger(UpdateStarter.class.getName());

    @ProcessMethod
    public static void updateMain(
            @ParameterName("inputDataDir") @IsRequired String importDirFromArgs,
            @ParameterName("startIndexer") @IsRequired Boolean startIndexerFromArgs,
            @ParameterName("pathtype") String pathtype
    )  {
        File inputDataDir;
        if (pathtype != null && pathtype.equals("relative")) {
            inputDataDir = new File(KConfiguration.getInstance().getProperty( "import.directory")+File.separator+importDirFromArgs);
        } else {
            inputDataDir = new File(importDirFromArgs);
        }

        LOGGER.info("--- Starting method: updateMain ---");
        LOGGER.info("Parameter 'inputDataDir': " + inputDataDir);
        LOGGER.info("Parameter 'startIndexer': " + startIndexerFromArgs);
        LOGGER.info("----------------------------------");

        LOGGER.info("Process platform " + inputDataDir.getAbsolutePath()+"; start indexer " + startIndexerFromArgs);
        //TODO: Pathtype - check
        UpdateStreams.updateMain(inputDataDir.getAbsolutePath(), startIndexerFromArgs);
    }
}
