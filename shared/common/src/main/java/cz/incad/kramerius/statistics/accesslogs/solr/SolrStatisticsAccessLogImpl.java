package cz.incad.kramerius.statistics.accesslogs.solr;

import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.security.impl.criteria.Licenses;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.XMLUtils;

import org.apache.commons.configuration.Configuration;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.pdf.utils.ModsUtils;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaLicenseUtils;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLogSupport;
import cz.incad.kramerius.statistics.accesslogs.AbstractStatisticsAccessLog;
import cz.incad.kramerius.statistics.accesslogs.LogRecord;
import cz.incad.kramerius.statistics.accesslogs.LogRecordDetail;
import cz.incad.kramerius.statistics.accesslogs.database.DatabaseStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.accesslogs.utils.SElemUtils;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUpdateUtils;
import cz.incad.kramerius.utils.solr.SolrUtils;

public class SolrStatisticsAccessLogImpl extends AbstractStatisticsAccessLog {

	private static final String DATE_RANGE_START_YEAR_FIELD = "date_range_start.year";
    private static final String DATE_RANGE_END_YEAR_FIELD = "date_range_end.year";
    private static final String DATE_STR_FIELD = "date.str";


    private static final String SOLR_POINT = "k7.log.solr.point";
    private static final String SOLR_POINT_NEW = "api.log.point";


    static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DatabaseStatisticsAccessLogImpl.class.getName());


    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    /* TODO AK_NEW
    @Inject
    @Named("cachedFedoraAccess")
    FedoraAccess fedoraAccess;

     */
    @Inject
    AkubraRepository akubraRepository;

    @Inject
    Provider<HttpServletRequest> requestProvider;

//    @Inject
//    LoggedUsersSingleton loggedUsersSingleton;

    @Inject
    Provider<User> userProvider;

    @Inject
    Set<StatisticReport> reports;

    @Inject
    VersionService versionService;

    //private XPathFactory xpfactory;
    private Client client;
    private DocumentBuilderFactory documentBuilderFactory;
    
    public SolrStatisticsAccessLogImpl() {
        //this.xpfactory = XPathFactory.newInstance();
        
        this.client = Client.create();
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        
        client.setReadTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));
        client.setConnectTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));
    }

    @Override
    public void reportAccess(final String pid, final String streamName) throws IOException {
        Document solrDoc = this.solrAccess.getSolrDataByPid(pid);
        
        ObjectPidsPath[] paths = this.solrAccess.getPidPaths(solrDoc);
        ObjectModelsPath[] mpaths = this.solrAccess.getModelPaths(solrDoc);
        ObjectPidsPath[] ownPidPaths = this.solrAccess.getOwnPidPaths(solrDoc);

        LogRecord logRecord = LogRecord.buildRecord(pid);

        // jestlize je uzivatel, pak tokenid, pokud ne, pak session id
        if (userProvider.get() != null) {
            String sessionId = requestProvider.get().getSession().getId();
            logRecord.setSessionToken(sessionId);
        } else {
            // user store token id 
        }

        String requestedUrl = requestProvider.get().getRequestURL().toString();
        logRecord.setRequestedUrl(requestedUrl);

        logRecord.setIpAddress(IPAddressUtils.getRemoteAddress(requestProvider.get()));
        //logRecord.setIpAddress(requestProvider.get().getRemoteAddr());
        /*if(requestProvider.get().getHeader("X-Forwarded-For")!=null){
            String remoteIp = requestProvider.get().getHeader("X-Forwarded-For");
            logRecord.setIpAddress(remoteIp);
        }*/
        
        logRecord.setPidsPaths(Arrays.stream(paths).map(ObjectPidsPath::getPathFromRootToLeaf).map(array-> {
            return Arrays.stream(array).collect(Collectors.joining("/"));
        }).collect(Collectors.toSet()));

        // only one now 
        if (mpaths.length > 0) {
            logRecord.setOwnModelPath(Arrays.stream(mpaths[0].getPathFromRootToLeaf()).collect(Collectors.joining("/")));
        }
        
        if (ownPidPaths.length > 0) {
            logRecord.setOwnPidpath(Arrays.stream(ownPidPaths[0].getPathFromRootToLeaf()).collect(Collectors.joining("/")));
        }
        try {
            String rootTitle = SolrUtils.rootTitle(solrDoc);
            logRecord.setRootTitle(rootTitle);
            
            String rootModel = SolrUtils.rootModel(solrDoc);
            logRecord.setRootModel(rootModel);
            
            String rootPid = SolrUtils.rootPid(solrDoc);
            logRecord.setRootPid(rootPid);
            
            logRecord.setLicenses(new LinkedHashSet<>(SolrUtils.disectLicenses(solrDoc.getDocumentElement())));
            User user = this.userProvider.get();
            logRecord.setUser(user.getLoginname());
            
            RightsReturnObject rightsReturnObject = CriteriaLicenseUtils.currentThreadReturnObject.get();
            Map<String, String> evaluateInfoMap = rightsReturnObject != null ? rightsReturnObject.getEvaluateInfoMap() : new HashMap<>();
            if (evaluateInfoMap != null) {
                try {
                    JSONObject evaluateMap =   new JSONObject(evaluateInfoMap);
                    logRecord.setEvaluatedMap(evaluateMap.toString());
                    String providedByLicense = null;
                    if (evaluateMap.has(Licenses.PROVIDED_BY_LICENSE)) {
                        providedByLicense = evaluateMap.getString(Licenses.PROVIDED_BY_LICENSE);
                    } else if (evaluateMap.has(Licenses.PROVIDED_BY_LABEL)) {
                        providedByLicense = evaluateMap.getString(Licenses.PROVIDED_BY_LABEL);
                    }
                    if (providedByLicense != null) {
                        logRecord.setProvidedByLicense(providedByLicense);
                    }
                } catch(Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
                // provided 
            }
            
            if (user.getSessionAttributes() != null) {
                logRecord.setUserSessionAttributes(new JSONObject(user.getSessionAttributes()).toString());
            }
            
            logRecord.setReportedAction(this.reportedAction != null  && this.reportedAction.get() != null ?  this.reportedAction.get().name() : ReportedAction.READ.name());
            logRecord.setDbVersion(versionService.getVersion());
            // pokud je user != null -> tokenid
            // jinak sessionid
            // - user is accessed before - would have thrown exception already. Will never be null here.
            /*if (user != null) {
                //user.get
            } else {
                String sessionId = requestProvider.get().getSession().getId();
                logRecord.setSessionToken(sessionId);
            }*/

            // Issue #1046
            Object dateFromSolr = SElemUtils.selem("str", DATE_STR_FIELD, solrDoc);
            if (dateFromSolr!= null) logRecord.setDateStr(dateFromSolr.toString());
            else LOGGER.fine("No "+DATE_STR_FIELD);
            
            Object dateRangeEnd = SElemUtils.selem("int", DATE_RANGE_END_YEAR_FIELD, solrDoc);
            if (dateRangeEnd != null) logRecord.setDateRangeEnd(dateRangeEnd.toString());
            else LOGGER.fine("No "+DATE_RANGE_END_YEAR_FIELD);
            
            Object dateRangeStart = SElemUtils.selem("int", DATE_RANGE_START_YEAR_FIELD, solrDoc);
            if (dateRangeStart != null) logRecord.setDateRangeStart(dateRangeStart.toString());
            else LOGGER.fine("No "+DATE_RANGE_START_YEAR_FIELD);
            
            logRecord.setFieldsFromHttpRequestHeaders(extractFieldsFromHttpRequestHeaders());
            
            for (int i = 0, ll = paths.length; i < ll; i++) {
                if (paths[i].contains(SpecialObjects.REPOSITORY.getPid())) {
                    paths[i] = paths[i].cutHead(0);
                }
                String[] pathFromLeafToRoot = paths[i].getPathFromLeafToRoot();
                for (int j = 0; j < pathFromLeafToRoot.length; j++) {
                    final String detailPid = pathFromLeafToRoot[j];
                    String detailModel = akubraRepository.re().getModel(detailPid);
                    LogRecordDetail logDetail = LogRecordDetail.buildDetail(detailPid, detailModel);
                    Document dc = akubraRepository.getDatastreamContent(detailPid, KnownDatastreams.BIBLIO_DC).asDom(false);
                    if (dc != null) {
                        Object dateFromDC = DCUtils.dateFromDC(dc);
                        if (dateFromDC != null) {
                            logRecord.addIssueDate(dateFromDC.toString());
                        }
                        
                        
                        Object languageFromDc = DCUtils.languageFromDC(dc);
                        if (languageFromDc != null) {
                            logRecord.addLang(languageFromDc.toString());
                        }
                        
                        Object title = DCUtils.titleFromDC(dc);
                        if (title != null) {
                            logRecord.addTitle(title.toString());
                            logDetail.setTitle(title.toString());
                        }
                        Document mods = akubraRepository.getDatastreamContent(detailPid, KnownDatastreams.BIBLIO_MODS).asDom(false);
                        Map<String, List<String>> identifiers;
                        try {
                            identifiers = ModsUtils.identifiersFromMods(mods);
                            for (String key : identifiers.keySet()) {
                                if (key.equals(ISBN_MODS_KEY)) {
                                    identifiers.get(ISBN_MODS_KEY).stream().forEach(isbn-> {
                                        logRecord.addISBN(isbn);
                                    });
                                }
                                if (key.equals(ISSN_MODS_KEY)) {
                                    identifiers.get(ISSN_MODS_KEY).stream().forEach(issn -> {
                                        logRecord.addISSN(issn);
                                    });
                                }
                                if (key.equals(CCNB_MODS_KEY)) {
                                    identifiers.get(CCNB_MODS_KEY).stream().forEach(ccnb-> {
                                        logRecord.addCCNB(ccnb);
                                    });
                                }
                                
                            }
                        } catch (XPathExpressionException e) {
                            Logger.getLogger(SolrStatisticsAccessLogImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                        }
                        
                        String[] creatorsFromDC = DCUtils.creatorsFromDC(dc);
                        for (String cr : creatorsFromDC) {
                            logRecord.addAuthor(cr);
                        }

                        String[] publishersFromDC = DCUtils.publishersFromDC(dc);
                        for (String p : publishersFromDC) {
                            logRecord.addPublisher(p);
                        }
                    }
                    logRecord.addDetail(logDetail);
                }
            }
        } catch (SQLException | XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            try {

                String loggerPoint;
                String updateUrl;


                Configuration config = KConfiguration.getInstance().getConfiguration();
                if (config.containsKey(SOLR_POINT)) {
                    loggerPoint = KConfiguration.getInstance().getProperty(SOLR_POINT,"http://localhost:8983/solr/logs");
                    updateUrl = loggerPoint+(loggerPoint.endsWith("/") ?  "" : "/")+"update";
                } else if (config.containsKey(SOLR_POINT_NEW)) {
                    loggerPoint = KConfiguration.getInstance().getProperty(SOLR_POINT_NEW,"http://localhost:8983/solr/logs");
                    updateUrl = loggerPoint+(loggerPoint.endsWith("/") ?  "" : "/")+"update";
                }  else {
                    loggerPoint = KConfiguration.getInstance().getProperty(SOLR_POINT,"http://localhost:8983/solr/logs");
                    updateUrl = loggerPoint+(loggerPoint.endsWith("/") ?  "" : "/")+"update";
                }



                LOGGER.fine("Log record is "+logRecord.toString());
                
                Document batch = logRecord.toSolrBatch(this.documentBuilderFactory);
                try {
                    StringWriter writer = new StringWriter();
                    XMLUtils.print(batch, writer);
                    LOGGER.log(Level.FINE, "Update doc  => {0}", writer.toString());
                } catch (TransformerException e1) {
                    LOGGER.log(Level.SEVERE,e1.getMessage(),e1);
                }
                
                SolrUpdateUtils.sendToDest(this.client, logRecord.toSolrBatch(this.documentBuilderFactory), updateUrl);
            } catch (ParserConfigurationException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private Map<String, String> extractFieldsFromHttpRequestHeaders() {
        Map<String, String> results = new HashMap<>();
        String referer = requestProvider.get().getHeader("Referer");
        if (referer != null) {
            results.put("Referer", referer);
        }
        String headerKrameriusClient = requestProvider.get().getHeader("client");
        if (headerKrameriusClient != null) {
            results.put("Kramerius-client", headerKrameriusClient);
        }
        return results;
    }
    
    //TODO: Implement
    @Override
    public void reportAccess(String pid, String streamName, String actionName) throws IOException {
        ReportedAction action = ReportedAction.valueOf(actionName);
        this.reportedAction.set(action);
        this.reportAccess(pid, streamName);
    }

    @Override
    public boolean isReportingAccess(String pid, String streamName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void processAccessLog(ReportedAction reportedAction, StatisticsAccessLogSupport sup) {
        // TODO Auto-generated method stub
    }

    @Override
    public StatisticReport[] getAllReports() {
        return (StatisticReport[]) this.reports.toArray(new StatisticReport[this.reports.size()]);
    }

    @Override
    public StatisticReport getReportById(String reportId) {
        for (StatisticReport rep : this.reports) {
            if (rep.getReportId().equals(reportId)) return rep;
        }
        return null;
    }

    @Override
    public int cleanData(Date dateFrom, Date dateTo) throws IOException {
        // delete by query
        String loggerPoint = KConfiguration.getInstance().getProperty(SOLR_POINT,"http://localhost:8983/solr/logs");
        String updateEndpoint = loggerPoint + (loggerPoint.endsWith("/") ? "" : "/" ) +"update";

        HttpPost httpPost = new HttpPost(updateEndpoint);
        
        String xml = String.format("<delete><query>date:[%s TO %s]</query></delete>", StatisticReport.SOLR_DATE_FORMAT.format(dateFrom), StatisticReport.SOLR_DATE_FORMAT.format(dateTo));
        StringEntity entity = new StringEntity(xml);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/xml");
        httpPost.setHeader("Content-type", "application/xml");
        httpPost.setEntity(entity);
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() == SC_OK) {
                   // HttpEntity respEntity = response.getEntity();
                    //InputStream content = respEntity.getContent();
                    //String resp = IOUtils.toString(content, "UTF-8");
                    //resp is not used
                    return 0;
                } else {
                    throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                }
            }
        }
    }

    @Override
    public void refresh() throws IOException {
    }

}
