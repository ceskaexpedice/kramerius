package cz.incad.kramerius.service.impl;

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
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.service.DeleteService;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class DeleteServiceImpl implements DeleteService {


    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    KConfiguration configuration;

    @Inject
    IResourceIndex resourceIndex;

    private List<String> predicates;

    public static final Logger LOGGER = Logger.getLogger(DeleteServiceImpl.class.getName());

    private static final String INFO = "info:fedora/";

    @Override
    public void deleteTree(String pid, String pidPath, String message, boolean deleteEmptyParents) throws IOException, RepositoryException, ResourceIndexException {
        Set<String> pids = fedoraAccess.getPids(pid);
        boolean purge = KConfiguration.getInstance().getConfiguration().getBoolean("delete.purgeObjects", true);
        for (String s : pids) {
            String p = s.replace(INFO, "");
            if (purge){
                LOGGER.info("Purging object: "+p);
                fedoraAccess.getInternalAPI().deleteobject(p);
            }else{
                throw new UnsupportedOperationException("This is not supported operaiton");
            }
        }

        IndexerProcessStarter.spawnIndexRemover(pidPath, pid);


        Repository repo = fedoraAccess.getInternalAPI();
        List<String> parents = resourceIndex.getParentsPids(pid);
        for (String parentPid : parents) {
            boolean parentRemoved = false;

            List<Triple<String, String, String>> relations = repo.getObject(parentPid).getRelations(FedoraNamespaces.KRAMERIUS_URI);
            for (Triple<String, String, String> triple: relations) {
                if (triple.getRight().equals(pid)) {
                    repo.getObject(parentPid).removeRelation(triple.getLeft(),triple.getMiddle(),pid);
                }
            }

            if (deleteEmptyParents) {
                parentPid = parentPid.replace(INFO, "");
                String parentPidPath = pidPath.replace("/"+pid,"");
                LOGGER.info("Deleting empty parent:" +parentPid+" "+parentPidPath );
                deleteTree(parentPid,parentPidPath,message,deleteEmptyParents);
                parentRemoved = true;
            }
            if (!parentRemoved) {
                IndexerProcessStarter.spawnIndexer(true, "Reindex parent after delete " + pid, parentPid.replace(INFO, ""));
            }
        }
    }



    /**
     * args[0] uuid of the root object (without uuid: prefix)
     * args[1] pid_path to root object
     * args[2] deleteEmptyParents (optional)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, RepositoryException, ResourceIndexException {

        LOGGER.info("DeleteService: "+Arrays.toString(args));

        DeleteServiceImpl inst = new DeleteServiceImpl();


        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        inst.fedoraAccess = fa;
        inst.predicates =  KConfiguration.getInstance().getConfiguration().getList("fedora.treePredicates");
        SolrAccess solrAccess = new SolrAccessImpl();

        Map<String, List<DCConent>> dcs = DCContentUtils.getDCS(inst.fedoraAccess, solrAccess, Arrays.asList(args[0]));
        List<DCConent> list = dcs.get(args[0]);
        DCConent dcConent = DCConent.collectFirstWin(list);
        ProcessStarter.updateName("Mazání objektu '" + (dcConent != null ? dcConent.getTitle() : "bez názvu") + "'");

        boolean deleteEmptyParents = false;
        if (args.length>2){
            deleteEmptyParents = Boolean.parseBoolean(args[2]);
        }

        inst.deleteTree(args[0], args[1], "Marked as deleted", deleteEmptyParents);


        LOGGER.info("DeleteService finished.");
    }

}
