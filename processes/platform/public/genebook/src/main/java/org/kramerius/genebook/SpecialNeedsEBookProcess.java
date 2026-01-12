package org.kramerius.genebook;

import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

public class SpecialNeedsEBookProcess {

    public static final Logger LOGGER = Logger.getLogger(SpecialNeedsEBookProcess.class.getName());

    @ProcessMethod
    public static void generate(
            @ParameterName("pid") @IsRequired String pid,
            @ParameterName("output") @IsRequired String output,
            @ParameterName("user") String user
    ) {
        LOGGER.info("Generating formatted text");
        LOGGER.info("pid: " + pid);
        LOGGER.info("output: " + output);
        LOGGER.info("output: " + user);
    }

}
