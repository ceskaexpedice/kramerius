package cz.kramerius.searchIndex;

import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.processes.utils.Utils;
import cz.kramerius.adapters.FedoraAccess;
import cz.kramerius.adapters.IResourceIndex;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexerProcess.IndexationType;
import cz.kramerius.searchIndex.indexerProcess.IndexerProcess;
import cz.kramerius.searchIndex.repositoryAccess.KrameriusRepositoryAccessAdapter;
import cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNewApi.RepositoryAccessImplByKrameriusNewApis;
import cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNewApi.ResourceIndexImplByKrameriusNewApis;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Definice procesu je dale v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st
 */
public class KrameriusIndexerProcess {

    public static final Logger LOGGER = Logger.getLogger(KrameriusIndexerProcess.class.getName());
    public static final String ID = "new_indexer";
    public static final String PARAM_PID = "pid";
    public static final String PARAM_TYPE = "type";

    public static final String API_AUTH_HEADER_AUTH_TOKEN = "process-auth-token";

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        //args
        //LOGGER.info("args: " + Arrays.asList(args));
        int argsIndex = 0;
        String authToken = args[argsIndex++]; //auth token always first, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        String type = args[argsIndex++];
        String pid = args[argsIndex++];
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

        //zmena nazvu
        //TODO: mozna spis abstraktni proces s metodou updateName() a samotny kod procesu by mel callback na zjisteni nazvu, kterym by se zavolal updateName()
        ProcessStarter.updateName(String.format("Indexace (typ %s, objekt %s)", type, pid));

        SolrConfig solrConfig = new SolrConfig(solrBaseUrl, solrCollection, solrUseHttps, solrLogin, solrPassword);
        //TODO: merge KrameriusIndexerProcess and IndexerProcess
        RepositoryAccessImplByKrameriusNewApis.Credentials krameriusCredentials = new RepositoryAccessImplByKrameriusNewApis.Credentials(krameriusApiAuthClient, krameriusApiAuthUid, krameriusApiAuthAccessToken);
        FedoraAccess repository = new RepositoryAccessImplByKrameriusNewApis(krameriusBackendBaseUrl, krameriusCredentials);
        IResourceIndex resourceIndex = new ResourceIndexImplByKrameriusNewApis(krameriusBackendBaseUrl);
        //TODO: smazat druhy KrameriusRepositoryAccessAdapter
        KrameriusRepositoryAccessAdapter repositoryAdapter = new KrameriusRepositoryAccessAdapter(repository, resourceIndex);
        IndexerProcess process = new IndexerProcess(repositoryAdapter, solrConfig, System.out);
        process.indexByObjectPid(pid, IndexationType.valueOf(type));
        LOGGER.info("Indexation finished");

        //TODO: zmenit nazev procesu - doplnit nazev objektu (bud odsud, nebo lepe callbackem z procesu)

        LOGGER.info("total duration: " + Utils.formatTime(System.currentTimeMillis() - start));
    }
}
