package cz.kramerius.searchIndex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.kramerius.adapters.ProcessingIndex;
import cz.kramerius.adapters.RepositoryAccess;
import cz.kramerius.adapters.impl.krameriusNewApi.ProcessingIndexImplByKrameriusNewApis;
import cz.kramerius.adapters.impl.krameriusNoApi.RepositoryAccessImplByKrameriusDirect;
import cz.kramerius.krameriusRepositoryAccess.KrameriusRepositoryFascade;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.execution.IndexationType;
import cz.kramerius.searchIndex.indexer.execution.Indexer;
import cz.kramerius.searchIndex.indexer.execution.ProgressListener;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.utils.ProcessingIndexUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deklarace procesu je v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st (new_indexer_index_model)
 */
public class NewIndexerProcessIndexModel {
    public static final Logger LOGGER = Logger.getLogger(NewIndexerProcessIndexModel.class.getName());

    /**
     * args[0] - authToken
     * args[1] - indexation type
     * args[2] - model
     * args[3] - ignore inconsistent objects - if indexer should continue, or fail when meeting object that is inconsistent in repository
     * args[4] - index objects that are not indexed
     * args[5] - index objects that running or erroneously indexed
     * args[6] - index objects that indexed with older version of indexer
     * args[7] - index objects that indexed with current version of indexer
     * args[8]  - optional - if false, do not update Process name, just log (for running as standalone process)
     */
    public static void main(String[] args) throws IOException, SolrServerException, RepositoryException {
        //args
       /* LOGGER.info("args: " + Arrays.asList(args));
        for (String arg : args) {
            System.out.println(arg);
        }*/
        if (args.length < 8) {
            throw new RuntimeException("Not enough arguments.");
        }
        int argsIndex = 0;
        //token for keeping possible following processes in same batch
        String authToken = args[argsIndex++]; //auth token always second, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        //process params
        IndexationType type = IndexationType.valueOf(args[argsIndex++]);
        report("type: " + type);
        String modelPid = args[argsIndex++];
        report(modelPid);
        Boolean ignoreInconsistentObjects = Boolean.valueOf(args[argsIndex++]);
        //what to index
        Filters filters = new Filters();
        filters.indexNotIndexed = Boolean.valueOf(args[argsIndex++]);
        filters.indexRunningOrError = Boolean.valueOf(args[argsIndex++]);
        filters.indexIndexedOutdated = Boolean.valueOf(args[argsIndex++]);
        filters.indexIndexed = Boolean.valueOf(args[argsIndex++]);
        report(filters.toString());

        if (!modelPid.startsWith("model:")) {
            LOGGER.severe("Špatný formát pidu modelu:" + modelPid);
            ProcessStarter.updateStatus(States.FAILED);
            return;
        }
        String model = modelPid.substring("model:".length());
        if (args.length>8 && !Boolean.valueOf(args[8])){
            LOGGER.info(String.format("Indexace %s (typ %s)", modelPid, type));
        }else {
            ProcessStarter.updateName(String.format("Indexace %s (typ %s)", modelPid, type));
        }
        if (filters.indexNone()) {
            LOGGER.info("Podle kombinace filtrů není co indexovat, končím");
            return;
        }

        SolrConfig solrConfig = new SolrConfig();

        //access to repository through new public HTTP APIs
        /*RepositoryAccessImplByKrameriusNewApis.Credentials krameriusCredentials = new RepositoryAccessImplByKrameriusNewApis.Credentials(krameriusApiAuthClient, krameriusApiAuthUid, krameriusApiAuthAccessToken);
        FedoraAccess repository = new RepositoryAccessImplByKrameriusNewApis(krameriusBackendBaseUrl, krameriusCredentials);*/

        //access to repository through java directly (injected cz.incad.kramerius.FedoraAccess)
        Injector injector = Guice.createInjector(new SearchIndexModule(), new NullStatisticsModule(), new SolrModule(), new RepoModule(), new ResourceIndexModule());

        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));

//        cz.incad.kramerius.FedoraAccess rawRepositoryAccess = injector.getInstance(Key.get(cz.incad.kramerius.FedoraAccess.class, Names.named("rawFedoraAccess")));
//        FedoraAccess repository = new RepositoryAccessImplByKrameriusDirect(rawRepositoryAccess);

        

        //Injector injector = Guice.createInjector(new SearchIndexModule(), new NullStatisticsModule(), new SolrModule(), new RepoModule());
        cz.incad.kramerius.FedoraAccess rawRepository = injector.getInstance(Key.get(cz.incad.kramerius.FedoraAccess.class, Names.named("rawFedoraAccess")));
        RepositoryAccess repository = new RepositoryAccessImplByKrameriusDirect(rawRepository);

        //access to processing index through new public APIs
        ProcessingIndex processingIndex = new ProcessingIndexImplByKrameriusNewApis(akubraRepository, ProcessUtils.getCoreBaseUrl());

        KrameriusRepositoryFascade krameriusRepositoryFascade = new KrameriusRepositoryFascade(repository, processingIndex);
        Indexer indexer = new Indexer(krameriusRepositoryFascade, solrConfig, System.out, ignoreInconsistentObjects);

        SolrAccess solrAccess = filters.indexAll() ? null : injector.getInstance(Key.get(SolrAccess.class, Names.named("new-index")));

        int processed = 0;
        int nowIgnored = 0;
        int nowIndexed = 0;
        int nowErrors = 0;
        final int[] totalObjectProcessed = {0};

        String cursor = "*";
        int limit = 100;
        while (cursor != null) {
            Pair titlePidPairsByModel = ProcessingIndexUtils.getPidsOfObjectsWithTitlesByModelWithCursor(model, true, cursor, limit, akubraRepository);
            String nextCursorMark = (String) titlePidPairsByModel.getRight();
            List<Pair<String, String>> titleIdPairs = (List<Pair<String, String>>) titlePidPairsByModel.getLeft();
            cursor = cursor.equals(titlePidPairsByModel.getRight()) ? null : nextCursorMark;
            processed += titleIdPairs.size();
            List<Pair<String, String>> toBeIndexed = filters.indexAll() ? titleIdPairs : filter(solrAccess, titleIdPairs, filters);
            nowIgnored += titleIdPairs.size() - toBeIndexed.size();
            for (Pair<String, String> titlePidPair : toBeIndexed) {
                String title = titlePidPair.getLeft();
                String pid = titlePidPair.getRight();
                //report(String.format("indexing %s: %s", pid, title));
                try {
                    indexer.indexByObjectPid(pid, type, new ProgressListener() {
                        @Override
                        public void onProgress(int processed) {
                            totalObjectProcessed[0]++;
                            //log number of objects processed so far
                            if (totalObjectProcessed[0] < 100 && totalObjectProcessed[0] % 10 == 0 ||
                                    totalObjectProcessed[0] < 1000 && totalObjectProcessed[0] % 100 == 0 ||
                                    totalObjectProcessed[0] % 1000 == 0
                            ) {
                                LOGGER.info("objects processed so far: " + totalObjectProcessed[0]);
                            }
                        }

                        @Override
                        public void onFinished(int processed) {
                            
                        }
                    });
                    nowIndexed++;
                } catch (Throwable e) {
                    e.printStackTrace();
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    nowErrors++;
                }
            }
        }

        report(" ");
        report("Top-level summary");
        report("===========================================");
        report(" Top-level objects processed:   " + processed);
        report(" Top-level objects indexed:     " + nowIndexed);
        report(" Top-level objects ignored:     " + nowIgnored);
        report(" Top-level objects erroneous:   " + nowErrors);
        report(" Top-level objects erroneous:   " + nowErrors);
        report("     Total objects processed:   " + totalObjectProcessed[0]);
        report("===========================================");
        
        if (nowErrors > 0 || nowIgnored >0) {
            throw new IllegalStateException("Indexation finished with errors; see error log");
        }
        
    }

    private static List<Pair<String, String>> filter(SolrAccess solrAccess, List<Pair<String, String>> titlePidPairs, Filters filters) throws IOException {
        List<Pair<String, String>> result = new ArrayList<>();
        if (titlePidPairs.isEmpty()) {
            return result;
        }
        String q = "";
        for (int i = 0; i < titlePidPairs.size(); i++) {
            String pid = titlePidPairs.get(i).getRight();
            q += "pid:" + pid.replace(":", "\\:");
            if (i != titlePidPairs.size() - 1) {
                q += " OR ";
            }
        }
        String query = "fl=pid,indexer_version,full_indexation_in_progress&rows=" + titlePidPairs.size() + "&q=" + URLEncoder.encode(q, "UTF-8");
        JSONObject jsonObject = solrAccess.requestWithSelectReturningJson(query);
        JSONArray docs = jsonObject.getJSONObject("response").getJSONArray("docs");
        Map<String, JSONObject> docByPid = new HashMap<>();
        for (int i = 0; i < docs.length(); i++) {
            JSONObject doc = docs.getJSONObject(i);
            docByPid.put(doc.getString("pid"), doc);
        }
        for (Pair<String, String> titlePidPair : titlePidPairs) {
            String title = titlePidPair.getLeft();
            String pid = titlePidPair.getRight();
            JSONObject jsonDoc = docByPid.get(pid);
            boolean inIndex = jsonDoc != null;
            int indexerVersion = jsonDoc == null ? -1 : (jsonDoc.has("indexer_version") ? jsonDoc.getInt("indexer_version") : 0);
            boolean runningOrError = jsonDoc == null ? false : (jsonDoc.has("full_indexation_in_progress") ? jsonDoc.getBoolean("full_indexation_in_progress") : false);
            if (shouldIndex(filters, inIndex, indexerVersion, runningOrError)) {
                result.add(titlePidPair);
            } else {
                //report(String.format("Ignoring %s: %s", pid, title));
            }
        }
        return result;
    }

    private static boolean shouldIndex(Filters filters, boolean inIndex, int indexerVersion, boolean runningOrError) {
        return filters.indexNotIndexed && !inIndex ||
                filters.indexRunningOrError && runningOrError ||
                filters.indexIndexedOutdated && (indexerVersion < Indexer.INDEXER_VERSION) ||
                filters.indexIndexed && (indexerVersion == Indexer.INDEXER_VERSION);
    }

    private static void report(String message) {
        System.out.println(message);
    }

    private static class Filters {
        boolean indexNotIndexed = false;
        boolean indexRunningOrError = false;
        boolean indexIndexedOutdated = false;
        boolean indexIndexed = false;

        boolean indexAll() { //optimalizace: neni treba ptat vyhledavaciho indexu na stav objektu
            return indexNotIndexed && indexRunningOrError && indexIndexedOutdated && indexIndexed;
        }

        boolean indexNone() { //optimalizace: nema smysl ani iterovat objekty, rovnou koncim
            return !indexNotIndexed && !indexRunningOrError && !indexIndexedOutdated && !indexIndexed;
        }

        @Override
        public String toString() {
            return "Filters{" +
                    "indexNotIndexed=" + indexNotIndexed +
                    ", indexRunningOrError=" + indexRunningOrError +
                    ", indexIndexedOutdated=" + indexIndexedOutdated +
                    ", indexIndexed=" + indexIndexed +
                    '}';
        }
    }

}
