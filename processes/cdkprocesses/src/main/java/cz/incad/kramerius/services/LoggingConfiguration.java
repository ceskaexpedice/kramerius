package cz.incad.kramerius.services;

import cz.incad.kramerius.processes.logging.StOutConsoleHandler;
import cz.incad.kramerius.services.logging.OnlyMessageFormatter;

import java.io.IOException;
import java.util.logging.*;

public class LoggingConfiguration {

    public static final Logger LOGGER = Logger.getLogger(LoggingConfiguration.class.getName());


    public static void main(String[] args) throws IOException {

        FileHandler fileHandler = new FileHandler("test.log");
        fileHandler.setFormatter(new OnlyMessageFormatter());

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(fileHandler);

        LogManager.getLogManager().addLogger(rootLogger);


        for (int i = 0; i < 100; i++) {
            LOGGER.info(String.format("ITeration %s", i));
        }
    }
}
