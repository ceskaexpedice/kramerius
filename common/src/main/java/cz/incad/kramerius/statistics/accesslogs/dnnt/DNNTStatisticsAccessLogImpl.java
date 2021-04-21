package cz.incad.kramerius.statistics.accesslogs.dnnt;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.*;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsAccessLogSupport;
import cz.incad.kramerius.statistics.accesslogs.dnnt.date.DNNTStatisticsDateFormat;
import cz.incad.kramerius.statistics.accesslogs.dnnt.date.YearLogFormat;
import cz.incad.kramerius.statistics.accesslogs.utils.SElemUtils;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class DNNTStatisticsAccessLogImpl  implements StatisticsAccessLog {

    // access logger for kibana processing
    public static Logger KRAMERIUS_LOGGER_FOR_KIBANA = Logger.getLogger("kramerius.access");
    protected ThreadLocal<ReportedAction> reportedAction = new ThreadLocal<ReportedAction>();
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DNNTStatisticsAccessLogImpl.class.getName());

    private static final String[] MODS_DATE_XPATHS = {
            "//mods:part/mods:date/text()",
            "//mods:originInfo/mods:dateIssued/text()",
            "//mods:originInfo/mods:dateIssued[@encoding='marc']/text()",
            "//mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()"
    };

    private static final List<XPathExpression> MODS_DATE_XPATH_EXPRS = new ArrayList<XPathExpression>() {{
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        for (String strExpr : MODS_DATE_XPATHS) {
            try {
                add(xpath.compile(strExpr));
            } catch (XPathExpressionException e) {
                LOGGER.log(Level.SEVERE, "Can't compile XPath expression \"" + strExpr + "\"!", e);
            }
        }
    }};

    @Inject
    SolrAccess solrAccess;

    @Inject
    @Named("cachedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    Provider<User> userProvider;

    @Override
    public void reportAccess(String pid, String streamName) throws IOException {
        Document solrDoc = this.solrAccess.getSolrDataDocument(pid);

        ObjectPidsPath[] paths = this.solrAccess.getPath(null, solrDoc);
        ObjectModelsPath[] mpaths = this.solrAccess.getPathOfModels(solrDoc);

        String rootTitle  = SElemUtils.selem("str", "root_title", solrDoc);
        String rootPid  = SElemUtils.selem("str", "root_pid", solrDoc);
        String dctitle = SElemUtils.selem("str", "dc.title", solrDoc);
        String solrDate = SElemUtils.selem("str", "datum_str", solrDoc);
        String dnnt = SElemUtils.selem("bool", "dnnt", solrDoc);
        String policy = SElemUtils.selem("str", "dostupnost", solrDoc);

        List<String> sAuthors = solrAuthors(rootPid, solrAccess);
        List<String> dcPublishers = dcPublishers(paths, fedoraAccess);

        // WRITE TO LOG - kibana processing
        if (reportedAction.get() == null || reportedAction.get().equals(ReportedAction.READ)) {
            log(pid, rootTitle, dctitle, solrDate, findModsDate(paths, fedoraAccess),
                    dnnt, policy, dcPublishers, sAuthors, paths, mpaths);
        }
    }

    public static  List<String> solrAuthors(String rootPid, SolrAccess solrAccess) throws IOException {
        List<String> sAuthors = new ArrayList<>();
        if (rootPid != null) {
            Document rootSolrDoc = solrAccess.getSolrDataDocument(rootPid);
            Element array = XMLUtils.findElement(rootSolrDoc.getDocumentElement(), new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    String attr = element.getAttribute("name");
                    if (nodeName.equals("arr") && StringUtils.isAnyString(attr) && attr.equals("dc.creator"))
                        return true;
                    return false;
                }
            });
            if (array != null) {
                sAuthors = XMLUtils.getElements(array).stream().map(it -> it.getTextContent()).filter(it -> it != null).map(String::trim).collect(Collectors.toList());
            }
        }
        return sAuthors;
    }

    public static  List<String> dcPublishers(ObjectPidsPath[] paths, FedoraAccess fedoraAccess) throws IOException {
        List<String> dcPublishers = new ArrayList<>();
        for (int i = 0, ll = paths.length; i < ll; i++) {
            if (paths[i].contains(SpecialObjects.REPOSITORY.getPid())) {
                paths[i] = paths[i].cutHead(0);
            }
            final int pathIndex = i;
            String[] pathFromLeafToRoot = paths[i].getPathFromLeafToRoot();
            for (int j = 0; j < pathFromLeafToRoot.length; j++) {
                final String detailPid = pathFromLeafToRoot[j];
                Document document = fedoraAccess.getDC(detailPid);

                List<String> collected = Arrays.stream(DCUtils.publishersFromDC(document)).map(it -> {
                    return it.replaceAll("\\r?\\n", " ");
                }).collect(Collectors.toList());
                dcPublishers.addAll(collected);
            }
        }
        return dcPublishers;
    }

    @Override
    public void reportAccess(String pid, String streamName, String actionName) throws IOException {
        ReportedAction action = ReportedAction.valueOf(actionName);
        this.reportedAction.set(action);
        this.reportAccess(pid, streamName);
    }

    @Override
    public boolean isReportingAccess(String pid, String streamName) {
        return false;
    }

    @Override
    public void processAccessLog(ReportedAction reportedAction, StatisticsAccessLogSupport sup) {

    }

    @Override
    public StatisticReport[] getAllReports() {
        return new StatisticReport[0];
    }

    @Override
    public StatisticReport getReportById(String reportId) {
        return null;
    }

    public static String findModsDate(ObjectPidsPath[] paths, FedoraAccess fedoraAccess) {
        for (ObjectPidsPath path : paths) {
            String[] pathFromLeafToRoot = path.getPathFromLeafToRoot();
            for (String detailPid : pathFromLeafToRoot) {
                String modsDate = findModsDateOfPid(detailPid, fedoraAccess);
                if (modsDate != null)
                    return modsDate;
            }
        }
        return null;
    }

    private static String findModsDateOfPid(String pid, FedoraAccess fedoraAccess) {
        Document biblioMods;
        try {
            biblioMods = fedoraAccess.getBiblioMods(pid);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can't get BIBLIO_MODS datastream of " + pid, e);
            return null;
        }

        synchronized (MODS_DATE_XPATH_EXPRS) {
            for (XPathExpression expr : MODS_DATE_XPATH_EXPRS) {
                try {
                    Object date = expr.evaluate(biblioMods, XPathConstants.NODE);
                    if (date != null)
                        return ((Text) date).getData();
                } catch (XPathExpressionException e) {
                    LOGGER.log(Level.WARNING,
                            "An exception occurred while parsing a date from BIBLIO_MODS datastream of " + pid, e);
                }
            }
        }
        return null;
    }

    public void log(String pid, String rootTitle, String dcTitle, String solrDate, String modsDate, String dnntFlag, String policy, List<String> dcPublishers, List<String> dcAuthors, ObjectPidsPath[] paths, ObjectModelsPath[] mpaths) throws IOException {
        User user = this.userProvider.get();
        RightsReturnObject rightsReturnObject = CriteriaDNNTUtils.currentThreadReturnObject.get();
        boolean providedByDnnt =  rightsReturnObject != null ? CriteriaDNNTUtils.allowedByReadDNNTFlagRight(rightsReturnObject) : false;

        // store json object
        JSONObject jObject = toJSON(pid, rootTitle, dcTitle,
                IPAddressUtils.getRemoteAddress(requestProvider.get(), KConfiguration.getInstance().getConfiguration()),
                user != null ? user.getLoginname() : null,
                user != null ? user.getEmail() : null,
                getDate(new YearLogFormat(), solrDate),
                getDate(new YearLogFormat(), modsDate),
                dnntFlag,
                providedByDnnt,
                policy,
                rightsReturnObject.getEvaluateInfoMap(),
                user.getSessionAttributes(),
                dcAuthors,
                dcPublishers,
                paths,
                mpaths
        );

        DNNTStatisticsAccessLogImpl.KRAMERIUS_LOGGER_FOR_KIBANA.log(Level.INFO, jObject.toString());
    }

    public static  JSONObject toJSON(String pid,
                             String rootTitle,
                             String dcTitle,
                             String remoteAddr,
                             String username,
                             String email,
                             String solrDate,
                             String modsDate,
                             String dnntFlag,
                             boolean providedByDnnt,
                             String policy,
                             Map<String,String> rightEvaluationAttribute,
                             Map<String,String> sessionAttributes,
                             List<String> dcAuthors,
                             List<String> dcPublishers,
                             ObjectPidsPath[] paths,
                             ObjectModelsPath[] mpaths) throws IOException {

        LocalDateTime date = LocalDateTime.now();
        String timestamp = date.format(DateTimeFormatter.ISO_DATE_TIME);

        JSONObject jObject = new JSONObject();

        jObject.put("pid",pid);
        jObject.put("remoteAddr",remoteAddr);
        jObject.put("username",username);
        jObject.put("email",email);

        jObject.put("rootTitle",rootTitle);
        jObject.put("dcTitle",dcTitle);

        if (dnntFlag != null )  jObject.put("dnnt", dnntFlag.trim().toLowerCase().equals("true"));

        // info from criteriums
        rightEvaluationAttribute.keySet().stream().forEach(key->{ jObject.put(key, rightEvaluationAttribute.get(key)); });

        jObject.put("providedByDnnt", providedByDnnt);
        jObject.put("policy", policy);


        if (solrDate != null)  jObject.put("solrDate", solrDate);
        if (modsDate != null) jObject.put("publishedDate", modsDate);


        jObject.put("date",timestamp);

        sessionAttributes.keySet().stream().forEach(key->{ jObject.put(key, sessionAttributes.get(key)); });


        if (!dcAuthors.isEmpty()) {
            JSONArray authorsArray = new JSONArray();
            for (int i=0,ll=dcAuthors.size();i<ll;i++) {
                authorsArray.put(dcAuthors.get(i));
            }
            jObject.put("authors",authorsArray);
        }

        if (!dcPublishers.isEmpty()) {
            JSONArray publishersArray = new JSONArray();
            for (int i=0,ll=dcPublishers.size();i<ll;i++) {
                publishersArray.put(dcPublishers.get(i));
            }
            jObject.put("publishers",publishersArray);
        }

        JSONArray pidsArray = new JSONArray();
        for (int i = 0; i < paths.length; i++) {
            pidsArray.put(Arrays.stream(paths[i].getPathFromRootToLeaf()).collect(Collectors.joining("/")));
        }
        jObject.put("pids_path",pidsArray);

        JSONArray modelsArray = new JSONArray();
        for (int i = 0; i < mpaths.length; i++) {
            modelsArray.put(Arrays.stream(mpaths[i].getPathFromRootToLeaf()).collect(Collectors.joining("/")));
        }
        jObject.put("models_path",modelsArray);
        if (paths.length > 0) {
            String[] pathFromRootToLeaf = paths[0].getPathFromRootToLeaf();
            if (pathFromRootToLeaf.length > 0) {
                jObject.put("rootPid",pathFromRootToLeaf[0]);
            }
        }

        if (mpaths.length > 0) {
            String[] mpathFromRootToLeaf = mpaths[0].getPathFromRootToLeaf();
            if (mpathFromRootToLeaf.length > 0) {
                jObject.put("rootModel",mpathFromRootToLeaf[0]);
            }
        }

        return jObject;
    }

    private  String getDate(DNNTStatisticsDateFormat dateFormat, String publishedDate)  {
        if (dateFormat != null && publishedDate != null)
            return dateFormat.format(publishedDate);
        else
            return null;
    }
}
