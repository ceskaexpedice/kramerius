package org.kramerius.plugin;

import cz.incad.kramerius.utils.StringUtils;
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
        String resolvedPathType = resolvePathType(pathtype);
        File inputDataDir;
        if ("relative".equals(resolvedPathType)) {
            inputDataDir = new File(KConfiguration.getInstance().getProperty( "import.directory")+File.separator+importDirFromArgs);
        } else {
            inputDataDir = new File(importDirFromArgs);
        }



        LOGGER.info("--- Starting method: updateMain ---");
        LOGGER.info("Parameter 'inputDataDir': " + inputDataDir);
        LOGGER.info("Parameter 'startIndexer': " + startIndexerFromArgs);
        LOGGER.info("Parameter 'pathtype': " + (resolvedPathType == null ? "N/A" : resolvedPathType));
        LOGGER.info("----------------------------------");

        LOGGER.info("Process platform " + inputDataDir.getAbsolutePath()+"; start indexer " + startIndexerFromArgs);
        //TODO: Pathtype - check
        UpdateStreams.updateMain(inputDataDir.getAbsolutePath(), startIndexerFromArgs);
    }

    private static String resolvePathType(String pathtype) {
        if (StringUtils.isAnyString(pathtype)) {
            return pathtype;
        }
        String configuredPathType = KConfiguration.getInstance().getConfiguration().getString("imports.pathtype");
        return StringUtils.isAnyString(configuredPathType) ? configuredPathType : pathtype;
    }
}
