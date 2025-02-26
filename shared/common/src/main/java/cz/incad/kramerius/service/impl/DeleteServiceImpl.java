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
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.service.DeleteService;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;
import org.ceskaexpedice.akubra.utils.ProcessingIndexUtils;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteServiceImpl implements DeleteService {


    /* TODO AK_NEW
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

     */
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

        List<String> pids = RelsExtUtils.getPids(pid, akubraRepository);
        for (String deletingPids : pids) {
            List<Pair<String, String>> pairs = ProcessingIndexUtils.findByTargetPid(pid, akubraRepository);
            for (Pair<String, String> p : pairs) {
                String right = p.getRight();
                String left = p.getLeft();
                repo.relsExtRemoveRelation(left, right, RepositoryNamespaces.KRAMERIUS_URI, deletingPids);
            }
        }

        boolean purge = KConfiguration.getInstance().getConfiguration().getBoolean("delete.purgeObjects", true);
        for (String s : pids) {
            String p = s.replace(INFO, "");
            if (purge) {
                LOGGER.info("Purging object: " + p);
                try {
                    akubraRepository.deleteObject(p);
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

        Set<String> parents = ProcessingIndexUtils.getPidsOfParents(pid, akubraRepository).getRight();
        for (String parentPid : parents) {
            boolean parentRemoved = false;

            String finalParentPid = parentPid;
            akubraRepository.relsExtGet(parentPid).getRelations(RepositoryNamespaces.KRAMERIUS_URI).forEach(rel -> {
                if (rel.getResource().equals(pid)) {
                    akubraRepository.relsExtRemoveRelation(finalParentPid, rel.getNamespace(), rel.getLocalName(), pid);
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
    public static void main(final String[] args) throws IOException, RepositoryException, SolrServerException {
        LOGGER.info("DeleteService: " + Arrays.toString(args));
        DeleteServiceImpl inst = new DeleteServiceImpl();
        SolrAccess solrAccess = new SolrAccessImplNewIndex();

        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
        // TODO AK_NEW FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
        inst.akubraRepository = akubraRepository;
        inst.predicates = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.treePredicates"), Functions.toStringFunction());
        inst.solrAccess = solrAccess;

        Map<String, List<DCConent>> dcs = DCContentUtils.getDCS(inst.akubraRepository, solrAccess, Arrays.asList(args[0]));
        List<DCConent> list = dcs.get(args[0]);
        DCConent dcConent = DCConent.collectFirstWin(list);
        ProcessStarter.updateName("Mazání objektu '" + (dcConent != null ? dcConent.getTitle() : "bez názvu") + "'");


        ProcessingIndexUtils.doWithProcessingIndexCommit(inst.akubraRepository, (repo) -> {
            try {
                inst.deleteTree(repo, args[0], args[1], "Marked as deleted", args.length > 2 ? Boolean.parseBoolean(args[2]) : false, args.length > 3 ? Boolean.parseBoolean(args[3]) : true);
            } catch (IOException e) {
                throw new RepositoryException(e);
            } catch (SolrServerException e) {
                throw new RepositoryException(e);
            }
        });

        LOGGER.info("DeleteService finished.");
    }

}
