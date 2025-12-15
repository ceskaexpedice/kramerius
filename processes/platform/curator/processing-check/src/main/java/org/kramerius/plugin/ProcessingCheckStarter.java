package org.kramerius.plugin;

import cz.incad.kramerius.processingindex.ProcessingIndexCheck;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

public class ProcessingCheckStarter {

    public static final Logger LOGGER = Logger.getLogger(ProcessingCheckStarter.class.getName());

    @ProcessMethod
    public static void processingCheckMain() {
        ProcessingIndexCheck.processingCheckMain();
    }
}
