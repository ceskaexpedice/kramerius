package org.kramerius.plugin;

import cz.inovatika.licenses.FlagToLicenseProcess;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

public class FlagToLicensePidsStarter {

    public static final Logger LOGGER = Logger.getLogger(FlagToLicensePidsStarter.class.getName());

    @ProcessMethod
    public static void flagToLicensePidsMain(
            @ParameterName("pid") @IsRequired String target
    ) throws Exception {
        LOGGER.info("--- Starting method: flagToLicensePidsMain ---");
        LOGGER.info("Parameter 'target': " + (target == null ? "N/A" : target));
        LOGGER.info("-------------------------------------");

        FlagToLicenseProcess.mainForPids(target);
    }
}
