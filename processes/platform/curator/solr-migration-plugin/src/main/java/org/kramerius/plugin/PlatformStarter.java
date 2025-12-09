package org.kramerius.plugin;

import cz.inovatika.kramerius.services.Migration;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

public class PlatformStarter {

    public static final Logger LOGGER = Logger.getLogger(PlatformStarter.class.getName());

    @ProcessMethod
    public static void migrationMain(
            @ParameterName("configFile") @IsRequired String configFile
    )  {
        Migration.startMigration(configFile);
    }
}
