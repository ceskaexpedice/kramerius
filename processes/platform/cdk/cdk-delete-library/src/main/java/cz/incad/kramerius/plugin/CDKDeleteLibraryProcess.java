package cz.incad.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

public class CDKDeleteLibraryProcess {

    private static final Logger LOGGER = Logger.getLogger(CDKDeleteLibraryProcess.class.getName());

    @ProcessMethod
    public static void deleteLibraryMain(
            @ParameterName("destinationUrl") @IsRequired String destinationUrl,
            @ParameterName("library") @IsRequired String library,
            @ParameterName("filterQuery") String filterQuery,
            @ParameterName("rows") Integer rows,
            @ParameterName("reportEvery") Integer reportEvery,
            @ParameterName("onlyShowConfiguration") Boolean onlyShowConfiguration
    ) throws Exception {

        LOGGER.info("--- Starting method: deleteLibraryMain ---");
        LOGGER.info(String.format("destinationUrl=%s", destinationUrl));
        LOGGER.info(String.format("library=%s", library));
        LOGGER.info(String.format("filterQuery=%s", filterQuery));

        CDKDeleteLibrary.deleteLibrary(
                destinationUrl,
                library,
                filterQuery,
                rows != null ? rows : 300,
                reportEvery != null ? reportEvery : 5000,
                Boolean.TRUE.equals(onlyShowConfiguration));
    }


    public static void main(String[] args) throws Exception {
        runDeleteLibraryTest();
        //runDeleteLibraryKKV();
    }

    private static void runDeleteLibraryTest() throws Exception {
        String destinationUrl = "http://localhost:8983/solr/search_cdk_v1";
        String library = "tul";
        //String filterQuery = "pid:\"uuid:77274343-b156-4287-99ae-3a2d25e20ee7\"";
        String filterQuery = "";
        Integer rows = 20;
        Integer reportEvery = 5000;
        Boolean onlyShowConfiguration = false;

        CDKDeleteLibraryProcess.deleteLibraryMain(
                destinationUrl,
                library,
                filterQuery,
                rows,
                reportEvery,
                onlyShowConfiguration);
    }

    private static void runDeleteLibraryKKV() throws Exception {
        String destinationUrl = "http://localhost:8983/solr/search_cdk_v1";
        String library = "kkkv";
        //String filterQuery = "pid:\"uuid:31121f1c-649f-4deb-8e9e-501ed5780520\"";
        String filterQuery = "";
        Integer rows = 20;
        Integer reportEvery = 5000;
        Boolean onlyShowConfiguration = false;

        CDKDeleteLibraryProcess.deleteLibraryMain(
                destinationUrl,
                library,
                filterQuery,
                rows,
                reportEvery,
                onlyShowConfiguration);
    }

}
