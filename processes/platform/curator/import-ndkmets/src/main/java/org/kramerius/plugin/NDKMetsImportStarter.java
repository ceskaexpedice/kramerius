package org.kramerius.plugin;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;
import org.kramerius.Import;
import org.kramerius.importer.inventory.ScheduleStrategy;
import org.kramerius.importmets.MetsConvertor;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;

public class NDKMetsImportStarter {


    public static final Logger LOGGER = Logger.getLogger(NDKMetsImportStarter.class.getName());

    @ProcessMethod
    public static void ndkmets(
            @ParameterName("policy") @IsRequired String policy,
            @ParameterName("inputDataDir") @IsRequired String importDirFromArgs,
            @ParameterName("startIndexer") @IsRequired Boolean startIndexerFromArgs,
            @ParameterName("useIIPServer")  Boolean useIIPServer,
            @ParameterName("license") String license,
            @ParameterName("collections")String addCollection,
            @ParameterName("pathtype") String pathtype,
            @ParameterName("indexationType") String scheduleStrategy
    ) {
        try {


            File inputDataDir = null;
            if (pathtype == null || pathtype.equals("relative")) {
                inputDataDir = new File(KConfiguration.getInstance().getProperty( "import.directory")+File.separator+importDirFromArgs);
            } else {
                inputDataDir = new File(importDirFromArgs);
            }



            LOGGER.info("--- Starting method: ndkmets ---");
            LOGGER.info("Parameter 'policy': " + policy);
            LOGGER.info("Parameter 'inputDataDir': " + inputDataDir);
            LOGGER.info("Parameter 'startIndexer': " + startIndexerFromArgs);
            LOGGER.info("Parameter 'useIIPServer': " + (useIIPServer == null ? "N/A" : useIIPServer));
            LOGGER.info("Parameter 'license': " + (license == null ? "N/A" : license));
            LOGGER.info("Parameter 'collections': " + (addCollection == null ? "N/A" : addCollection));
            LOGGER.info("Parameter 'pathtype': " + (pathtype == null ? "N/A" : pathtype));
            LOGGER.info("Parameter 'indexationType': " + (scheduleStrategy == null ? "N/A" : scheduleStrategy));
            LOGGER.info("---------------------------------");

            String exportRoot = KConfiguration.getInstance().getConfiguration().getString("convert.target.directory");

            ScheduleStrategy strategy = ScheduleStrategy.indexRoots;
            if (scheduleStrategy != null) {
                strategy = ScheduleStrategy.fromArg(scheduleStrategy);
            }
            if (useIIPServer) {
                System.setProperty("convert.useImageServer", useIIPServer.toString());
                LOGGER.info(String.format("convert.useImageServer %s", useIIPServer.toString()));
            }
            LOGGER.info(String.format("Starting convert directory %s", inputDataDir.getAbsolutePath()));
            new MetsConvertor().run(inputDataDir.getAbsolutePath(), exportRoot, policy != null ?  policy.toLowerCase().equals("private") : false, startIndexerFromArgs, null, license,addCollection, strategy);
        } catch (JAXBException | IOException | InterruptedException | SAXException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }
}
