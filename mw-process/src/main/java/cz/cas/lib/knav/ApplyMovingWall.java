package cz.cas.lib.knav;


import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.configuration.Configuration;

import com.sun.scenario.effect.impl.prism.PrCropPeer;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;
import cz.incad.kramerius.service.impl.PolicyServiceImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Process sets flag public | private according to configuration
 * @author pavels
 */
public class ApplyMovingWall {

    public static final Logger LOGGER = Logger.getLogger(ApplyMovingWall.class.getName());
    
   
    public static void main(String[] args) throws IOException, RightCriteriumException {
        FedoraAccess fa = new FedoraAccessImpl(KConfiguration.getInstance(), null);
        SolrAccess sa = new SolrAccessImpl();
        if (args.length  > 0) {
            for (String pid : args) {
                movingWall(pid, fa, sa);
                IndexerProcessStarter.spawnIndexer(false, "Reindex policy "+pid, pid);
            }
        }
    }
    
    /**
     * Process one tree and subtree  
     * @param masterPid Starting root of tree
     * @param fa FedoraAccess instance
     * @param sa SolrAccess instance
     * @throws IOException 
     * @throws RightCriteriumException
     */
     static void movingWall(String masterPid, FedoraAccess fa,
            SolrAccess sa) throws IOException, RightCriteriumException {
        LOGGER.info("Setting public | private flag for pid "+masterPid);
        process(fa, sa, masterPid);
        Set<String> pids = fa.getPids(masterPid);
        for (String onePid : pids) {
            process(fa, sa, onePid);
        }
    }

    private static void process(FedoraAccess fa, SolrAccess sa, String onePid)
            throws IOException, RightCriteriumException {
        ProcessCriteriumContext ctx = new ProcessCriteriumContext(onePid,fa,sa);
        MovingWall mw = new MovingWall();
        mw.setEvaluateContext(ctx);
        int wall = configuredWall(sa, onePid,KConfiguration.getInstance().getConfiguration());
        LOGGER.info("use configuration value "+wall);
        mw.setCriteriumParamValues(new Object[] { ""+wall });
        EvaluatingResult result = mw.evalute();
        if (result == EvaluatingResult.TRUE) {
            LOGGER.info("Set policy flag for '"+onePid+"' to value true ");
            setPolicyFlag(onePid,true,fa);
        } else if (result == EvaluatingResult.FALSE)  {
            // set private 
            LOGGER.info("Set policy flag for '"+onePid+"' to value false");
            setPolicyFlag(onePid,false,fa);
        } else {
            // wrong data
            LOGGER.warning("cannot set flag for pid "+onePid);
        }
    }

    
    /**
     * Sets policy flag
     * @param b Flag
     * @param fa FedraAccess 
     * @throws IOException 
     */
     static void setPolicyFlag(String pid, boolean b, FedoraAccess fa) throws IOException {
        PolicyServiceImpl policy = new PolicyServiceImpl();
        policy.setFedoraAccess(fa);
        policy.setConfiguration(KConfiguration.getInstance());
        policy.setPolicy(pid, b ? "public":"private");
     }
    
    /**
     * Configured moving wall 
     * @param sa SolrAccess instance 
     * @param onePid Configured pid
     * @return Wall configuration value
     * @throws IOException
     */
     static int configuredWall(SolrAccess sa, String onePid, Configuration conf)
            throws IOException {
        ObjectModelsPath[] pathOfModels = sa.getPathOfModels(onePid);
        ObjectModelsPath path = pathOfModels[0];
        String[] models = path.getPathFromLeafToRoot();
        int wall =conf.getInt("mwprocess.wall",70);
        for (String model : models) {
            if (conf.containsKey("mwprocess.model."+model+".wall")) {
                wall = conf.getInt("mwprocess.model."+model+".wall");
            }
        }
        return wall;
    }
    
}
