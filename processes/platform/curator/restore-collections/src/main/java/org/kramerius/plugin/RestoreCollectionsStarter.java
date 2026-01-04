package org.kramerius.plugin;

import cz.inovatika.collections.Restore;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import java.util.logging.Logger;

public class RestoreCollectionsStarter {

    public static final Logger LOGGER = Logger.getLogger(RestoreCollectionsStarter.class.getName());

    @ProcessMethod
    public static void backupMain(
            @ParameterName("backupname") @IsRequired String target
    ) throws Exception {
        Restore.restoreMain(target);
    }
}
