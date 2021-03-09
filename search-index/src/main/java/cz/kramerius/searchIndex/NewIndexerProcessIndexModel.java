package cz.kramerius.searchIndex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.processes.utils.Utils;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.java.Pair;
import cz.kramerius.adapters.FedoraAccess;
import cz.kramerius.adapters.IResourceIndex;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexerProcess.IndexationType;
import cz.kramerius.searchIndex.indexerProcess.Indexer;
import cz.kramerius.searchIndex.repositoryAccess.KrameriusRepositoryAccessAdapter;
import cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNewApi.ResourceIndexImplByKrameriusNewApis;
import cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNoApi.RepositoryAccessImplByKrameriusDirect;
import org.apache.solr.client.solrj.SolrServerException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Deklarace procesu je v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st (new_indexer_index_model)
 */
public class NewIndexerProcessIndexModel {
    public static final Logger LOGGER = Logger.getLogger(NewIndexerProcessIndexObject.class.getName());

    public static final String API_AUTH_HEADER_AUTH_TOKEN = "process-auth-token";

    public static void main(String[] args) throws IOException, SolrServerException, RepositoryException {
        long start = System.currentTimeMillis();
        //args
       /* LOGGER.info("args: " + Arrays.asList(args));
        for (String arg : args) {
            System.out.println(arg);
        }*/
        int argsIndex = 0;
        String authToken = args[argsIndex++]; //auth token always first, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        //Kramerius
        String krameriusBackendBaseUrl = args[argsIndex++];
        String krameriusApiAuthClient = args[argsIndex++];
        String krameriusApiAuthUid = args[argsIndex++];
        String krameriusApiAuthAccessToken = args[argsIndex++];
        //SOLR
        String solrBaseUrl = args[argsIndex++];
        String solrCollection = args[argsIndex++];
        boolean solrUseHttps = Boolean.valueOf(args[argsIndex++]);
        String solrLogin = args[argsIndex++];
        String solrPassword = args[argsIndex++];
        //indexation info
        IndexationType type = IndexationType.valueOf(args[argsIndex++]);
        System.out.println("type: " + type);
        String modelPid = args[argsIndex++];
        System.out.println(modelPid);
        //what to index
        Filters filters = new Filters();
        filters.indexNotIndexed = Boolean.valueOf(args[argsIndex++]);
        filters.indexRunningOrError = Boolean.valueOf(args[argsIndex++]);
        filters.indexIndexedOutdated = Boolean.valueOf(args[argsIndex++]);
        filters.indexIndexed = Boolean.valueOf(args[argsIndex++]);
        System.out.println(filters);

        if (!modelPid.startsWith("model:")) {
            LOGGER.severe("Špatný formát pidu modelu:" + modelPid);
            ProcessStarter.updateStatus(States.FAILED);
            return;
        }
        String model = modelPid.substring("model:".length());
        ProcessStarter.updateName(String.format("Indexace %s (typ %s)", modelPid, type));
        if (filters.indexNone()) {
            LOGGER.info("Podle kombinace filtrů není co indexovat, končím");
            return;
        }

        SolrConfig solrConfig = new SolrConfig(solrBaseUrl, solrCollection, solrUseHttps, solrLogin, solrPassword);

        //access to repository through new public HTTP APIs
        /*RepositoryAccessImplByKrameriusNewApis.Credentials krameriusCredentials = new RepositoryAccessImplByKrameriusNewApis.Credentials(krameriusApiAuthClient, krameriusApiAuthUid, krameriusApiAuthAccessToken);
        FedoraAccess repository = new RepositoryAccessImplByKrameriusNewApis(krameriusBackendBaseUrl, krameriusCredentials);*/

        //access to repository through java directly (injected cz.incad.kramerius.FedoraAccess)
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        cz.incad.kramerius.FedoraAccess rawRepositoryAccess = injector.getInstance(Key.get(cz.incad.kramerius.FedoraAccess.class, Names.named("rawFedoraAccess")));
        FedoraAccess repository = new RepositoryAccessImplByKrameriusDirect(rawRepositoryAccess);

        //access to resource index through new public APIs
        IResourceIndex resourceIndex = new ResourceIndexImplByKrameriusNewApis(krameriusBackendBaseUrl);

        KrameriusRepositoryAccessAdapter repositoryAdapter = new KrameriusRepositoryAccessAdapter(repository, resourceIndex);

        Indexer indexer = new Indexer(repositoryAdapter, solrConfig, System.out);


        KrameriusRepositoryApiImpl krameriusRepositoryApi = injector.getInstance(Key.get(KrameriusRepositoryApiImpl.class));

        int processed = 0;
        int nowIgnored = 0;
        int nowIndexed = 0;
        int nowErrors = 0;

        report("TODO: actually run");
        String cursor = "*";
        int limit = 3; //TODO: set to 1000?

        while (cursor != null) {
            RepositoryApi.TitlePidPairs titlePidPairsByModel = krameriusRepositoryApi.getLowLevelApi().getPidsOfObjectsWithTitlesByModelWithCursor(model, true, cursor, limit);
            cursor = cursor.equals(titlePidPairsByModel.nextCursorMark) ? null : titlePidPairsByModel.nextCursorMark;
            processed += titlePidPairsByModel.titlePidPairs.size();
            List<Pair<String, String>> toBeIndexed = filters.indexAll() ? titlePidPairsByModel.titlePidPairs : filter(titlePidPairsByModel.titlePidPairs, filters);
            nowIgnored += titlePidPairsByModel.titlePidPairs.size() - toBeIndexed.size();
            for (Pair<String, String> titlePidPair : toBeIndexed) {
                String title = titlePidPair.getFirst();
                String pid = titlePidPair.getSecond();
                report(String.format("indexing %s (%s)", title, pid));
                //TODO: actually index
                //TODO: use progresslistener
                //indexer.indexByObjectPid(pid, type, null);
                nowIndexed++; //TODO: nowErrors try-catch indexByObjectPid
            }
        }

        //TODO: 2. ziskat stavy objektu stylem getIndexationInfoForPids
        //TODO: 3. podle stavovych filtru zpracovat, nebo preskocit
        //TODO: 5. vypsat vysledny stav

        //TODO: raději všechno česky
        report("Total Summary");
        report("===========================================");
        report(" top-level objects processed: " + processed);
        report(" top-level objects indexed  : " + nowIndexed);
        report(" top-level objects ignored  : " + nowIgnored);
        report(" top-level objects erroneous: " + nowErrors);
        report("===========================================");

        LOGGER.info("Indexace dokončena");
        LOGGER.info("Celková doba: " + Utils.formatTime(System.currentTimeMillis() - start));
    }

    private static List<Pair<String, String>> filter(List<Pair<String, String>> titlePidPairs, Filters filters) {
        List<Pair<String, String>> result = new ArrayList<>();
        //TODO: zeptat se solru na stav (getIndexationInfoForPids) a podle filtru vratit jen to, co sedi
        Random random = new Random();
        for (Pair<String, String> titlePidPair : titlePidPairs) {
            if (random.nextBoolean()) {
                result.add(titlePidPair);
            } else {
                String title = titlePidPair.getFirst();
                String pid = titlePidPair.getSecond();
                report(String.format("ignoring %s (%s)", title, pid));
            }
        }
        return result;
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
