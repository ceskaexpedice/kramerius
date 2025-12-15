package org.kramerius.plugin;

import cz.incad.kramerius.statistics.impl.nkp.NKPLogProcess;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

public class NkpLogsStarter {

    public static final Logger LOGGER = Logger.getLogger(NkpLogsStarter.class.getName());

    @ProcessMethod
    public static void nkpLogsMain(
            @ParameterName("from") @IsRequired String from,
            @ParameterName("to") @IsRequired String to,
            @ParameterName("emailNotification") Boolean emailNotification
    ) throws Exception {
        NKPLogProcess.nkpLogMainMain(from, to, emailNotification);
    }
}
