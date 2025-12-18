package org.kramerius.plugin;

import cz.inovatika.licenses.FlagToLicenseProcess;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

public class FlagToLicenseStarter {

    public static final Logger LOGGER = Logger.getLogger(FlagToLicenseStarter.class.getName());

    @ProcessMethod
    public static void flagToLicenseMain() throws Exception {
        FlagToLicenseProcess.main();
    }
}