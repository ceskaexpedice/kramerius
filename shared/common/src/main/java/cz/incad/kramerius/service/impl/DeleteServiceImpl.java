package cz.incad.kramerius.service.impl;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.document.model.utils.DCContentUtils;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.service.DeleteService;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteServiceImpl implements DeleteService {


    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;


    @Inject
    IResourceIndex resourceIndex;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    List<String> predicates;


    public static final Logger LOGGER = Logger.getLogger(DeleteServiceImpl.class.getName());

    private static final String INFO = "info:fedora/";

    @Override
    public void deleteTree(Repository repo, String pid, String pidPath, String message, boolean deleteEmptyParents, boolean spawnIndexer) throws IOException, RepositoryException, ResourceIndexException, SolrServerException {

        List<String> pids = fedoraAccess.getPids(pid);
        for (String deletingPids : pids) {
            List<Pair<String, String>> pairs = repo.getProcessingIndexFeeder().findByTargetPid(pid);
            for (Pair<String, String> p : pairs) {
                String right = p.getRight();
                String left = p.getLeft();
                repo.getObject(left).removeRelation(right, FedoraNamespaces.KRAMERIUS_URI, deletingPids);
            }
        }

        boolean purge = KConfiguration.getInstance().getConfiguration().getBoolean("delete.purgeObjects", true);
        for (String s : pids) {
            String p = s.replace(INFO, "");
            if (purge) {
                LOGGER.info("Purging object: " + p);
                try {
                    fedoraAccess.getInternalAPI().deleteObject(p);
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

        List<String> parents = resourceIndex.getParentsPids(pid);
        for (String parentPid : parents) {
            boolean parentRemoved = false;

            List<Triple<String, String, String>> relations = repo.getObject(parentPid).getRelations(FedoraNamespaces.KRAMERIUS_URI);
            for (Triple<String, String, String> triple : relations) {
                if (triple.getRight().equals(pid)) {
                    repo.getObject(parentPid).removeRelation(triple.getLeft(), triple.getMiddle(), pid);
                }
            }

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
    public static void main(final String[] args) throws IOException, RepositoryException, ResourceIndexException, SolrServerException {
        LOGGER.info("DeleteService: " + Arrays.toString(args));
        DeleteServiceImpl inst = new DeleteServiceImpl();
        SolrAccess solrAccess = new SolrAccessImplNewIndex();

        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        inst.fedoraAccess = fa;
        inst.predicates = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.treePredicates"), Functions.toStringFunction());
        inst.resourceIndex = injector.getInstance(IResourceIndex.class);
        inst.solrAccess = solrAccess;

        Map<String, List<DCConent>> dcs = DCContentUtils.getDCS(inst.fedoraAccess, solrAccess, Arrays.asList(args[0]));
        List<DCConent> list = dcs.get(args[0]);
        DCConent dcConent = DCConent.collectFirstWin(list);
        ProcessStarter.updateName("Mazání objektu '" + (dcConent != null ? dcConent.getTitle() : "bez názvu") + "'");


        Fedora4Utils.doWithProcessingIndexCommit(inst.fedoraAccess.getInternalAPI(), (repo) -> {
            try {
                inst.deleteTree(repo, args[0], args[1], "Marked as deleted", args.length > 2 ? Boolean.parseBoolean(args[2]) : false, args.length > 3 ? Boolean.parseBoolean(args[3]) : true);
            } catch (IOException e) {
                throw new RepositoryException(e);
            } catch (ResourceIndexException e) {
                throw new RepositoryException(e);
            } catch (SolrServerException e) {
                throw new RepositoryException(e);
            }
        });

        LOGGER.info("DeleteService finished.");
    }

}
