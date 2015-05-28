package cz.cas.lib.knav;


import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Document;

import cz.cas.lib.knav.indexer.CollectPidForIndexing;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.service.impl.PolicyServiceImpl;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Process sets flag public | private according to configuration
 * @author pavels
 */
public class ApplyMovingWall {

    public static final Logger LOGGER = Logger.getLogger(ApplyMovingWall.class.getName());
    
    
    
    public static void main(String[] args) throws IOException, RightCriteriumException, XPathExpressionException {
        FedoraAccess fa = new FedoraAccessImpl(KConfiguration.getInstance(), null);
        SolrAccess sa = new SolrAccessImpl();
        CollectPidForIndexing coll = new CollectPidForIndexing();
        try {
            for (int i = 0; i < args.length; i++) {
                if (args.length > 0) {
                    for (String pid : args) {
                        movingWall(pid, fa, sa, coll);
                    }
                }
            }
        } finally {
            coll.close();
        }
    }
    
    /**
     * Process one tree and subtree  
     * @param masterPid Starting root of tree
     * @param fa FedoraAccess instance
     * @param sa SolrAccess instance
     * @throws IOException 
     * @throws RightCriteriumException
     * @throws XPathExpressionException 
     */
     static void movingWall(String masterPid, FedoraAccess fa,
            SolrAccess sa, CollectPidForIndexing coll) throws IOException, RightCriteriumException, XPathExpressionException {
        LOGGER.info("Setting public | private flag for pid "+masterPid);
        process(fa, sa, masterPid,coll);
        Set<String> pids = fa.getPids(masterPid);
        for (String onePid : pids) {
            process(fa, sa, onePid,coll);
        }
    }

    private static void process(FedoraAccess fa, SolrAccess sa, String onePid, CollectPidForIndexing coll)
            throws IOException, RightCriteriumException, XPathExpressionException {
        ProcessCriteriumContext ctx = new ProcessCriteriumContext(onePid,fa,sa);
        MovingWall mw = new MovingWall();
        mw.setEvaluateContext(ctx);
        int wall = configuredWall(sa, onePid,KConfiguration.getInstance().getConfiguration());
        LOGGER.info("use configuration value "+wall);
        mw.setCriteriumParamValues(new Object[] { ""+wall });
        EvaluatingResult result = mw.evalute();
        String flagFromRELSEXT = disectFlagFromRELSEXT(onePid, fa);
        if (result == EvaluatingResult.TRUE) {
            LOGGER.info("Set policy flag for '"+onePid+"' to value true ");
            setPolicyFlag(onePid,true,fa,flagFromRELSEXT,coll);
        } else if (result == EvaluatingResult.FALSE)  {
            // set private 
            LOGGER.info("Set policy flag for '"+onePid+"' to value false");
            setPolicyFlag(onePid,false,fa,flagFromRELSEXT, coll);
        } else {
            // wrong data
            LOGGER.warning("cannot set flag for pid "+onePid);
        }
    }

    
    public static String disectFlagFromRELSEXT(String onePid, FedoraAccess fa) throws IOException, XPathExpressionException {
        Document relsExtDocument = fa.getRelsExt(onePid);
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//kramerius:policy/text()");
        Object policy = expr.evaluate(relsExtDocument, XPathConstants.STRING);
        return policy != null ? policy.toString() : null;
    }

    /**
     * Sets policy flag
     * @param b Flag
     * @param fa FedraAccess 
     * @throws IOException 
     */
     static void setPolicyFlag(String pid, boolean b, FedoraAccess fa, String previousState,CollectPidForIndexing coll) throws IOException {
         if (detectChange(b, previousState)) {
             PolicyServiceImpl policy = new PolicyServiceImpl();
             policy.setFedoraAccess(fa);
             policy.setConfiguration(KConfiguration.getInstance());
             policy.setPolicyForNode(pid, b ? "public":"private");
             coll.enqueuePid(pid);
         } else {
             LOGGER.info("no change for pid "+pid);
         }
     }

     
     
     
     /**
      * Detect change 
      * @param b new flag
      * @param previousState previous state
      * @return
      */
     public static boolean detectChange(boolean b, String previousState) {
        // no flag before
         boolean doChange = !StringUtils.isAnyString(previousState);
         // if want to set true and previous state is private
         doChange = doChange ? doChange : (b && previousState.equals("policy:private"));
         // if want to set false and previous state is public
         doChange = doChange ? doChange : (!b && previousState.equals("policy:public"));
        return doChange;
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
