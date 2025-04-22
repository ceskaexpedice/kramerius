package cz.incad.kramerius.statistics.impl;

import static cz.incad.kramerius.database.cond.ConditionsInterpretHelper.versionCondition;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.solr.SolrFieldsMapping;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.statistics.accesslogs.dnnt.DNNTStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.accesslogs.dnnt.date.YearLogFormat;
import cz.incad.kramerius.statistics.accesslogs.utils.SElemUtils;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.utils.ReportUtils;
import cz.incad.kramerius.utils.IterationUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.IterationUtils.IterationCallback;
import cz.incad.kramerius.utils.IterationUtils.IterationEndCallback;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.Offset;
import cz.incad.kramerius.utils.solr.SolrUtils;

/**
 * NKP Logy 
 */
public class NKPLogReport extends AbstractStatisticsReport implements StatisticReport {

    /*
    public static final String RUNTIME_ATTRS = "runtimeAttributes";
    public static final String MISSING_ATTRS = "missingAttributes";
    */
    public static final Logger LOGGER = Logger.getLogger(AuthorReport.class.getName());

    public static final String REPORT_ID = "nkp";


    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Inject
    @Named("cachedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    VersionService versionService;

    @javax.inject.Inject
    @javax.inject.Named("solr-client")
    javax.inject.Provider<CloseableHttpClient> provider;

    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction reportedAction, StatisticsFiltersContainer filters,
            Offset rOffset) throws StatisticsReportException {
        throw new UnsupportedOperationException("unsupported for this report");
    }

    @Override
    public List<String> getOptionalValues(StatisticsFiltersContainer filters) {
        return new ArrayList<>();
    }

    @Override
    public String getReportId() {
        return REPORT_ID;
    }


    @Override
    public void processAccessLog(ReportedAction action, StatisticsReportSupport sup, StatisticsFiltersContainer filters)
            throws StatisticsReportException {
        
        
        try {
            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            if (dateFilter.getFromDate() != null && dateFilter.getToDate() != null) {
                dateFilter.setInputFormat(DATE_FORMAT);
                dateFilter.setOutputFormat(SOLR_DATE_FORMAT);
                
                StringBuilder builder = new StringBuilder("*");
                ReportUtils.enhanceDateFilter(builder, dateFilter);
                
                String selectEndpoint = logsEndpoint();
                Client client = Client.create();
                // commit 
                commit(client, selectEndpoint);
                logsCursorIteration(client, selectEndpoint, builder.toString(), (elm, i) -> {

                    Element result = XMLUtils.findElement(elm, new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            String nodeName = element.getNodeName();
                            return nodeName.equals("result");
                        }
                    });
                    if (result != null) {
                        List<Element> elements = XMLUtils.getElements(result, new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String nodeName = element.getNodeName();
                                return nodeName.equals("doc");
                            }
                        });

                        List<String> idents = elements.stream().map(item -> {
                                    Element str = XMLUtils.findElement(item, new XMLUtils.ElementsFilter() {
                                                @Override
                                                public boolean acceptElement(Element element) {
                                                    return element.getNodeName().equals("str");
                                                }
                                            }
                                    );
                                    return str.getTextContent();
                                }
                        ).collect(Collectors.toList());

                        reportIdents(idents, sup);
                    } 
                }, ()->{});
                
            } else {
                throw new UnsupportedOperationException(
                        "Full report is not supported. Please, use dateFrom and dateTo");
            }

        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (BrokenBarrierException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    
    private static void commit(Client client, String logsindex) {
        String updateCommit = "update?commit=true";
        WebResource r = client.resource(logsindex + (logsindex.endsWith("/") ? "" : "/") + updateCommit);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        LOGGER.fine(String.format("Committing, %s; response %s ", r.toString(), t));
    }

    public static void logsCursorIteration(Client client,String address, String masterQuery,IterationCallback callback, IterationEndCallback endCallback) throws ParserConfigurationException,  SAXException, IOException, InterruptedException, BrokenBarrierException {
        String cursorMark = null;
        String queryCursorMark = null;
        do {
            Element element = pidsCursorQuery(client, address, masterQuery, cursorMark);
            cursorMark =  IterationUtils.findCursorMark(element);
            queryCursorMark = IterationUtils.findQueryCursorMark(element);
            callback.call(element, cursorMark);
        } while((cursorMark != null && queryCursorMark != null) && !cursorMark.equals(queryCursorMark));
        endCallback.end();
    }

    static Element pidsCursorQuery(Client client, String url, String mq,  String cursor)  throws ParserConfigurationException, SAXException, IOException{
        int rows = 1000;
        String query = "select" + "?q="+mq + (cursor!= null ? String.format("&rows=%d&cursorMark=%s", rows, cursor) : String.format("&rows=%d&cursorMark=*", rows))+"&sort=" + URLEncoder.encode("id desc", "UTF-8")+"&fl=id";
        return IterationUtils.executeQuery(client, url, query);
    }

    
    private void reportIdents(List<String> idents, StatisticsReportSupport sup) {
        String selectEndpoint = super.logsEndpoint();
        Lists.partition(idents, 10).stream().forEach(it->{
            try {
                StringBuilder builder = new StringBuilder("q=*");
                String joinedIds = it.stream().map(oneIdent-> {
                    return '"'+ oneIdent+'"';
                }).collect(Collectors.joining(" OR "));
                
                builder.append("&fq="+URLEncoder.encode("id:("+joinedIds+")", "UTF-8"));

                InputStream iStream = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(this.provider.get(), selectEndpoint, builder.toString(), "json", null);
                String string = IOUtils.toString(iStream, "UTF-8");
                JSONObject result = new JSONObject(string);
                JSONObject response = result.getJSONObject("response");
                JSONArray docs = response.getJSONArray("docs");
                for (int i = 0; i < docs.length(); i++) {
                    JSONObject jsonObject = docs.getJSONObject(i);
                    Map map = toMap(jsonObject);
                    logReport(map, sup);
                }
            } catch (JSONException | IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        });
    }


    void logReport(Map map,  StatisticsReportSupport sup) {
        String pid = map.get("pid").toString();
//        boolean disbleFedoraAccess = KConfiguration.getInstance().getConfiguration()
//                .getBoolean("nkp.logs.disablefedoraaccess", false);
//        Object dbversion = map.get("dbversion");
//        if (dbversion != null)
//            map.remove("dbversion");
//        if (dbversion == null || versionCondition(dbversion.toString(), "<", "6.6.6")) {
//            //TODO: Delete
//            try {
//                if (!disbleFedoraAccess) {
//                    // only one place where we are connecting
//                    Document solrDoc = solrAccess.getSolrDataByPid(pid);
//                    if (solrDoc != null) {
//
//                        map.put(DNNTStatisticsAccessLogImpl.SOLR_DATE_KEY,
//                                new YearLogFormat().format(SElemUtils.selem("str", "datum_str", solrDoc)));
//
//                        // fill publishers
//                        ObjectPidsPath[] paths = solrAccess.getPidPaths(null, solrDoc);
//                        List<String> dcPublishers = DNNTStatisticsAccessLogImpl.dcPublishers(paths, fedoraAccess);
//                        if (!dcPublishers.isEmpty()) {
//                            JSONArray publishersArray = new JSONArray();
//                            for (int i = 0, ll = dcPublishers.size(); i < ll; i++) {
//                                publishersArray.put(dcPublishers.get(i));
//                            }
//                            map.put(DNNTStatisticsAccessLogImpl.PUBLISHERS_KEY, publishersArray);
//                        }
//
//                        // fill identifiers
//                        Map<String, List<String>> identifiers = DNNTStatisticsAccessLogImpl.identifiers(paths,
//                                fedoraAccess);
//                        identifiers.keySet().forEach(key -> map.put(key, identifiers.get(key)));
//
//                        List<String> dnntLabels = SolrUtils.disectLicenses(solrDoc.getDocumentElement());
//                        map.put(DNNTStatisticsAccessLogImpl.DNNT_LABELS_KEY, dnntLabels);
//
//                        String s = SolrUtils.disectDNNTFlag(solrDoc.getDocumentElement());
//                        if (s != null) {
//                            map.put(DNNTStatisticsAccessLogImpl.DNNT_KEY, Boolean.valueOf(s));
//                        }
//
//                        List<String> runtimeFileds = new ArrayList<>(Arrays.asList(
//                                DNNTStatisticsAccessLogImpl.SOLR_DATE_KEY,
//                                DNNTStatisticsAccessLogImpl.PUBLISHERS_KEY, DNNTStatisticsAccessLogImpl.DNNT_KEY,
//                                DNNTStatisticsAccessLogImpl.DNNT_LABELS_KEY,
//                                DNNTStatisticsAccessLogImpl.PROVIDED_BY_DNNT_KEY));
//                        identifiers.keySet().forEach(runtimeFileds::add);
//
//                        //map.put(RUNTIME_ATTRS, runtimeFileds);
//                        //map.put(MISSING_ATTRS, Arrays.asList("shibboleth", "providedByLabel"));
//                    }
//                } else {
////                   map.put(MISSING_ATTRS, Arrays.asList("shibboleth", "providedByLabel",
////                            DNNTStatisticsAccessLogImpl.SOLR_DATE_KEY, DNNTStatisticsAccessLogImpl.PUBLISHERS_KEY,
////                            DNNTStatisticsAccessLogImpl.DNNT_KEY, DNNTStatisticsAccessLogImpl.DNNT_LABELS_KEY,
////                            DNNTStatisticsAccessLogImpl.PROVIDED_BY_DNNT_KEY));
//
//                }
//            } catch (IOException e) {
//                LOGGER.log(Level.SEVERE, e.getMessage(), e);
//            }
//        } else if (dbversion != null && versionCondition(dbversion.toString(), ">=", "6.6.6") && versionCondition(dbversion.toString(), "<=", "6.6.7")) {
//            // TODO: Delete
//            if (!disbleFedoraAccess) {
//                try {
//                    Document solrDoc = solrAccess.getSolrDataByPid(pid);
//                    if (solrDoc != null) {
//                        List<String> dnntLabels = SolrUtils.disectLicenses(solrDoc.getDocumentElement());
//                        map.put(DNNTStatisticsAccessLogImpl.DNNT_LABELS_KEY, dnntLabels);
//                        //map.put(RUNTIME_ATTRS, Arrays.asList(DNNTStatisticsAccessLogImpl.DNNT_LABELS_KEY));
//                    }
//                } catch (IOException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                }
//            } else {
//                //map.put(MISSING_ATTRS, Arrays.asList(DNNTStatisticsAccessLogImpl.DNNT_LABELS_KEY));
//            }
//        }
        sup.processReportRecord(map);
    }

    public List<String> verifyFilters(ReportedAction action, StatisticsFiltersContainer filters) {
        List<String> list = new ArrayList<>();
        DateFilter dateFilter = filters.getFilter(DateFilter.class);
        boolean flag = dateFilter.getToDate() != null && dateFilter.getFromDate() != null;
        if (!flag) {
            list.add("dateFrom and dateTo are mandatory");
        }
        VerificationUtils.dateVerification(list, dateFilter.getRawFromDate());
        VerificationUtils.dateVerification(list, dateFilter.getRawToDate());
        return list;
    }

    private static Map toMap(JSONObject obj) {
        Map map = new HashMap();
        map.put("pid", obj.optString("pid"));
        map.put("date", obj.optString("date"));
        map.put("remoteAddr", obj.optString("ip_address"));
        if (obj.has("user")) {
            map.put("username", obj.optString("user"));
        }
        
        
        if (obj.has("date.str")) {
            map.put("date.str", obj.getString("date.str"));
            
            int start = obj.getInt("date_range_start.year");
            if (start > -1) {
                map.put("date_range_start.year", start);
                
            }
            int end = obj.getInt("date_range_end.year");
            if (end > -1) {
                map.put("date_range_end.year", end);
            }
            //Issue #1046
            if (start >-1 && start == end ) {
                map.put("date.publication_year", start);
            }
            
        }

        if (obj.has("hrh_referer")) {
            //v SOLR: "hrh_referer":["http://localhost:4321/"],
            String hrhReferer = obj.optString("hrh_referer");
            if (hrhReferer.startsWith("[\"") && hrhReferer.endsWith("\"]")) {
                hrhReferer = hrhReferer.substring(2, hrhReferer.length() - 2);
            }
            map.put("hrh_referer", hrhReferer);
        }
        if (obj.has("hrh_kramerius_client")) {
            if (obj.has("hrh_kramerius_client")) {
                //v SOLR: "hrh_kramerius_client":["kramerius.org"],
                String hrhKrameriusClient = obj.optString("hrh_kramerius_client");
                if (hrhKrameriusClient.startsWith("[\"") && hrhKrameriusClient.endsWith("\"]")) {
                    hrhKrameriusClient = hrhKrameriusClient.substring(2, hrhKrameriusClient.length() - 2);
                }
                map.put("hrh_kramerius_client", hrhKrameriusClient);
            }
        }
        // TODO: Bad name; rename it
        List<String> licenses = licenses(obj);
        map.put("dnnt", licenses.contains("dnntt") || licenses.contains("dnnto"));

        if (obj.has("evaluated_map")) {
            String evaluatedMap = obj.getString("evaluated_map");
            JSONObject evaluatedMapJSON = new JSONObject(evaluatedMap);
            for (Object key : evaluatedMapJSON.keySet()) {
                map.put(key.toString(), evaluatedMapJSON.get(key.toString()));
            }
        }
        if (obj.has("user_session_attributes")) {
            String userSessionAttributes = obj.getString("user_session_attributes");
            JSONObject userSessionAttributesJSON = new JSONObject(userSessionAttributes);
            for (Object key : userSessionAttributesJSON.keySet()) {
                map.put(key.toString(), userSessionAttributesJSON.get(key.toString()));
            }
        }

        JSONArray licJSONArray = new JSONArray();
        licenses.stream().forEach(licJSONArray::put);
        map.put(DNNTStatisticsAccessLogImpl.DNNT_LABELS_KEY, licJSONArray);
        map.put(DNNTStatisticsAccessLogImpl.LICENSES_KEY, licJSONArray);

        // root title
        if (obj.has("root_title")) {
            map.put("rootTitle", obj.optString("root_title"));
        }
        //if (obj.has(""))
        List<String> titles = listString(obj, "titles");
        if (!titles.isEmpty()) {
            map.put("dcTitle", titles.get(0));
        }
        if (obj.has("root_model")) {
            map.put("rootModel", obj.optString("root_model"));
        }
        // map.put("models_path", )
        map.put("models_path", obj.optString("own_model_path"));
        map.put("pids_path", obj.optString("own_pid_path"));

        // Issue #1038 / all_pids_paths, all_models
        if (obj.has("pid_paths")) {
            map.put("all_pids_paths", obj.optJSONArray("pid_paths"));
        }
        if (obj.has("all_models")) {
            map.put("all_models", obj.optJSONArray("all_models"));
        }
        Set keys = obj.keySet();
        for (Object key : keys) {
            if (key.toString().startsWith("pids_")) {
                JSONArray pidsModel = obj.optJSONArray(key.toString());
                if (pidsModel != null) {
                    map.put(key.toString(), pidsModel);
                }
            }
        }

        //List<String> publishers = new ArrayList<>();
        List<String> publishers = listString(obj, "publishers");
        if (!publishers.isEmpty()) {
            map.put("publishers", publishers);
        }
        List<String> authors = listString(obj, "authors");
        if (!authors.isEmpty()) {
            map.put("authors", authors);
        }
        List<String> issns = listString(obj, "id_issn");
        if (!issns.isEmpty()) {
            map.put("issn", issns);
        }
        List<String> isbns = listString(obj, "id_isbn");
        if (!isbns.isEmpty()) {
            map.put("isbn", isbns);
        }

        List<String> cnbs = listString(obj, "id_ccnb");
        if (!isbns.isEmpty()) {
            map.put("ccnb", cnbs);
        }

        return map;
    }

    private static List<String> licenses(JSONObject obj) {
        if (obj.has("licenses")) {
            return listString(obj, "licenses");
        } else if (obj.has("licences")) {
            return listString(obj, "licences");
        } else return new ArrayList<>();
    }

    private static List<String> listString(JSONObject obj, String key) {
        List<String> retvals = new ArrayList<>();
        if (obj.has(key)) {
            JSONArray publisherArray = obj.getJSONArray(key);
            for (int i = 0; i < publisherArray.length(); i++) {
                retvals.add(publisherArray.getString(i));
            }
        }
        return retvals;
    }
    
}
