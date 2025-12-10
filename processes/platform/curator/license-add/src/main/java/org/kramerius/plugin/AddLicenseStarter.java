package org.kramerius.plugin;

import cz.incad.kramerius.SetLicenseProcess;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

public class AddLicenseStarter {

    public static final Logger LOGGER = Logger.getLogger(AddLicenseStarter.class.getName());

    @ProcessMethod
    public static void addLicenseMain(
            @ParameterName("license") @IsRequired String license,
            @ParameterName("pid") @IsRequired String target
    ) {

        LOGGER.info("--- Starting method: addLicenseMain ---");
        LOGGER.info("Parameter 'license': " + (license == null ? "N/A" : license));
        LOGGER.info("Parameter 'target': " + (target == null ? "N/A" : target));
        LOGGER.info("-------------------------------------");

        SetLicenseProcess.setLicenseMain(SetLicenseProcess.Action.ADD.name(),license,target );

    }
}