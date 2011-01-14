
package cz.incad.kramerius.indexer;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import dk.defxws.fedoragsearch.server.*;

import java.util.HashMap;
import java.util.Map;


import org.apache.log4j.Logger;

/*
import fedora.client.FedoraClient;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.Datastream;
import fedora.server.types.gen.MIMETypedStream;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.fedora.api.Datastream;
import org.fedora.api.FedoraAPIMService;
 */
import java.util.ArrayList;
import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.MIMETypedStream;

/**
 * performs the generic parts of the operations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class FedoraOperations {

    private static final Logger logger =
            Logger.getLogger(FedoraOperations.class);
    private static final Map fedoraClients = new HashMap();
    protected String fgsUserName;
    protected String indexName_;
    //public Properties config;
    public byte[] foxmlRecord;
    protected String dsID;
    protected byte[] ds;
    protected String dsText;
    protected String[] params = null;
    String foxmlFormat;
    FedoraAccess fa;

    public FedoraOperations() {
        fa = new FedoraAccessImpl(KConfiguration.getInstance());
    }

    private static String getBaseURL(String fedoraSoap)
            throws Exception {
        final String end = "/services";
        String baseURL = fedoraSoap;
        if (fedoraSoap.endsWith(end)) {
            return fedoraSoap.substring(0, fedoraSoap.length() - end.length());
        } else {
            throw new Exception("Unable to determine baseURL from fedoraSoap" + " value (expected it to end with '" + end + "'): " + fedoraSoap);
        }
    }

    private FedoraAPIA getAPIA()
            throws Exception {
        
        try {
            return fa.getAPIA();
        } catch (Exception e) {
            throw new Exception("Error getting API-A stub");
        }
    }

    private FedoraAPIM getAPIM()
            throws Exception {
        return fa.getAPIM();
    }

    public void init(String indexName/*, Properties currentConfig*/) {
        init(null, indexName/*, currentConfig*/);
    }

    public void init(String fgsUserName, String indexName/*, Properties currentConfig*/) {
//        config = currentConfig;
        foxmlFormat = KConfiguration.getInstance().getConfiguration().getString("FOXMLFormat");
        this.fgsUserName = KConfiguration.getInstance().getConfiguration().getString("fgsUserName");
        if (null == this.fgsUserName || this.fgsUserName.length() == 0) {
            try {
                this.fgsUserName = KConfiguration.getInstance().getConfiguration().getString("fedoragsearch.testUserName");
            } catch (Exception e) {
                this.fgsUserName = "fedoragsearch.testUserName";
            }
        }
    }

    public void updateIndex(
            String action,
            String value,
            ArrayList<String> requestParams)
            throws java.rmi.RemoteException, Exception {
        logger.info("updateIndex" +
                " action=" + action +
                " value=" + value);

//        String repositoryName = repositoryNameParam;
//        if (repositoryNameParam == null || repositoryNameParam.equals("")) {
//            repositoryName = KConfiguration.getInstance().getConfiguration().getString("RepositoryName");
//        }

        SolrOperations ops = new SolrOperations(this);
        ops.updateIndex(action, value, requestParams);
    }

    public byte[] getAndReturnFoxmlFromPid(
            String pid)
            throws java.rmi.RemoteException, Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("getAndReturnFoxmlFromPid pid=" + pid);
        }
        

        try {
            return fa.getAPIM().export(pid, foxmlFormat, "public");
        } catch (Exception e) {
            throw new Exception("Fedora Object " + pid + " not found. ", e);
        }
    }

    public void getFoxmlFromPid(
            String pid)
            throws java.rmi.RemoteException, Exception {

        if (logger.isInfoEnabled()) {
            logger.info("getFoxmlFromPid" +
                    " pid=" + pid);
        }
        
        String format = "info:fedora/fedora-system:FOXML-1.1";
        try {
            foxmlRecord = fa.getAPIM().export(pid, format, "public");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Fedora Object " + pid + " not found. ", e);
        }
    }

    public int getPdfPagesCount(
            String pid,
            String dsId)
            throws Exception {
        ds = null;
        if (dsId != null) {
            try {
                FedoraAPIA apia = fa.getAPIA();
                MIMETypedStream mts = apia.getDatastreamDissemination(pid,
                        dsId, null);
                if (mts == null) {
                    return 1;
                }
                ds = mts.getStream();
                return (new TransformerToText().getPdfPagesCount(ds) + 1);
                
            } catch (Exception e) {
                throw new Exception(e.getClass().getName() + ": " + e.toString());
            }
        }
        return 1;
    }
    public String getDatastreamText(
            String pid,
            String repositoryName,
            String dsId,
            String docCount)
            throws Exception {
        return getDatastreamText(pid, repositoryName, dsId,
                KConfiguration.getInstance().getConfiguration().getString("FedoraSoap"),
                KConfiguration.getInstance().getConfiguration().getString("FedoraUser"),
                KConfiguration.getInstance().getConfiguration().getString("FedoraPass"),
                KConfiguration.getInstance().getConfiguration().getString("TrustStorePath"),
                KConfiguration.getInstance().getConfiguration().getString("TrustStorePass"),docCount);
    }
    public String getParentsOld(String pid){
        try{
        String query = "$object * <info:fedora/" + pid + ">";
        String urlStr = KConfiguration.getInstance().getConfiguration().getString("FedoraResourceIndex") + "?type=triples&flush=true&lang=spo&format=N-Triples&limit=&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            StringBuilder sb = new StringBuilder();

            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine;
            int end;
            while ((inputLine = in.readLine()) != null) {
//<info:fedora/uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6> <http://www.nsdl.org/ontologies/relationships#hasPage> <info:fedora/uuid:75fca1f0-64b2-11dd-9fd4-000d606f5dc6> .
//<info:fedora/uuid:f0da6570-8f3b-11dd-b796-000d606f5dc6> <http://www.nsdl.org/ontologies/relationships#isOnPage> <info:fedora/uuid:75fca1f0-64b2-11dd-9fd4-000d606f5dc6> .
                end = inputLine.indexOf(">");
//18 je velikost   <info:fedora/uuid:
                inputLine = inputLine.substring(18,end);
                sb.append(inputLine);
                sb.append(";");
            }
            in.close();
            sb.delete(sb.length()-1, sb.length());
            return sb.toString();

        }catch(Exception ex){
            ex.printStackTrace();
            logger.error(ex);
            return "";
        }
    }
    
    public String getParents(String pid){
        try{
            StringBuilder sb = new StringBuilder();
            IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
            ArrayList<String> l = g.getParentsPids(pid);
            for(int i=0; i<l.size();i++){
            //for(String s : l){
                sb.append(l.get(i));
                if(i<l.size()-1)  sb.append(";");
            }
            
            return sb.toString();
            
        }catch(Exception ex){
            ex.printStackTrace();
            logger.error(ex);
            return "";
        }
    }

    public String getDatastreamText(
            String pid,
            String repositoryName,
            String dsId,
            String fedoraSoap,
            String fedoraUser,
            String fedoraPass,
            String trustStorePath,
            String trustStorePass,
            String pageNum)
            throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("getDatastreamText" + " pid=" + pid + " repositoryName=" + repositoryName + " dsId=" + dsId + " fedoraSoap=" + fedoraSoap + " fedoraUser=" + fedoraUser + " fedoraPass=" + fedoraPass + " trustStorePath=" + trustStorePath + " trustStorePass=" + trustStorePass);
        }
        StringBuffer dsBuffer = new StringBuffer();
        String mimetype = "";
        ds = null;
        if (dsId != null) {
            try {
                FedoraAPIA apia = fa.getAPIA();
                MIMETypedStream mts = apia.getDatastreamDissemination(pid,
                        dsId, null);
                if (mts == null) {
                    return "";
                }
                ds = mts.getStream();
                mimetype = mts.getMIMEType();
            } catch (Exception e) {
                throw new Exception(e.getClass().getName() + ": " + e.toString());
            }
        }
        if (ds != null) {
            dsBuffer = (new TransformerToText().getText(ds, mimetype, pageNum));
        } else {
            logger.debug("ds is null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getDatastreamText" +
                    " pid=" + pid +
                    " dsId=" + dsId +
                    " mimetype=" + mimetype +
                    " dsBuffer=" + dsBuffer.toString());
        }     
        return dsBuffer.toString();
    }
    /*
    public StringBuffer getFirstDatastreamText(
    String pid,
    String repositoryName,
    String dsMimetypes)
    throws Exception, Exception {
    return getFirstDatastreamText(pid, repositoryName, dsMimetypes,
    config.getProperty("FedoraSoap"),
    config.getProperty("FedoraUser"),
    config.getProperty("FedoraPass"),
    config.getProperty("TrustStorePath"),
    config.getProperty("TrustStorePass"));
    }
    
    public StringBuffer getFirstDatastreamText(
    String pid,
    String repositoryName,
    String dsMimetypes,
    String fedoraSoap,
    String fedoraUser,
    String fedoraPass,
    String trustStorePath,
    String trustStorePass)
    throws Exception, Exception {
    if (logger.isInfoEnabled()) {
    logger.info("getFirstDatastreamText" + " pid=" + pid + " dsMimetypes=" + dsMimetypes + " fedoraSoap=" + fedoraSoap + " fedoraUser=" + fedoraUser + " fedoraPass=" + fedoraPass + " trustStorePath=" + trustStorePath + " trustStorePass=" + trustStorePass);
    }
    StringBuffer dsBuffer = new StringBuffer();
    Datastream[] dsds = null;
    try {
    FedoraAPIM apim = getAPIM(
    repositoryName,
    fedoraSoap,
    fedoraUser,
    fedoraPass,
    trustStorePath,
    trustStorePass);
    dsds = apim.getDatastreams(pid, null, "A");
    } catch (AxisFault e) {
    throw new Exception(e.getClass().getName() + ": " + e.toString());
    } catch (RemoteException e) {
    throw new Exception(e.getClass().getName() + ": " + e.toString());
    }
    //      String mimetypes = "text/plain text/html application/pdf application/ps application/msword";
    String mimetypes = config.getProperty("MimeTypes");
    if (dsMimetypes != null && dsMimetypes.length() > 0) {
    mimetypes = dsMimetypes;
    }
    String mimetype = "";
    dsID = null;
    if (dsds != null) {
    int best = 99999;
    for (int i = 0; i < dsds.length; i++) {
    int j = mimetypes.indexOf(dsds[i].getMIMEType());
    if (j > -1 && best > j) {
    dsID = dsds[i].getID();
    best = j;
    mimetype = dsds[i].getMIMEType();
    }
    }
    }
    ds = null;
    if (dsID != null) {
    try {
    FedoraAPIA apia = getAPIA(
    repositoryName,
    fedoraSoap,
    fedoraUser,
    fedoraPass,
    trustStorePath,
    trustStorePass);
    MIMETypedStream mts = apia.getDatastreamDissemination(pid,
    dsID, null);
    ds = mts.getStream();
    } catch (AxisFault e) {
    throw new Exception(e.getClass().getName() + ": " + e.toString());
    } catch (RemoteException e) {
    throw new Exception(e.getClass().getName() + ": " + e.toString());
    }
    }
    if (ds != null) {
    dsBuffer = (new TransformerToText().getText(ds, mimetype));
    }
    if (logger.isDebugEnabled()) {
    logger.debug("getFirstDatastreamText" +
    " pid=" + pid +
    " dsID=" + dsID +
    " mimetype=" + mimetype +
    " dsBuffer=" + dsBuffer.toString());
    }
    return dsBuffer;
    }
    
    public StringBuffer getDisseminationText(
    String pid,
    String repositoryName,
    String bDefPid,
    String methodName,
    String parameters,
    String asOfDateTime)
    throws Exception, Exception {
    return getDisseminationText(pid, repositoryName, bDefPid, methodName, parameters, asOfDateTime,
    config.getProperty("FedoraSoap"),
    config.getProperty("FedoraUser"),
    config.getProperty("FedoraPass"),
    config.getProperty("TrustStorePath"),
    config.getProperty("TrustStorePass"));
    }
    
    public StringBuffer getDisseminationText(
    String pid,
    String repositoryName,
    String bDefPid,
    String methodName,
    String parameters,
    String asOfDateTime,
    String fedoraSoap,
    String fedoraUser,
    String fedoraPass,
    String trustStorePath,
    String trustStorePass)
    throws Exception, Exception {
    if (logger.isInfoEnabled()) {
    logger.info("getDisseminationText" +
    " pid=" + pid +
    " bDefPid=" + bDefPid +
    " methodName=" + methodName +
    " parameters=" + parameters +
    " asOfDateTime=" + asOfDateTime + " fedoraSoap=" + fedoraSoap + " fedoraUser=" + fedoraUser + " fedoraPass=" + fedoraPass + " trustStorePath=" + trustStorePath + " trustStorePass=" + trustStorePass);
    }
    StringTokenizer st = new StringTokenizer(parameters);
    fedora.server.types.gen.Property[] params = new fedora.server.types.gen.Property[st.countTokens()];
    for (int i = 0; i < st.countTokens(); i++) {
    String param = st.nextToken();
    String[] nameAndValue = param.split("=");
    params[i] = new fedora.server.types.gen.Property(nameAndValue[0], nameAndValue[1]);
    }
    if (logger.isDebugEnabled()) {
    logger.debug("getDisseminationText" +
    " #parameters=" + params.length);
    }
    StringBuffer dsBuffer = new StringBuffer();
    String mimetype = "";
    ds = null;
    if (pid != null) {
    try {
    FedoraAPIA apia = getAPIA(
    repositoryName,
    fedoraSoap,
    fedoraUser,
    fedoraPass,
    trustStorePath,
    trustStorePass);
    MIMETypedStream mts = apia.getDissemination(pid, bDefPid,
    methodName, params, asOfDateTime);
    if (mts == null) {
    throw new Exception("getDissemination returned null");
    }
    ds = mts.getStream();
    mimetype = mts.getMIMEType();
    if (logger.isDebugEnabled()) {
    logger.debug("getDisseminationText" +
    " mimetype=" + mimetype);
    }
    } catch (AxisFault e) {
    if (e.getFaultString().indexOf("DisseminatorNotFoundException") > -1) {
    return new StringBuffer();
    } else {
    throw new Exception(e.getFaultString() + ": " + e.toString());
    }
    } catch (RemoteException e) {
    throw new Exception(e.getClass().getName() + ": " + e.toString());
    }catch (Exception e) {
    if (e.toString().indexOf("DisseminatorNotFoundException") > -1) {
    return new StringBuffer();
    } else {
    throw new Exception(e.toString());
    }
    } 
    }
    if (ds != null) {
    dsBuffer = (new TransformerToText().getText(ds, mimetype));
    }
    if (logger.isDebugEnabled()) {
    logger.debug("getDisseminationText" +
    " pid=" + pid +
    " bDefPid=" + bDefPid +
    " mimetype=" + mimetype +
    " dsBuffer=" + dsBuffer.toString());
    }
    return dsBuffer;
    }
     */
}
