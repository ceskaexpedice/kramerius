package cz.kramerius.searchIndex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.execution.Counters;
import cz.kramerius.searchIndex.indexer.execution.IndexationType;
import cz.kramerius.searchIndex.indexer.execution.Indexer;
import cz.kramerius.searchIndex.indexer.execution.ProgressListener;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static cz.incad.kramerius.processes.utils.ProcessUtils.extractPids;


/**
 * Deklarace procesu je v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st (new_indexer_index_object)
 */
public class NewIndexerProcessIndexObject {

    public static final Logger LOGGER = Logger.getLogger(NewIndexerProcessIndexObject.class.getName());
    public static final String PIDLIST_FILE_PREFIX = "pidlist_file:";

    /**
     * args[0] - authToken
     * args[1] - indexation type
     * args[2] - pid
     * args[3] - ignore inconsistent objects - if indexer should continue, or fail when meeting object that is inconsistent in repository
     * args[4...] - optional title
     */
    public static void indexerMain(
            String type,
            String pidsP,
            Boolean ignoreInconsistentObjects,
            String title
    ) {
        LOGGER.info(String.format("Type of indexation %s",type));
        PluginContext pluginContext = PluginContextHolder.getContext();

        LOGGER.info(String.format( "Extracting argument %s", pidsP));
        List<String> pids = extractPids(pidsP);
        
        //tady je problem v tom, ze pokud jeden z parametru obsahuje carku, tak Kramerius pri parsovani argumentu z pole v databazi to vyhodnoti jako vice argumentu.
        //napr.
        //["TREE_AND_FOSTER_TREES", "uuid:23345cf7-7e62-47e9-afad-018624a19ea6", "Quartet A minor, op. 51, no. 2. Andante moderato"] se pri registraci procesu ulozi a po jeho spusteni nacte jako:
        //["TREE_AND_FOSTER_TREES", "uuid:23345cf7-7e62-47e9-afad-018624a19ea6", "Quartet A minor", " op. 51", " no. 2. Andante moderato"]
        //proto nazev, co muze obsahovat carku, pouzivam jako posledni argument
        //String title = shortenIfTooLong(mergeArraysEnd(args, argsIndex), 256);

        //zmena nazvu
        //TODO: mozna spis abstraktni proces s metodou updateName() a samotny kod procesu by mel callback na zjisteni nazvu, kterym by se zavolal updateName()

        if (pidsP.startsWith("pidlist_file")) {
            pluginContext.updateProcessName(title != null
                    ? String.format("Indexace %s (%s, typ %s)", title, pidsP.substring(PIDLIST_FILE_PREFIX.length()), type)
                    : String.format("Indexace %s (typ %s)",pidsP.substring(PIDLIST_FILE_PREFIX.length()), type));
        } else {
            pluginContext.updateProcessName(title != null
                    ? String.format("Indexace %s (%s, typ %s)", title, pids.toString(), type)
                    : String.format("Indexace %s (typ %s)", pids.toString(), type));
        }

        SolrConfig solrConfig = new SolrConfig();

        //access to repository through new public HTTP APIs
        /*RepositoryAccessImplByKrameriusNewApis.Credentials krameriusCredentials = new RepositoryAccessImplByKrameriusNewApis.Credentials(krameriusApiAuthClient, krameriusApiAuthUid, krameriusApiAuthAccessToken);
        FedoraAccess repository = new RepositoryAccessImplByKrameriusNewApis(krameriusBackendBaseUrl, krameriusCredentials);*/

        //access to repository through java directly (injected cz.incad.kramerius.FedoraAccess)
        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
        Indexer indexer = new Indexer(akubraRepository, solrConfig, System.out, ignoreInconsistentObjects);
        LOGGER.info(" --- PIDS PROCESSING --- ");
        Counters counters = new Counters();
        try {
            for (int i = 0; i < pids.size(); i++) {
                String pid = pids.get(i);
                LOGGER.info(String.format("Processing pid '%s'; %d of %d ", pid, i, pids.size()));
                indexer.indexByObjectPid(pid, IndexationType.valueOf(type), counters, false, new ProgressListener() {
                    @Override
                    public void onProgress(int processed) {
                        //log number of objects processed so far
                        if (processed < 100 && processed % 10 == 0 ||
                                processed < 1000 && processed % 100 == 0 ||
                                processed % 1000 == 0
                        ) {
                            LOGGER.info("objects processed so far: " + processed);
                        }
                    }

                    @Override
                    public void onFinished(int processed) {
                    }
                });
            }
        } catch (RuntimeException e) {
            throw e;
        } finally {
            indexer.summary(pids, counters);
            indexer.commmit(counters);
        }
    }

    //FIXME: duplicate code (same method in NewIndexerProcessIndexObject, SetPolicyProcess), use abstract/utility class, but not before bigger cleanup in process scheduling
    //["Quartet A minor", " op. 51", " no. 2. Andante moderato"] => "Quartet A minor, op. 51, no. 2 Andante moderato"
    private static String mergeArraysEnd(String[] args, int argsIndex) {
        String result = "";
        for (int i = argsIndex; i < args.length; i++) {
            String arg = args[i];
            if (arg != null && !"null".equals(arg)) {
                result += args[i];
                if (i != args.length - 1) {
                    result += ",";
                }
            }
        }
        result = result.trim();
        return result.isEmpty() ? null : result;
    }

    //FIXME: duplicate code (same method in NewIndexerProcessIndexObject, SetPolicyProcess), use abstract/utility class, but not before bigger cleanup in process scheduling
    private static String shortenIfTooLong(String string, int maxLength) {
        if (string == null || string.isEmpty() || string.length() <= maxLength) {
            return string;
        } else {
            String suffix = "...";
            return string.substring(0, maxLength - suffix.length()) + suffix;
        }
    }
}
