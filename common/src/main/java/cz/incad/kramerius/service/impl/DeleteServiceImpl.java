package cz.incad.kramerius.service.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.document.model.utils.DCContentUtils;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.service.DeleteService;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.fedora.api.RelationshipTuple;

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

    private List<String> predicates;

    public static final Logger LOGGER = Logger.getLogger(DeleteServiceImpl.class.getName());

    private static final String INFO = "info:fedora/";

    @Override
    public void deleteTree(String pid, String pidPath, String message, boolean deleteEmptyParents) throws IOException {
        Set<String> pids = fedoraAccess.getPids(pid);
        boolean purge = KConfiguration.getInstance().getConfiguration().getBoolean("delete.purgeObjects", true);
        for (String s : pids) {
            String p = s.replace(INFO, "");
            if (purge){
                LOGGER.info("Purging object: "+p);
                try{
                    fedoraAccess.getAPIM().purgeObject(p, message, false);
                }catch(Exception ex){
                    LOGGER.warning("Cannot purge object "+p+", skipping: "+ex);
                }
            }else{
                LOGGER.info("Marking object as deleted: "+p);
                try{
                    fedoraAccess.getAPIM().modifyObject(p, "D", null, null, message);
                }catch(Exception ex){
                    LOGGER.warning("Cannot mark object "+p+" as deleted, skipping: "+ex);
                }
            }
        }
        IndexerProcessStarter.spawnIndexRemover(pidPath, pid);

        List<RelationshipTuple> parents = FedoraUtils.getSubjectPids(pid);
        for (RelationshipTuple parent:parents){
            try{
                String parentPid = parent.getSubject();
                fedoraAccess.getAPIM().purgeRelationship(parentPid, parent.getPredicate(), parent.getObject(), parent.isIsLiteral(), parent.getDatatype());
                LOGGER.info("Removed relation from parent:"+parentPid+" "+ parent.getPredicate()+" "+ parent.getObject());

                boolean parentRemoved = false;
                if (deleteEmptyParents) {

                    List<RelationshipTuple> existingWS = fedoraAccess.getAPIM().getRelationships(parentPid, null);
                    boolean foundRelation = false;
                    outerLoop:
                    for (RelationshipTuple rel : existingWS) {
                        for (String predicate : predicates) {
                            if (rel.getPredicate().endsWith(predicate)) {
                                foundRelation = true;
                                break outerLoop;
                            }
                        }
                    }

                    if (foundRelation)
                        continue;

                    parentPid = parentPid.replace(INFO, "");
                    String parentPidPath = pidPath.replace("/"+pid,"");
                    LOGGER.info("Deleting empty parent:" +parentPid+" "+parentPidPath );
                    deleteTree(parentPid,parentPidPath,message,deleteEmptyParents);
                    parentRemoved = true;

                }
                if (!parentRemoved) {
                    IndexerProcessStarter.spawnIndexer(true, "Reindex parent after delete " + pid, parentPid.replace(INFO, ""));
                }

            }catch (Exception e){
                LOGGER.warning("Cannot delete object relation for"+parent.getSubject()+", skipping: "+e);
            }
        }


    }



    /**
     * args[0] uuid of the root object (without uuid: prefix)
     * args[1] pid_path to root object
     * args[2] deleteEmptyParents (optional)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
        LOGGER.info("DeleteService: "+Arrays.toString(args));


        DeleteServiceImpl inst = new DeleteServiceImpl();
        inst.fedoraAccess = new FedoraAccessImpl(null, null);
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
