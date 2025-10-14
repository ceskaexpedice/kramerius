package cz.incad.kramerius.service.impl;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.document.model.utils.DCContentUtils;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.service.DeleteService;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteServiceImpl implements DeleteService {

    @Inject
    AkubraRepository akubraRepository;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    List<String> predicates;


    public static final Logger LOGGER = Logger.getLogger(DeleteServiceImpl.class.getName());

    private static final String INFO = "info:fedora/";

    @Override
    public void deleteTree(AkubraRepository repo, String pid, String pidPath, String message, boolean deleteEmptyParents, boolean spawnIndexer) throws IOException, RepositoryException, SolrServerException {

        List<String> pids = akubraRepository.re().getPidsInTree(pid);
        for (String deletingPids : pids) {
            List<ProcessingIndexItem> pairs = akubraRepository.pi().getParents(pid);
            for (ProcessingIndexItem p : pairs) {
                repo.re().removeRelation(p.source(), p.relation(), RepositoryNamespaces.KRAMERIUS_URI, deletingPids);
            }
        }

        boolean purge = KConfiguration.getInstance().getConfiguration().getBoolean("delete.purgeObjects", true);
        for (String s : pids) {
            String p = s.replace(INFO, "");
            if (purge) {
                LOGGER.info("Purging object: " + p);
                try {
                    akubraRepository.delete(p);
                } catch (RepositoryException e) {
                    LOGGER.log(Level.WARNING, "Error while deleting " + p + " due " + e.getMessage(), e);
                }
            } else {
                throw new UnsupportedOperationException("Marking object as deleted is not supported operation");
            }
        }

        if (spawnIndexer) {
            spawnIndexRemover(pid);
        }

        List<ProcessingIndexItem> parents = akubraRepository.pi().getOwnedAndFosteredParents(pid).foster();
        for (ProcessingIndexItem processingIndexItem : parents) {
            String parentPid = processingIndexItem.source();
            boolean parentRemoved = false;

            String finalParentPid = parentPid;
            akubraRepository.re().getRelations(parentPid, RepositoryNamespaces.KRAMERIUS_URI).forEach(rel -> {
                if (rel.getResource().equals(pid)) {
                    akubraRepository.re().removeRelation(finalParentPid, rel.getNamespace(), rel.getLocalName(), pid);
                }
            });

            if (deleteEmptyParents) {
                parentPid = parentPid.replace(INFO, "");
                String parentPidPath = pidPath.replace("/" + pid, "");
                LOGGER.info("Deleting empty parent:" + parentPid + " " + parentPidPath);
                deleteTree(repo, parentPid, parentPidPath, message, deleteEmptyParents, spawnIndexer);
                parentRemoved = true;
            }
            if (!parentRemoved) {
                if (spawnIndexer) {
                    spawnIndexer(pid, parentPid);
                }
            }
        }

    }

    void spawnIndexer(String pid, String parentPid) throws UnsupportedEncodingException {
        IndexerProcessStarter.spawnIndexer(true, "Reindex parent after delete " + pid, parentPid.replace(INFO, ""));
    }

    void spawnIndexRemover(String pid) {
        IndexerProcessStarter.spawnIndexRemover(pid);
    }


    /**
     * args[0] uuid of the root object (without uuid: prefix)
     * args[1] pid_path to root object
     * args[2] deleteEmptyParents (optional)
     *
     * @throws IOException
     */
    @ProcessMethod
    public static void deleteServiceMain(
            @ParameterName("pids") String pids,
            @ParameterName("pidPath") String pidPath,
            @ParameterName("deleteEmptyParents") Boolean deleteEmptyParents,
            @ParameterName("spawnIndexer") Boolean spawnIndexer
    ) throws IOException {
        LOGGER.info("DeleteService: " + pids);
        DeleteServiceImpl inst = new DeleteServiceImpl();
        SolrAccess solrAccess = new SolrAccessImplNewIndex();
        PluginContext pluginContext = PluginContextHolder.getContext();

        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
        inst.akubraRepository = akubraRepository;
        inst.predicates = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.treePredicates"), Functions.toStringFunction());
        inst.solrAccess = solrAccess;

        Map<String, List<DCConent>> dcs = DCContentUtils.getDCS(inst.akubraRepository, solrAccess, Arrays.asList(pids));
        List<DCConent> list = dcs.get(pids);
        DCConent dcConent = DCConent.collectFirstWin(list);
        pluginContext.updateProcessName("Mazání objektu '" + (dcConent != null ? dcConent.getTitle() : "bez názvu") + "'");

        try {
            akubraRepository.pi().doWithCommit(() -> {
                try {
                    inst.deleteTree(akubraRepository, pids, pidPath, "Marked as deleted", deleteEmptyParents, spawnIndexer);
                } catch (IOException e) {
                    throw new RepositoryException(e);
                } catch (SolrServerException e) {
                    throw new RepositoryException(e);
                }
            });
        }finally {
            akubraRepository.shutdown();
        }

        LOGGER.info("DeleteService finished.");
    }

}
