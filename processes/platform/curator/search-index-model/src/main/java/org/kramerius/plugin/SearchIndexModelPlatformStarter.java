package org.kramerius.plugin;

import cz.kramerius.searchIndex.NewIndexerProcessIndexModel;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.IOException;
import java.util.logging.Logger;

public class SearchIndexModelPlatformStarter {

    public static final Logger LOGGER = Logger.getLogger(SearchIndexModelPlatformStarter.class.getName());

    @ProcessMethod
    public static void indexMain(
            @ParameterName("indexationType") String indexationType,
            @ParameterName("modelPid") String modelPid,
            @ParameterName("indexNotIndexed") Boolean indexNotIndexed,
            @ParameterName("indexRunningOrError") Boolean indexRunningOrError,
            @ParameterName("indexIndexedOutdated") Boolean indexIndexedOutdated,
            @ParameterName("indexIndexed") Boolean indexIndexed,
            @ParameterName("ignoreInconsistentObjects") Boolean ignoreInconsistentObjects,
            @ParameterName("updateProcessName") Boolean updateProcessName
    ) {

        LOGGER.info("--- Starting method: indexMain ---");
        LOGGER.info("Parameter 'indexationType': " + (indexationType == null ? "N/A" : indexationType));
        LOGGER.info("Parameter 'modelPid': " + (modelPid == null ? "N/A" : modelPid));

        // Boolean parameters check for null
        LOGGER.info("Parameter 'indexNotIndexed': " + (indexNotIndexed == null ? "N/A" : indexNotIndexed));
        LOGGER.info("Parameter 'indexRunningOrError': " + (indexRunningOrError == null ? "N/A" : indexRunningOrError));
        LOGGER.info("Parameter 'indexIndexedOutdated': " + (indexIndexedOutdated == null ? "N/A" : indexIndexedOutdated));
        LOGGER.info("Parameter 'indexIndexed': " + (indexIndexed == null ? "N/A" : indexIndexed));
        LOGGER.info("Parameter 'ignoreInconsistentObjects': " + (ignoreInconsistentObjects == null ? "N/A" : ignoreInconsistentObjects));
        LOGGER.info("Parameter 'updateProcessName': " + (updateProcessName == null ? "N/A" : updateProcessName));
        LOGGER.info("---------------------------------");

        try {
            NewIndexerProcessIndexModel.indexMain(indexationType, modelPid,
                    indexNotIndexed,
                    indexRunningOrError,
                    indexIndexedOutdated, indexIndexed,
                    ignoreInconsistentObjects, updateProcessName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}