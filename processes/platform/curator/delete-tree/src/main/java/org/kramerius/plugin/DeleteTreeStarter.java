package org.kramerius.plugin;

import cz.incad.kramerius.DeleteTreeProcess;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.IOException;
import java.util.logging.Logger;

public class DeleteTreeStarter {

    public static final Logger LOGGER = Logger.getLogger(DeleteTreeStarter.class.getName());

    @ProcessMethod
    public static void deleteMain(
            @ParameterName("pid") String pid,
            @ParameterName("title") String titleP,
            @ParameterName("ignoreIncosistencies") Boolean ignoreIncosistencies
       ) throws IOException, SolrServerException {

        LOGGER.info("--- Starting method: deleteMain ---");
        LOGGER.info("Parameter 'pid': " + (pid == null ? "N/A" : pid));
        LOGGER.info("Parameter 'title': " + (titleP == null ? "N/A" : titleP));
        LOGGER.info("Parameter 'ignoreIncosistencies': " + (ignoreIncosistencies == null ? "N/A" : ignoreIncosistencies));
        LOGGER.info("----------------------------------");

        DeleteTreeProcess.deleteMain(pid, titleP, ignoreIncosistencies);
    }
}