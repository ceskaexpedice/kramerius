//$Id: GenericOperationsImpl.java 7828 2008-11-12 13:57:09Z gertsp $
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import cz.incad.kramerius.indexer.SolrOperations;

import java.rmi.RemoteException;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.axis.AxisFault;

import org.apache.log4j.Logger;

import fedora.client.FedoraClient;


import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.Datastream;
import fedora.server.types.gen.MIMETypedStream;

import java.util.ArrayList;
import java.util.Properties;

/**
 * performs the generic parts of the operations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class GenericOperationsImpl {

    private static final Logger logger =
            Logger.getLogger(GenericOperationsImpl.class);
    private static final Map fedoraClients = new HashMap();
    protected String fgsUserName;
    protected String indexName;
    public Properties config;
    public byte[] foxmlRecord;
    protected String dsID;
    protected byte[] ds;
    protected String dsText;
    protected String[] params = null;
    String foxmlFormat;

    public static FedoraClient getFedoraClient(
            String repositoryName,
            String fedoraSoap,
            String fedoraUser,
            String fedoraPass)
            throws Exception {
        try {
            String baseURL = getBaseURL(fedoraSoap);
            String user = fedoraUser;
            String clientId = user + "@" + baseURL;
            synchronized (fedoraClients) {
                if (fedoraClients.containsKey(clientId)) {
                    return (FedoraClient) fedoraClients.get(clientId);
                } else {
                    FedoraClient client = new FedoraClient(baseURL,
                            user, fedoraPass);
                    fedoraClients.put(clientId, client);
                    return client;
                }
            }
        } catch (Exception e) {
            throw new Exception("Error getting FedoraClient" + " for repository: " + repositoryName, e);
        }
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

    private static FedoraAPIA getAPIA(
            String repositoryName,
            String fedoraSoap,
            String fedoraUser,
            String fedoraPass,
            String trustStorePath,
            String trustStorePass)
            throws Exception {
        if (trustStorePath != null) {
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        }
        if (trustStorePass != null) {
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
        }
        FedoraClient client = getFedoraClient(repositoryName, fedoraSoap, fedoraUser, fedoraPass);
        try {
            return client.getAPIA();
        } catch (Exception e) {
            throw new Exception("Error getting API-A stub" + " for repository: " + repositoryName, e);
        }
    }

    private static FedoraAPIM getAPIM(
            String repositoryName,
            String fedoraSoap,
            String fedoraUser,
            String fedoraPass,
            String trustStorePath,
            String trustStorePass)
            throws Exception {
        if (trustStorePath != null) {
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        }
        if (trustStorePass != null) {
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
        }
        FedoraClient client = getFedoraClient(repositoryName, fedoraSoap, fedoraUser, fedoraPass);
        
        try {
            return client.getAPIM();
        } catch (Exception e) {
            throw new Exception("Error getting API-M stub" + " for repository: " + repositoryName, e);
        }
    }

    public void init(String indexName, Properties currentConfig) {
        init(null, indexName, currentConfig);
    }

    public void init(String fgsUserName, String indexName, Properties currentConfig) {
        config = currentConfig;
        foxmlFormat = config.getProperty("FOXMLFormat");
        this.fgsUserName = config.getProperty("fgsUserName");
        this.indexName = config.getProperty("IndexName");
        if (null == this.fgsUserName || this.fgsUserName.length() == 0) {
            try {
                this.fgsUserName = config.getProperty("fedoragsearch.testUserName");
            } catch (Exception e) {
                this.fgsUserName = "fedoragsearch.testUserName";
            }
        }
    }


    public void updateIndex(
            String action,
            String value,
            String repositoryNameParam,
            String indexNames,
            String indexDocXslt,
            String resultPageXslt,
            ArrayList<String> requestParams)
            throws java.rmi.RemoteException, Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("updateIndex" +
                    " action=" + action +
                    " value=" + value +
                    " repositoryName=" + repositoryNameParam +
                    " indexNames=" + indexNames +
                    " indexDocXslt=" + indexDocXslt +
                    " resultPageXslt=" + resultPageXslt);
        }
        
            logger.info("updateIndex" +
                    " action=" + action +
                    " value=" + value +
                    " repositoryName=" + repositoryNameParam +
                    " indexNames=" + indexNames +
                    " indexDocXslt=" + indexDocXslt +
                    " resultPageXslt=" + resultPageXslt);
        
        String repositoryName = repositoryNameParam;
        if (repositoryNameParam == null || repositoryNameParam.equals("")) {
            repositoryName = config.getProperty("RepositoryName");
        }
        
        SolrOperations ops = new SolrOperations(this);
        ops.updateIndex(action, value, repositoryName, indexName, indexDocXslt, resultPageXslt, requestParams);
    }
    
    public byte[] getAndReturnFoxmlFromPid(
            String pid,
            String repositoryName)
            throws java.rmi.RemoteException, Exception {

        if (logger.isInfoEnabled()) {
            logger.info("getAndReturnFoxmlFromPid" +
                    " pid=" + pid +
                    " repositoryName=" + repositoryName);
        }
        FedoraAPIM apim = getAPIM(repositoryName,
                config.getProperty("FedoraSoap"),
                config.getProperty("FedoraUser"),
                config.getProperty("FedoraPass"),
                config.getProperty("TrustStorePath"),
                config.getProperty("TrustStorePass"));

        String fedoraVersion = config.getProperty("FedoraVersion");
        
        try {
            return apim.export(pid, foxmlFormat, "public");
        } catch (RemoteException e) {
            throw new Exception("Fedora Object " + pid + " not found at " + repositoryName, e);
        }
    }

    public void getFoxmlFromPid(
            String pid,
            String repositoryName)
            throws java.rmi.RemoteException, Exception {

        if (logger.isInfoEnabled()) {
            logger.info("getFoxmlFromPid" +
                    " pid=" + pid +
                    " repositoryName=" + repositoryName);
        }
        FedoraAPIM apim = getAPIM(repositoryName,
                config.getProperty("FedoraSoap"),
                config.getProperty("FedoraUser"),
                config.getProperty("FedoraPass"),
                config.getProperty("TrustStorePath"),
                config.getProperty("TrustStorePass"));

        String fedoraVersion = config.getProperty("FedoraVersion");
//        String format = Constants.FOXML1_1.uri;
//        if (fedoraVersion != null && fedoraVersion.startsWith("2.")) {
//            format = Constants.FOXML1_0_LEGACY;
//        }
        String format = "info:fedora/fedora-system:FOXML-1.1";
        try {
            foxmlRecord = apim.export(pid, format, "public");
        } catch (RemoteException e) {
            throw new Exception("Fedora Object " + pid + " not found at " + repositoryName, e);
        }
    }

    public String getDatastreamText(
            String pid,
            String repositoryName,
            String dsId)
            throws Exception, Exception {
        return getDatastreamText(pid, repositoryName, dsId,
                config.getProperty("FedoraSoap"),
                config.getProperty("FedoraUser"),
                config.getProperty("FedoraPass"),
                config.getProperty("TrustStorePath"),
                config.getProperty("TrustStorePass"));
    }
                
    public String getDatastreamText(
            String pid,
            String repositoryName,
            String dsId,
            String fedoraSoap,
            String fedoraUser,
            String fedoraPass,
            String trustStorePath,
            String trustStorePass)
            throws Exception, Exception {
        if (logger.isInfoEnabled()) {
            logger.info("getDatastreamText" + " pid=" + pid + " repositoryName=" + repositoryName + " dsId=" + dsId + " fedoraSoap=" + fedoraSoap + " fedoraUser=" + fedoraUser + " fedoraPass=" + fedoraPass + " trustStorePath=" + trustStorePath + " trustStorePass=" + trustStorePass);
        }
        StringBuffer dsBuffer = new StringBuffer();
        String mimetype = "";
        ds = null;
        if (dsId != null) {
            try {
                FedoraAPIA apia = getAPIA(
                        repositoryName,
                        fedoraSoap,
                        fedoraUser,
                        fedoraPass,
                        trustStorePath,
                        trustStorePass);
                MIMETypedStream mts = apia.getDatastreamDissemination(pid,
                        dsId, null);
                if (mts == null) {
                    return "";
                }
                ds = mts.getStream();
                mimetype = mts.getMIMEType();
            } catch (AxisFault e) {
                if (e.getFaultString().indexOf("DatastreamNotFoundException") > -1 ||
                        e.getFaultString().indexOf("DefaulAccess") > -1) {
                    return new String();
                } else {
                    throw new Exception(e.getFaultString() + ": " + e.toString());
                }
            } catch (RemoteException e) {
                throw new Exception(e.getClass().getName() + ": " + e.toString());
            }
        }
        if (ds != null) {
            dsBuffer = (new TransformerToText().getText(ds, mimetype));
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

}
