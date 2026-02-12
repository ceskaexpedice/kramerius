package org.kramerius.plugin;

import cz.inovatika.dilia.logs.UpdateLogs;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

/**
 * UpdateLogsStarter
 * @author ppodsednik
 */
public class UpdateLogsStarter {

    @ProcessMethod
    public static void updateLogsMain() {
        UpdateLogs.main(new String[0]);
    }
}
