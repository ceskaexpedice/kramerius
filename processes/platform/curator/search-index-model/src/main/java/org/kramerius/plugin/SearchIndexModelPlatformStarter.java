package org.kramerius.plugin;

import cz.kramerius.searchIndex.NewIndexerProcessIndexModel;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.IOException;

public class SearchIndexModelPlatformStarter {

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