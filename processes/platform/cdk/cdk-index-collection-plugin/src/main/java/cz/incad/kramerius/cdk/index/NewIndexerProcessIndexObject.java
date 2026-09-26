package cz.incad.kramerius.cdk.index;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static cz.incad.kramerius.processes.utils.ProcessUtils.extractPids;


/**
 * Deklarace procesu je v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st (new_indexer_index_object)
 */
public class NewIndexerProcessIndexObject {

    public static final Logger LOGGER = Logger.getLogger(NewIndexerProcessIndexObject.class.getName());
    public static final String PIDLIST_FILE_PREFIX = "pidlist_file:";

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

        /*
        if (pidsP.startsWith("pidlist_file")) {
            String titleToUpdate = title != null
                    ? String.format("Indexace %s (%s, typ %s)", title, pidsP.substring(PIDLIST_FILE_PREFIX.length()), type)
                    : String.format("Indexace %s (typ %s)",pidsP.substring(PIDLIST_FILE_PREFIX.length()), type);

            LOGGER.info(String.format("Process title %s", titleToUpdate));
            pluginContext.updateProcessName(titleToUpdate);
        } else {
            List<String> titlePids = pids.isEmpty() ? new ArrayList<>() :  pids.subList(0, Math.min(5,pids.size()));
            String titleToUpdate = title != null
                    ? String.format("Indexace %s (%s, typ %s)", title, titlePids.toString(), type)
                    : String.format("Indexace %s (typ %s)", titlePids.toString(), type);
            LOGGER.info(String.format("Process title %s", titleToUpdate));

            pluginContext.updateProcessName(titleToUpdate);
        }

         */

        SolrConfig solrConfig = new SolrConfig();

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
            boolean commitAfterBatch = KConfiguration.getInstance().getConfiguration().getBoolean("solrSearch.commitAfterBatch", true);
            if (commitAfterBatch) {
                indexer.commmit(counters);
            }
        }
    }

    /*
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

     */
}
