package cz.cas.lib.knav;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.cas.lib.knav.indexer.CollectPidForIndexing;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.service.impl.PolicyServiceImpl;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.utils.solr.SolrUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Apply MW Utility
 * @author pavels
 */
public class ApplyMWUtils {

    
    /**
     * Apply moving wall on pids in given array
     * @param fa FedoraAccess 
     * @param sa SolrAccess
     * @param coll Collects pids for indexing
     * @param userValue User defined values
     * @param mode Year or month
     * @param pids PIDS array
     * @throws IOException
     * @throws RightCriteriumException
     * @throws XPathExpressionException
     */
    public static void applyMWOverPidsArray(FedoraAccess fa, SolrAccess sa,
            CollectPidForIndexing coll, String userValue, String mode, String[] pids)
            throws IOException, RightCriteriumException,
            XPathExpressionException {
        String title = ApplyMWUtils.updateMovingWallTitle(pids, sa);
        ApplyMovingWall.LOGGER.info("Apply moving wall for " + title);
        try {
            for (int i = 0; i < pids.length; i++) {
                if (pids.length > 0) {
                    for (String pid : pids) {
                        ApplyMWUtils.movingWallOnTree(pid, userValue, mode, fa, sa, coll);
                    }
                }
            }
        } finally {
            if (!coll.hasBeenTouched()) {
                ApplyMovingWall.LOGGER.info("No changes for :"+ title);
            }
            coll.close();
        }
    }


    /**
     * Human readable title of the process
     * 
     * @param pid
     *            Proccess pid
     * @param sa
     *            SolrAccess
     */
    public static String updateMovingWallTitle(String[] pids, SolrAccess sa) {
        try {
            if (pids.length == 0)
                return null;
            Document solrDoc = sa.getSolrDataDocument(pids[0]);
            Element foundElm = XMLUtils.findElement(
                    solrDoc.getDocumentElement(),
                    new XMLUtils.ElementsFilter() {
    
                        @Override
                        public boolean acceptElement(Element element) {
                            String nameAttr = element.getAttribute("name");
    
                            boolean isElmStr = element.getNodeName().equals(
                                    "str");
                            boolean hasGoodAttr = nameAttr != null
                                    && nameAttr.equals("dc.title");
    
                            if (isElmStr && hasGoodAttr) {
                                return true;
                            } else
                                return false;
                        }
                    });
    
            if (foundElm != null) {
    
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < pids.length; i++) {
                    builder.append(pids[i]);
                    if (i > 0) builder.append(",");
                }
                
                String postfix = pids.length == 1 ? "" : ",...";
                String name = "pid(s) [" + builder.toString()
                        + "] - title: " + foundElm.getTextContent() + postfix;
                ProcessStarter.updateName(URLEncoder.encode(name, "UTF-8"));
                return name;
            } else return null;
        } catch (Exception ex) {
            ApplyMovingWall.LOGGER.warning("cannot change name of process");
        }
        return null;
    }

    /**
     * Process one tree and subtree
     * 
     * @param masterPid Starting root of tree
     * @param userValue value of moving wall
     * @param mode mode of moving wall
     * @param fa FedoraAccess instance
     * @param sa SolrAccess instance
     * @throws IOException
     * @throws RightCriteriumException
     * @throws XPathExpressionException
     */
    public static void movingWallOnTree(String masterPid, String userValue, String mode, FedoraAccess fa, SolrAccess sa,
            CollectPidForIndexing coll) throws IOException,
            RightCriteriumException, XPathExpressionException {
        String firstModel = null;
        String firstPid = null;
        Boolean firstIter = true;
        
        ApplyMovingWall.LOGGER.info("Setting public | private flag for pid " + masterPid);
        firstPid = masterPid;
        firstModel = ApplyMWUtils.getModel(masterPid);
        ApplyMWUtils.process(fa, sa, masterPid, firstPid, firstModel, userValue, mode, coll);
        firstIter = false;
        Set<String> pids = fa.getPids(masterPid);

        String[] root;
        ObjectPidsPath[] path = sa.getPath(masterPid);
        if(path == null) {
            root = new String[1];
            root[0] = masterPid;
        } else {
            root = path[path.length - 1].getPathFromRootToLeaf();
        }
        for (int i = 0; i < root.length; i++) {
            if("policy:private".equals(disectFlagFromRELSEXT(root[i],fa))){
                    pids.add(root[i]);
                }
            }
        
        for (String onePid : pids) {
            ApplyMWUtils.process(fa, sa, onePid, firstPid, firstModel, userValue, mode, coll);
        }
    }

    /**
     * Process one pid
     * @param fa FedoraAccess
     * @param sa SolrAccess
     * @param onePid Concrete PID
     * @param userValue User defined value; if null it takes value from configuration
     * @param coll Collect pid for indexing 
     * @throws IOException 
     * @throws RightCriteriumException
     * @throws XPathExpressionException
     */
    public static void process(FedoraAccess fa, SolrAccess sa, String onePid,
            String firstPid, String firstModel, String userValue, String mode,
            CollectPidForIndexing coll) throws IOException,
            RightCriteriumException, XPathExpressionException {
        ProcessCriteriumContext ctx = new ProcessCriteriumContext(onePid, fa,
                sa);


        MovingWall mw = new MovingWall();
        mw.setEvaluateContext(ctx);
        int wall = 0;
        if (userValue != null) {
            try {
                wall = Integer.parseInt(userValue);
            } catch (NumberFormatException e) {
                ApplyMovingWall.LOGGER.severe("Cannot parse user value");
                ApplyMovingWall.LOGGER.severe(e.getMessage());
                return;
            }
        } else {
            wall = ApplyMWUtils.configuredWall(sa, onePid, KConfiguration.getInstance()
                    .getConfiguration());
        }
        ApplyMovingWall.LOGGER.info("Used value is: " + wall);
        mw.setCriteriumParamValues(new Object[] { "" + wall, mode, firstModel, firstPid });
        EvaluatingResult result = mw.evalute();
        String flagFromRELSEXT = ApplyMWUtils.disectFlagFromRELSEXT(onePid, fa);
        if (result == EvaluatingResult.TRUE) {
            ApplyMovingWall.LOGGER.info("Set policy flag for '" + onePid + "' to value true ");
            ApplyMWUtils.setPolicyFlag(onePid, true, fa, flagFromRELSEXT, coll);
        } else if (result == EvaluatingResult.FALSE) {
            // set private
            ApplyMovingWall.LOGGER.info("Set policy flag for '" + onePid + "' to value false");
            ApplyMWUtils.setPolicyFlag(onePid, false, fa, flagFromRELSEXT, coll);
        } else {
            // wrong data
            ApplyMovingWall.LOGGER.warning("cannot set flag for pid " + onePid);
        }
    }

    public static String disectFlagFromRELSEXT(String onePid, FedoraAccess fa)
            throws IOException, XPathExpressionException {
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
     * 
     * @param b
     *            Flag
     * @param fa
     *            FedraAccess
     * @throws IOException
     */
    public static void setPolicyFlag(String pid, boolean b, FedoraAccess fa,
            String previousState, CollectPidForIndexing coll)
            throws IOException {
        if (ApplyMWUtils.detectChange(b, previousState)) {
            PolicyServiceImpl policy = new PolicyServiceImpl();
            policy.setFedoraAccess(fa);
            policy.setConfiguration(KConfiguration.getInstance());
            policy.setPolicyForNode(pid, b ? "public" : "private");
            coll.enqueuePid(pid);
        } else {
            ApplyMovingWall.LOGGER.info("no change for pid " + pid);
        }
    }

    /**
     * Detect change
     * 
     * @param b
     *            new flag
     * @param previousState
     *            previous state
     * @return
     */
    public static boolean detectChange(boolean b, String previousState) {
        // no flag before
        boolean doChange = !StringUtils.isAnyString(previousState);
        // if want to set true and previous state is private
        doChange = doChange ? doChange : (b && previousState
                .equals("policy:private"));
        // if want to set false and previous state is public
        doChange = doChange ? doChange : (!b && previousState
                .equals("policy:public"));
        return doChange;
    }

    /**
     * Configured moving wall
     * 
     * @param sa
     *            SolrAccess instance
     * @param onePid
     *            Configured pid
     * @return Wall configuration value
     * @throws IOException
     */
    public static int configuredWall(SolrAccess sa, String onePid, Configuration conf)
            throws IOException {
        ObjectModelsPath[] pathOfModels = sa.getPathOfModels(onePid);
        ObjectModelsPath path = pathOfModels[0];
        String[] models = path.getPathFromLeafToRoot();
        int wall = defaultConfiguredWall( conf);
        for (String model : models) {
            if (conf.containsKey("mwprocess.model." + model + ".wall")) {
                wall = conf.getInt("mwprocess.model." + model + ".wall");
            }
        }
        return wall;
    }

    public static int defaultConfiguredWall( Configuration conf)
            throws IOException {
        int wall = conf.getInt("mwprocess.wall", 70);
        return wall;
    
    }
    
    public static String getModel(String pid) {
        Document doc;
        String fedoraModel = null;
        try {
            doc = SolrUtils.getSolrDataInternal(SolrUtils.UUID_QUERY + "\"" + pid + "\"");
            fedoraModel = SolrUtils.disectFedoraModel(doc);
        } catch (IOException ex) {
            Logger.getLogger(ApplyMWUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ApplyMWUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ApplyMWUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ApplyMWUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fedoraModel;
    }
}
