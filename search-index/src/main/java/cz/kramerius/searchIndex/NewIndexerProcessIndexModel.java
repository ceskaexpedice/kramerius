package cz.kramerius.searchIndex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.processes.utils.Utils;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.kramerius.adapters.FedoraAccess;
import cz.kramerius.adapters.IResourceIndex;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexerProcess.IndexerProcess;
import cz.kramerius.searchIndex.repositoryAccess.KrameriusRepositoryAccessAdapter;
import cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNewApi.ResourceIndexImplByKrameriusNewApis;
import cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNoApi.RepositoryAccessImplByKrameriusDirect;

import java.io.IOException;
import java.util.logging.Logger;

public class NewIndexerProcessIndexModel {
    public static final Logger LOGGER = Logger.getLogger(NewIndexerProcessIndexObject.class.getName());
    public static final String ID = "new_indexer_index_model";
    public static final String PARAM_PID = "pid";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_INDEX_NOT_INDEXED = "index_not_indexed";
    public static final String PARAM_INDEX_RUNNING_OR_ERROR = "index_running_or_error";
    public static final String PARAM_INDEX_INDEXED = "index_indexed";
    public static final String PARAM_INDEX_INDEXED_OUTDATED = "index_indexed_outdated";

    public static final String API_AUTH_HEADER_AUTH_TOKEN = "process-auth-token";

    public static void main(String[] args) throws IOException {
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
        String type = args[argsIndex++];
        String pid = args[argsIndex++];
        //what to index
        boolean indexNotIndexed = Boolean.valueOf(args[argsIndex++]);
        boolean indexRunningOrError = Boolean.valueOf(args[argsIndex++]);
        boolean indexIndexedOutdated = Boolean.valueOf(args[argsIndex++]);
        boolean indexIndexed = Boolean.valueOf(args[argsIndex++]);

        if (!pid.startsWith("model:")) {
            LOGGER.severe("Špatný formát pidu modelu:" + pid);
            ProcessStarter.updateStatus(States.FAILED);
            return;
        }
        String model = pid.substring("model:".length());
        ProcessStarter.updateName(String.format("Indexace %s (typ %s)", pid, type));
        if (!indexNotIndexed && !indexRunningOrError && !indexIndexedOutdated && !indexIndexed) {
            LOGGER.info("Podle kombinace filtrů není co indexovat, končím");
            return;
        }

        SolrConfig solrConfig = new SolrConfig(solrBaseUrl, solrCollection, solrUseHttps, solrLogin, solrPassword);
        //TODO: merge KrameriusIndexerProcess and IndexerProcess

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
        IndexerProcess process = new IndexerProcess(repositoryAdapter, solrConfig, System.out);
        process.indexByModel(model, type, indexNotIndexed, indexRunningOrError, indexIndexedOutdated, indexIndexed);

        LOGGER.info("Indexace dokončena");
        LOGGER.info("Ceková doba: " + Utils.formatTime(System.currentTimeMillis() - start));
    }
}
