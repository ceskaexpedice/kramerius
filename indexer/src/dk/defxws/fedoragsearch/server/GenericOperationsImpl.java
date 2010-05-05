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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import dk.defxws.fedoragsearch.server.errors.FedoraObjectNotFoundException;
import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

import org.apache.axis.AxisFault;

import org.apache.log4j.Logger;

import fedora.client.FedoraClient;

import fedora.common.Constants;

import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.Datastream;
import fedora.server.types.gen.MIMETypedStream;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.io.StringReader;

import java.util.ArrayList;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
    protected int insertTotal = 0;
    protected int updateTotal = 0;
    protected int deleteTotal = 0;
    protected int docCount = 0;
    protected int warnCount = 0;
    public byte[] foxmlRecord;
    protected String dsID;
    protected byte[] ds;
    protected String dsText;
    protected String[] params = null;

    public static FedoraClient getFedoraClient(
            String repositoryName,
            String fedoraSoap,
            String fedoraUser,
            String fedoraPass)
            throws GenericSearchException {
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
            throw new GenericSearchException("Error getting FedoraClient" + " for repository: " + repositoryName, e);
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
            throws GenericSearchException {
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
            throw new GenericSearchException("Error getting API-A stub" + " for repository: " + repositoryName, e);
        }
    }

    private static FedoraAPIM getAPIM(
            String repositoryName,
            String fedoraSoap,
            String fedoraUser,
            String fedoraPass,
            String trustStorePath,
            String trustStorePass)
            throws GenericSearchException {
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
            throw new GenericSearchException("Error getting API-M stub" + " for repository: " + repositoryName, e);
        }
    }

    public void init(String indexName, Properties currentConfig) {
        init(null, indexName, currentConfig);
    }

    public void init(String fgsUserName, String indexName, Properties currentConfig) {
        config = currentConfig;
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


    public String updateIndex(
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
        
        StringBuffer resultXml = new StringBuffer();
        String repositoryName = repositoryNameParam;
        if (repositoryNameParam == null || repositoryNameParam.equals("")) {
            repositoryName = config.getProperty("RepositoryName");
        }
        resultXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        resultXml.append("<resultPage");
        resultXml.append(" operation=\"updateIndex\"");
        resultXml.append(" action=\"" + action + "\"");
        resultXml.append(" value=\"" + value + "\"");
        resultXml.append(" repositoryName=\"" + repositoryName + "\"");
        resultXml.append(" indexNames=\"" + indexNames + "\"");
        resultXml.append(" resultPageXslt=\"" + resultPageXslt + "\"");
        resultXml.append(" dateTime=\"" + new Date() + "\"");
        resultXml.append(">\n");
        
            
            SolrOperations ops = new SolrOperations(this);
            resultXml.append(ops.updateIndex(action, value, repositoryName, indexName, indexDocXslt, resultPageXslt, requestParams));
        
        resultXml.append("</resultPage>\n");
        if (logger.isDebugEnabled()) {
            logger.debug("resultXml=" + resultXml);
        }
        return resultXml.toString();
    }
    
    public byte[] getAndReturnFoxmlFromPid(
            String pid,
            String repositoryName)
            throws java.rmi.RemoteException {

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
        String format = Constants.FOXML1_1.uri;
        if (fedoraVersion != null && fedoraVersion.startsWith("2.")) {
            format = Constants.FOXML1_0_LEGACY;
        }
        try {
            return apim.export(pid, format, "public");
        } catch (RemoteException e) {
            throw new FedoraObjectNotFoundException("Fedora Object " + pid + " not found at " + repositoryName, e);
        }
    }

    public void getFoxmlFromPid(
            String pid,
            String repositoryName)
            throws java.rmi.RemoteException {

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
        String format = Constants.FOXML1_1.uri;
        if (fedoraVersion != null && fedoraVersion.startsWith("2.")) {
            format = Constants.FOXML1_0_LEGACY;
        }
        try {
            foxmlRecord = apim.export(pid, format, "public");
        } catch (RemoteException e) {
            throw new FedoraObjectNotFoundException("Fedora Object " + pid + " not found at " + repositoryName, e);
        }
    }

    public String getDatastreamText(
            String pid,
            String repositoryName,
            String dsId)
            throws GenericSearchException, Exception {
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
            throws GenericSearchException, Exception {
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
                    throw new GenericSearchException(e.getFaultString() + ": " + e.toString());
                }
            } catch (RemoteException e) {
                throw new GenericSearchException(e.getClass().getName() + ": " + e.toString());
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
            throws GenericSearchException, Exception {
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
            throws GenericSearchException, Exception {
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
            throw new GenericSearchException(e.getClass().getName() + ": " + e.toString());
        } catch (RemoteException e) {
            throw new GenericSearchException(e.getClass().getName() + ": " + e.toString());
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
                throw new GenericSearchException(e.getClass().getName() + ": " + e.toString());
            } catch (RemoteException e) {
                throw new GenericSearchException(e.getClass().getName() + ": " + e.toString());
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
            throws GenericSearchException, Exception {
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
            throws GenericSearchException, Exception {
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
                    throw new GenericSearchException("getDissemination returned null");
                }
                ds = mts.getStream();
                mimetype = mts.getMIMEType();
                if (logger.isDebugEnabled()) {
                    logger.debug("getDisseminationText" +
                            " mimetype=" + mimetype);
                }
            } catch (GenericSearchException e) {
                if (e.toString().indexOf("DisseminatorNotFoundException") > -1) {
                    return new StringBuffer();
                } else {
                    throw new GenericSearchException(e.toString());
                }
            } catch (AxisFault e) {
                if (e.getFaultString().indexOf("DisseminatorNotFoundException") > -1) {
                    return new StringBuffer();
                } else {
                    throw new GenericSearchException(e.getFaultString() + ": " + e.toString());
                }
            } catch (RemoteException e) {
                throw new GenericSearchException(e.getClass().getName() + ": " + e.toString());
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

    /* Kramerius
     * Added by Incad 
     */
    public String getRelsTitle(String pid) throws GenericSearchException {
        try {
            logger.debug("getRelsTitle" +
                    " pid=" + pid);
            /* query
            select $model $title $object from <#ri> 
            where  $object <kramerius:hasPage> <info:fedora/uuid:68748b80-64a6-11dd-8fb6-000d606f5dc6> 
            and $object <dc:title> $title 
            and $object <fedora-model:hasModel> $model 
             */
            /* response
            <sparql xmlns="http://www.w3.org/2001/sw/DataAccess/rf1/result">
            <head>
            <variable name="title"/>
            <variable name="model"/>
            </head>
            <results>
            <result>
            <model uri="info:fedora/fedora-system:FedoraObject-3.0"/>
            <title>Kniha zlat�, anebo, Now� Zw�stowatel wsseho
            dobr�ho a v�ite�n�ho pro N�rod Slowensk�.</title>
            </result>
            <result>
            <model uri="info:fedora/model:monographunit"/>
            <title>Kniha zlat�, anebo, Now� Zw�stowatel wsseho
            dobr�ho a v�ite�n�ho pro N�rod Slowensk�.</title>
            </result>
            </results>
            </sparql>
            
             */
            String title = "";
            String fedoraUrl = "http://localhost:8080/fedora";
            String query = "select $title from <#ri> " +
                    "where  $object <dc:identifier> '" + pid + "' " +
                    "and $object <dc:title> $title  ";
            String command = fedoraUrl + "/risearch?type=tuples&flush=true&lang=itql&format=Sparql&limit=&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            StringBuffer result = new StringBuffer();
            java.net.URL url = new java.net.URL(command);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream(),
                    java.nio.charset.Charset.forName("UTF-8")));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                //result.append(URLDecoder.decode(inputLine, "UTF-8"));
                result.append(inputLine);
            }

            in.close();

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false); // never forget this!

            DocumentBuilder builder = domFactory.newDocumentBuilder();

            InputSource source = new InputSource(new StringReader(result.toString()));
            Document contentDom = builder.parse(source);

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            //Loading properties
            XPathExpression expr = xpath.compile("/sparql/results/result/title/text()");
            title = (String) expr.evaluate(contentDom, XPathConstants.STRING);
            return title;
        } catch (Exception e) {
            //throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
            return e.toString();
        }
    }
    String fedoraSystemModel = "info:fedora/fedora-system:FedoraObject-3.0";
    final String inFieldSeparator = "###";

    public String getTitleToShow(String pid,
            String model) throws GenericSearchException {
        try {
            logger.debug("getTitleToShow" +
                    " pid=" + pid);
            /* query
            select $model $title $creator $object from <#ri> 
            where  $object <kramerius:hasPage> <info:fedora/uuid:68748b80-64a6-11dd-8fb6-000d606f5dc6> 
            and $object <dc:title> $title 
            and $object <dc:creator> $creator 
            and $object <fedora-model:hasModel> $model 
             */
            /* response
            <sparql xmlns="http://www.w3.org/2001/sw/DataAccess/rf1/result">
            <head>
            <variable name="title"/>
            <variable name="model"/>
            </head>
            <results>
            <result>
            <model uri="info:fedora/fedora-system:FedoraObject-3.0"/>
            <title>Kniha zlat�, anebo, Now� Zw�stowatel wsseho
            dobr�ho a v�ite�n�ho pro N�rod Slowensk�.</title>
            </result>
            <result>
            <model uri="info:fedora/model:monographunit"/>
            <title>Kniha zlat�, anebo, Now� Zw�stowatel wsseho
            dobr�ho a v�ite�n�ho pro N�rod Slowensk�.</title>
            </result>
            </results>
            </sparql>
            
             */
            String title = "";
            String creator = "";
            String parentModel = "";
            String parentPid = "";
            String fedoraUrl = "http://localhost:8080/fedora";
            String query = "select $model $title $creator $object from <#ri> " +
                    "where  $object " + model + " <info:fedora/" + pid + ">  " +
                    "and $object <dc:title> $title  " +
                    "and $object <dc:creator> $creator " +
                    "and $object <fedora-model:hasModel> $model ";
            String command = fedoraUrl + "/risearch?type=tuples&flush=true&lang=itql&format=Sparql&limit=&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            StringBuffer result = new StringBuffer();
            java.net.URL url = new java.net.URL(command);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream(),
                    java.nio.charset.Charset.forName("UTF-8")));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                //result.append(URLDecoder.decode(inputLine, "UTF-8"));
                result.append(inputLine);
            }

            in.close();

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false);

            DocumentBuilder builder = domFactory.newDocumentBuilder();

            InputSource source = new InputSource(new StringReader(result.toString()));
            Document contentDom = builder.parse(source);

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            //Loading properties
            XPathExpression expr = xpath.compile("/sparql/results/result/*");
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            boolean hasModel = false;
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                if (childnode.getNodeName().equals("model")) {
                    String parentModel_ = childnode.getAttributes().getNamedItem("uri").getNodeValue();
                    if (parentModel_.contains("info:fedora/model:monograph") ||
                            parentModel_.contains("info:fedora/model:monographunit") ||
                            parentModel_.contains("info:fedora/model:periodical")) {
                        hasModel = true;
                        parentModel =parentModel_;
                        while (childnode.getNextSibling() != null) {
                            childnode = childnode.getNextSibling();
                            if (childnode.getNodeName().equals("title")) {
                                title = childnode.getFirstChild().getNodeValue();
                            }
                            if (childnode.getNodeName().equals("creator")) {
                                creator += childnode.getFirstChild().getNodeValue() + "@@";
                            }
                            if (childnode.getNodeName().equals("object")) {
                                parentPid = childnode.getAttributes().getNamedItem("uri").getNodeValue();
                            }
                        }
                    } else if (parentModel.contains("info:fedora/model:periodicalvolume")) {
                        while (childnode.getNextSibling() != null) {
                            childnode = childnode.getNextSibling();
                            if (childnode.getNodeName().equals("title")) {
                                title = childnode.getFirstChild().getNodeValue();
                            }
                            if (childnode.getNodeName().equals("creator")) {
                                creator = " ";
                            }
                            if (childnode.getNodeName().equals("object")) {
                                title = getTitleFromVolume(childnode.getAttributes().getNamedItem("uri").getNodeValue());
                            }
                        }
                    }
                    
                }
            }
                return title + inFieldSeparator + creator + inFieldSeparator + parentModel + inFieldSeparator + parentPid;

        } catch (Exception e) {
            //throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
            return e.toString();
        }
    }

    public String getTitleFromPage(String pid) throws GenericSearchException {
        try {
            return getTitleToShow(pid, "<kramerius:hasPage>");
        } catch (Exception e) {
            //throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
            return e.toString();
        }
    }

    public String getTitleFromVolume(String pid) throws GenericSearchException {
        try {
            return getTitleToShow(pid, "<kramerius:hasVolume>");
        } catch (Exception e) {
            //throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
            return e.toString();
        }
    }

    public String getTitleFromItem(String pid) throws GenericSearchException {
        try {
            return getTitleToShow(pid, "<kramerius:hasItem>");
        } catch (Exception e) {
            //throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
            return e.toString();
        }
    }
}
