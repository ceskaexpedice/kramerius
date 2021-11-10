package cz.kramerius.searchIndex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.new_api.IndexationScheduler;
import cz.incad.kramerius.processes.new_api.IndexationScheduler.ProcessCredentials;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.java.Pair;
import cz.kramerius.adapters.FedoraAccess;
import cz.kramerius.adapters.IResourceIndex;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexerProcess.IndexationType;
import cz.kramerius.searchIndex.indexerProcess.Indexer;
import cz.kramerius.searchIndex.indexerProcess.ProgressListener;
import cz.kramerius.searchIndex.repositoryAccess.KrameriusRepositoryAccessAdapter;
import cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNewApi.ResourceIndexImplByKrameriusNewApis;
import cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNoApi.RepositoryAccessImplByKrameriusDirect;
import org.apache.solr.client.solrj.SolrServerException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Deklarace procesu je v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st (new_indexer_index_model)
 */
public class NewIndexerProcessIndexModel {
    public static final Logger LOGGER = Logger.getLogger(NewIndexerProcessIndexModel.class.getName());

    public static final String API_AUTH_HEADER_AUTH_TOKEN = "process-auth-token";

    public static void main(String[] args) throws IOException, SolrServerException, RepositoryException {
        //args
       /* LOGGER.info("args: " + Arrays.asList(args));
        for (String arg : args) {
            System.out.println(arg);
        }*/
        if (args.length < 4) { //at least 4 args ar necessary: credentials for scheduling another process (in the same batch) after this process has finished
            throw new RuntimeException("Not enough arguments.");
        }
        int argsIndex = 0;
        ProcessCredentials processCredentials = new ProcessCredentials();
        //token for keeping possible following processes in same batch
        processCredentials.authToken = args[argsIndex++]; //auth token always first, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        //Kramerius
        processCredentials.krameriusApiAuthClient = args[argsIndex++];
        processCredentials.krameriusApiAuthUid = args[argsIndex++];
        processCredentials.krameriusApiAuthAccessToken = args[argsIndex++];
        //indexation info
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
        ProcessStarter.updateName(String.format("Indexace %s ", modelPid));

        if (filters.indexNone()) {
            LOGGER.info("Podle kombinace filtrů není co indexovat, končím");
            return;
        }

        SolrConfig solrConfig = new SolrConfig(KConfiguration.getInstance());

        //access to repository through new public HTTP APIs
        /*RepositoryAccessImplByKrameriusNewApis.Credentials krameriusCredentials = new RepositoryAccessImplByKrameriusNewApis.Credentials(krameriusApiAuthClient, krameriusApiAuthUid, krameriusApiAuthAccessToken);
        FedoraAccess repository = new RepositoryAccessImplByKrameriusNewApis(krameriusBackendBaseUrl, krameriusCredentials);*/

        //access to repository through java directly (injected cz.incad.kramerius.FedoraAccess)
        Injector injector = Guice.createInjector(new SearchIndexModule(), new NullStatisticsModule(), new SolrModule(), new RepoModule());
        cz.incad.kramerius.FedoraAccess rawRepositoryAccess = injector.getInstance(Key.get(cz.incad.kramerius.FedoraAccess.class, Names.named("rawFedoraAccess")));
        FedoraAccess repository = new RepositoryAccessImplByKrameriusDirect(rawRepositoryAccess);

        //access to resource index through new public APIs
        IResourceIndex resourceIndex = new ResourceIndexImplByKrameriusNewApis(ProcessUtils.getCoreBaseUrl());

        KrameriusRepositoryAccessAdapter repositoryAdapter = new KrameriusRepositoryAccessAdapter(repository, resourceIndex);
        Indexer indexer = new Indexer(repositoryAdapter, solrConfig, System.out, ignoreInconsistentObjects);

        KrameriusRepositoryApiImpl krameriusRepositoryApi = injector.getInstance(Key.get(KrameriusRepositoryApiImpl.class));
        SolrAccess solrAccess = filters.indexAll() ? null : injector.getInstance(Key.get(SolrAccess.class, Names.named("new-index")));

        int processed = 0;
        int nowIgnored = 0;
        int nowIndexed = 0;
        int nowErrors = 0;
        final int[] totalObjectProcessed = {0};

        String cursor = "*";
        int limit = 100;
        while (cursor != null) {
            RepositoryApi.TitlePidPairs titlePidPairsByModel = krameriusRepositoryApi.getLowLevelApi().getPidsOfObjectsWithTitlesByModelWithCursor(model, true, cursor, limit);
            cursor = cursor.equals(titlePidPairsByModel.nextCursorMark) ? null : titlePidPairsByModel.nextCursorMark;
            processed += titlePidPairsByModel.titlePidPairs.size();
            List<Pair<String, String>> toBeIndexed = filters.indexAll() ? titlePidPairsByModel.titlePidPairs : filter(solrAccess, titlePidPairsByModel.titlePidPairs, filters);
            nowIgnored += titlePidPairsByModel.titlePidPairs.size() - toBeIndexed.size();
            for (Pair<String, String> titlePidPair : toBeIndexed) {
                String title = titlePidPair.getFirst();
                String pid = titlePidPair.getSecond();
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
    }

    private static List<Pair<String, String>> filter(SolrAccess solrAccess, List<Pair<String, String>> titlePidPairs, Filters filters) throws IOException {
        List<Pair<String, String>> result = new ArrayList<>();
        if (titlePidPairs.isEmpty()) {
            return result;
        }
        String q = "";
        for (int i = 0; i < titlePidPairs.size(); i++) {
            String pid = titlePidPairs.get(i).getSecond();
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
            String title = titlePidPair.getFirst();
            String pid = titlePidPair.getSecond();
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
