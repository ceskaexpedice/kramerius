/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package cz.incad.kramerius.statistics.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.*;

import cz.incad.kramerius.*;
import cz.incad.kramerius.statistics.impl.dnnt.DNNTStatisticSupport;
import cz.incad.kramerius.statistics.impl.dnnt.format.YearLogFormat;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.pdf.utils.ModsUtils;
import cz.incad.kramerius.processes.NotReadyException;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsAccessLogSupport;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author pavels
 *
 */
public class DatabaseStatisticsAccessLogImpl implements StatisticsAccessLog {


    public static String[] MODS_XPATHS={"//mods:originInfo/mods:dateIssued[@encoding='marc']/text()","//mods:originInfo/mods:dateIssued/text()","//mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()","//mods:part/mods:date/text()"};



    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DatabaseStatisticsAccessLogImpl.class.getName());
    
    
    static Map<String, ReportedAction> ACTIONS = new HashMap<String, ReportedAction>();
    static {
        ACTIONS.put("img", ReportedAction.READ);
        ACTIONS.put("pdf", ReportedAction.PDF);
        ACTIONS.put("print", ReportedAction.PRINT);
        ACTIONS.put("zoomify", ReportedAction.READ);
    }
    
    protected ThreadLocal<ReportedAction> reportedAction = new ThreadLocal<ReportedAction>();
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    @Inject
    SolrAccess solrAccess;
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    Set<StatisticReport> reports;

    private XPathFactory xpfactory;

    public DatabaseStatisticsAccessLogImpl() {
        this.xpfactory = XPathFactory.newInstance();
    }

    @Override
    public void reportAccess(final String pid, final String streamName) throws IOException {
        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        ObjectModelsPath[] mpaths = this.solrAccess.getPathOfModels(pid);

        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");

            Map<String, Document> dcMap = new HashMap<>();
            Map<String, Document> modsMap = new HashMap<>();

            List<JDBCCommand> commands = new ArrayList<JDBCCommand>();
            commands.add(new InsertRecord(pid, loggedUsersSingleton, requestProvider, userProvider, this.reportedAction.get()));
            for (int i = 0, ll = paths.length; i < ll; i++) {

                if (paths[i].contains(SpecialObjects.REPOSITORY.getPid())) {
                    paths[i] = paths[i].cutHead(0);
                }
                final int pathIndex = i;
                
                String[] pathFromLeafToRoot = paths[i].getPathFromLeafToRoot();
                for (int j = 0; j < pathFromLeafToRoot.length; j++) {
                    final String detailPid = pathFromLeafToRoot[j];
                    Document dc = fedoraAccess.getDC(detailPid);
                    dcMap.put(detailPid, dc);

                    Document biblioMods = fedoraAccess.getBiblioMods(detailPid);
                    modsMap.put(detailPid, biblioMods);

                }

                for (int j = 0; j < pathFromLeafToRoot.length; j++) {
                    final String detailPid = pathFromLeafToRoot[j];

                    String kModel = fedoraAccess.getKrameriusModelName(detailPid);
                    Document dc = dcMap.containsKey(detailPid) ? dcMap.get(detailPid) : fedoraAccess.getDC(detailPid);

                    Object dateFromDC = DCUtils.dateFromDC(dc);
                    dateFromDC = dateFromDC != null ? dateFromDC : new JDBCUpdateTemplate.NullObject(String.class);
                    
                    Object languageFromDc = DCUtils.languageFromDC(dc);
                    languageFromDc = languageFromDc != null ? languageFromDc : new JDBCUpdateTemplate.NullObject(String.class);
                                      
                    Object title = DCUtils.titleFromDC(dc);
                    title = title != null ? title : new JDBCUpdateTemplate.NullObject(String.class);

                    Object rights = DCUtils.rightsFromDC(dc);
                    rights = rights != null ? rights : new JDBCUpdateTemplate.NullObject(String.class);
                    
                    Document mods =  fedoraAccess.getBiblioMods(pid);
                    ArrayList<String> languagesFromMods = null;
                   
                    try {
                        languagesFromMods = ModsUtils.languagesFromMods(mods);
                    } catch (XPathExpressionException ex) {
                        Logger.getLogger(DatabaseStatisticsAccessLogImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    if (!languagesFromMods.isEmpty()) {
                        for (String languageFromMods : languagesFromMods) {
                            InsertDetail insertDetail = new InsertDetail(detailPid, kModel, rights, dateFromDC, languageFromMods, title, pathIndex);
                            commands.add(insertDetail);
                        }
                    }
                    else {
                        InsertDetail insertDetail = new InsertDetail(detailPid, kModel, rights, dateFromDC, languageFromDc, title, pathIndex); 
                        commands.add(insertDetail);
                    }
                    
                    String[] creatorsFromDC = DCUtils.creatorsFromDC(dc);
                    for (String cr : creatorsFromDC) {
                        InsertAuthor insertAuth = new InsertAuthor(cr);
                        commands.add(insertAuth);
                    }
                }
            }



            // Prepare data from solr
            Document solrDoc = this.solrAccess.getSolrDataDocument(pid);
            String rootTitle  = selem("str", "root_title", solrDoc);
            String rootPid  = selem("str", "root_pid", solrDoc);
            String dctitle = selem("str", "dc.title", solrDoc);
            String solrDate = selem("str", "datum_str", solrDoc);

            String dnnt = selem("bool", "dnnt", solrDoc);
            String policy = selem("str", "dostupnost", solrDoc);

            List<String> dcAuthors = new ArrayList<>();
            if (rootPid != null) {
                Document rootSolrDoc = this.solrAccess.getSolrDataDocument(rootPid);
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
                    dcAuthors = XMLUtils.getElements(array).stream().map(it -> it.getTextContent()).filter(it -> it != null).map(String::trim).collect(Collectors.toList());
                }
            }

            List<String> dcPublishers = new ArrayList<>();
            for (int i = 0, ll = paths.length; i < ll; i++) {
                if (paths[i].contains(SpecialObjects.REPOSITORY.getPid())) {
                    paths[i] = paths[i].cutHead(0);
                }
                final int pathIndex = i;
                String[] pathFromLeafToRoot = paths[i].getPathFromLeafToRoot();
                for (int j = 0; j < pathFromLeafToRoot.length; j++) {
                    final String detailPid = pathFromLeafToRoot[j];
                    Document document = dcMap.get(detailPid);

                    List<String> collected = Arrays.stream(DCUtils.publishersFromDC(document)).map(it -> {
                        return it.replaceAll("\\r?\\n", " ");
                    }).collect(Collectors.toList());

                    dcPublishers.addAll(collected);
                }
            }



            // WRITE TO LOG - kibana processing
            if (reportedAction.get() == null || reportedAction.get().equals(ReportedAction.READ)) {
                new DNNTStatisticSupport(this.requestProvider, this.userProvider, new YearLogFormat()).log(pid,rootTitle,dctitle,solrDate, findModsDate(paths, modsMap) , dnnt,policy, dcPublishers ,dcAuthors, paths, mpaths);
            }

            //  WRITE TO DATABASE
            JDBCTransactionTemplate transactionTemplate = new JDBCTransactionTemplate(connection, true);
            transactionTemplate.updateWithTransaction(commands);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private String  findModsDate(ObjectPidsPath[] paths, Map<String, Document> modsMap) {
        try {
            for (int i = 0; i < paths.length; i++) {
                String[] pathFromLeafToRoot = paths[i].getPathFromLeafToRoot();

                for (int j = 0; j < pathFromLeafToRoot.length; j++) {
                    String detailPid = pathFromLeafToRoot[j];
                    for (String xPathExpression : MODS_XPATHS) {
                        XPath xpath = xpfactory.newXPath();
                        xpath.setNamespaceContext(new FedoraNamespaceContext());
                        XPathExpression expr = xpath.compile(xPathExpression);
                        Object date = expr.evaluate( modsMap.get(detailPid), XPathConstants.NODE);
                        if (date != null) return ((Text) date).getData();
                    }
                }
            }
            return null;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return null;
        }
    }


    private String selem(String type, String attrVal, Document solrDoc) {
        Element dcElm = XMLUtils.findElement(solrDoc.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                String attr = element.getAttribute("name");
                if (nodeName.equals(type) && StringUtils.isAnyString(attr) && attr.equals(attrVal)) return true;
                return false;
            }
        });
        return dcElm != null ? dcElm.getTextContent() : null;
    }



    @Override
    public void reportAccess(String pid, String streamName, String actionName) throws IOException {
        ReportedAction action = ReportedAction.valueOf(actionName);
        this.reportedAction.set(action);
        this.reportAccess(pid, streamName);
    }



    @Override
    public boolean isReportingAccess(String pid, String streamName) {
        return streamName.equals(ImageStreams.IMG_FULL.name()) || streamName.equals(ImageStreams.IMG_PREVIEW.name()); 
    }
    

    
    
    
    @Override
    public void processAccessLog(final ReportedAction repAction, final StatisticsAccessLogSupport sup) {
        // TODO Auto-generated method stub
        final StringTemplate records = stGroup.getInstanceOf("exportAllRecord");
        String sql = records.toString();

        new JDBCQueryTemplate<String>(this.connectionProvider.get()) {
            
            private int record_id=-1;
            
            @Override
            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                int record_id = rs.getInt("record_id");
                if (this.record_id == -1 || this.record_id != record_id) {
                    processMaster(rs);
                    processDetail(rs);
                } else {
                    processDetail(rs);
                }
                this.record_id = record_id;
                
                return super.handleRow(rs, returnsList);
            }

            private void processDetail(ResultSet rs) throws SQLException {
                String model = rs.getString("dmodel");
                String pid = rs.getString("dpid");
                String issuedDate = rs.getString("dissued_date");
                String rights = rs.getString("drights");
                String lang = rs.getString("dlang");
                String title = rs.getString("dtitle");

                Map<String, Object> record = new HashMap<String, Object>(); {
                    record.put("model", model);
                    record.put("pid", pid);
                    record.put("issued_date", issuedDate);
                    record.put("rights", rights);
                    record.put("lang", lang);
                    record.put("title", title);
                }
                sup.processDetailRecord(record);
            } 

            private void processMaster(ResultSet rs) throws SQLException {
                String pid = rs.getString("spid");
                Date d = rs.getDate("sdate");
                String remote = rs.getString("sremote_ip_address");
                String user = rs.getString("suser");
                String requestedUrl = rs.getString("srequested_url");
                String action = rs.getString("sstat_action");
                String sessionId = rs.getString("ssession_id");


                Map<String, Object> record = new HashMap<String, Object>(); {
                    record.put("pid", pid);
                    record.put("date", d);
                    record.put("remote_ip_address", remote);
                    record.put("user", user);
                    record.put("action",action);
                    record.put("session_id", sessionId);
                    record.put("requested_url", requestedUrl);
                }
                sup.processMainRecord(record);
                
            }
            
        }.executeQuery(sql);
        
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

    public static String disectedURL(Provider<HttpServletRequest> requestProvider) {
        String url = requestProvider.get().getRequestURL().toString() +"?"+requestProvider.get().getQueryString();
        return url;
    }
    
 
    public static class InsertRecord extends JDBCCommand {
        
        private LoggedUsersSingleton loggedUserSingleton;
        private Provider<HttpServletRequest> requestProvider;
        private Provider<User> userProvider;
        private String pid;
        private ReportedAction action;
        
        public InsertRecord(String pid,LoggedUsersSingleton loggedUserSingleton, Provider<HttpServletRequest> requestProvider, Provider<User> userProvider, ReportedAction action) {
            super();
            this.loggedUserSingleton = loggedUserSingleton;
            this.requestProvider = requestProvider;
            this.userProvider = userProvider;
            this.pid = pid;
            this.action = action;
        }


        public Object executeJDBCCommand(Connection con) throws SQLException {
            Map<String, Integer> previousResult = (Map<String, Integer>) getPreviousResult();
            if (previousResult == null) previousResult = new HashMap<String, Integer>();
            
            final StringTemplate statRecord = stGroup.getInstanceOf("insertStatisticRecord");
            String sessionId = requestProvider.get().getSession().getId();
            boolean logged = loggedUserSingleton.isLoggedUser(requestProvider);
            Object user = logged ? userProvider.get().getLoginname() : new JDBCUpdateTemplate.NullObject(String.class);
            String url = disectedURL(requestProvider); //requestProvider.get().getRequestURL().toString() +"?"+requestProvider.get().getQueryString();
            int record_id  = new JDBCUpdateTemplate(con, false)
                .executeUpdate(statRecord.toString(), pid, new java.sql.Timestamp(System.currentTimeMillis()), requestProvider.get().getRemoteAddr(), user, url, action != null ? action.name() : ReportedAction.READ.name() , sessionId);
            previousResult.put("record_id", new Integer(record_id));
            return previousResult;
        }
    }

    public static class InsertDetail extends JDBCCommand {

        private String detailPid = null;
        private String kModel = null;
        private Object language = null;
        private Object title = null;
        private Object date = null;
        private int pathIndex = 0;
        private Object rights=null;
        
        public InsertDetail(String detailPid, String kModel,Object rights ,Object date, Object language, Object title, int pathIndex) {
            super();
            this.detailPid = detailPid;
            this.kModel = kModel;
            this.language = language;
            this.title = title;
            this.date = date;
            this.rights = rights;
            this.pathIndex = pathIndex;
        }

        @Override
        public Object executeJDBCCommand(Connection con) throws SQLException {
            Map<String, Integer> previousResult = (Map<String, Integer>) getPreviousResult();
            if (previousResult == null) previousResult = new HashMap<String, Integer>();

            final StringTemplate detail = stGroup.getInstanceOf("insertStatisticRecordDetail");
            String sql = detail.toString();

            int record_id = previousResult.get("record_id");
            
            //(detail_ID, PID,model,ISSUED_DATE,RIGHTS, LANG, TITLE, BRANCH_ID,RECORD_ID)
            int detail_id  = new JDBCUpdateTemplate(con, false)
                .executeUpdate(sql, detailPid, kModel, date, rights , language, title, pathIndex,record_id);
            
            previousResult.put("detail_id", detail_id);
            
            return previousResult;
        } 
    }

    public static class InsertAuthor extends JDBCCommand {
        
        private String authorName;

        public InsertAuthor(String authorName) {
            super();
            this.authorName = authorName;
        }


        @Override
        public Object executeJDBCCommand(Connection con) throws SQLException {
            Map<String, Integer> previousResult = (Map<String, Integer>) getPreviousResult();
            if (previousResult == null) previousResult = new HashMap<String, Integer>();

            final StringTemplate detail = stGroup.getInstanceOf("insertStatisticRecordDetailAuthor");
            String sql = detail.toString();
            int record_id = previousResult.get("record_id");
            int detail_id = previousResult.get("detail_id");
            
            new JDBCUpdateTemplate(con, false)
            .executeUpdate(sql, this.authorName, detail_id, record_id);
            
            return previousResult;
        }
    }
    

    public static StringTemplateGroup stGroup;
    static {
        InputStream is = DatabaseStatisticsAccessLogImpl.class.getResourceAsStream("res/statistics.stg");
        stGroup = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
    }


}
