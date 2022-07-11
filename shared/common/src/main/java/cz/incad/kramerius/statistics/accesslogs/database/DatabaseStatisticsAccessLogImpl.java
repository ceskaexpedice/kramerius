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
package cz.incad.kramerius.statistics.accesslogs.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.*;

import cz.incad.kramerius.*;
import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils;
import cz.incad.kramerius.statistics.accesslogs.AbstractStatisticsAccessLog;
import cz.incad.kramerius.statistics.accesslogs.utils.SElemUtils;
import cz.incad.kramerius.statistics.database.StatisticDbInitializer;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.json.JSONObject;
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
import cz.incad.kramerius.statistics.StatisticsAccessLogSupport;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;

/**
 * @author pavels
 *
 */
public class DatabaseStatisticsAccessLogImpl extends AbstractStatisticsAccessLog {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DatabaseStatisticsAccessLogImpl.class.getName());


    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Inject
    @Named("cachedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;

    @Inject
    Provider<User> userProvider;

    @Inject
    Set<StatisticReport> reports;

    @Inject
    VersionService versionService;

    private XPathFactory xpfactory;


    public DatabaseStatisticsAccessLogImpl() {
        this.xpfactory = XPathFactory.newInstance();
    }

    @Override
    public void reportAccess(final String pid, final String streamName) throws IOException {

        ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
        ObjectModelsPath[] mpaths = this.solrAccess.getModelPaths(pid);

        Connection connection = null;
        try {
            connection = connectionProvider.get();
            if (connection == null)
                throw new NotReadyException("connection not ready");

            List<JDBCCommand> commands = new ArrayList<>();

            Document solrDoc = this.solrAccess.getSolrDataByPid(pid);
            //String dnnt = SElemUtils.selem("bool", "dnnt", solrDoc);

            List<String> licenses = SolrUtils.disectLicenses(solrDoc.getDocumentElement());
            //boolean containsDnntLicense = licenses.stream().filter(it-> {return it.startsWith("dnnt"); }).count() > 0;


            User user = this.userProvider.get();
            RightsReturnObject rightsReturnObject = CriteriaDNNTUtils.currentThreadReturnObject.get();
            Map<String, String> evaluateInfoMap = rightsReturnObject != null ? rightsReturnObject.getEvaluateInfoMap() : new HashMap<>();

            // insert standardni zaznam 
            commands.add(new InsertRecord(pid, loggedUsersSingleton, requestProvider, userProvider, this.reportedAction.get(), false, false, evaluateInfoMap, user.getSessionAttributes(), versionService.getVersion(), licenses));
            for (int i = 0, ll = paths.length; i < ll; i++) {

                if (paths[i].contains(SpecialObjects.REPOSITORY.getPid())) {
                    paths[i] = paths[i].cutHead(0);
                }
                final int pathIndex = i;

                String[] pathFromLeafToRoot = paths[i].getPathFromLeafToRoot();
                for (int j = 0; j < pathFromLeafToRoot.length; j++) {
                    final String detailPid = pathFromLeafToRoot[j];

                    String kModel = fedoraAccess.getKrameriusModelName(detailPid);
                    //Document sDoc = this.solrAccess.getSolrDataByPid(pid);
                    Document dc = null;
                    try {
                        dc = fedoraAccess.getDC(detailPid);
                    } catch (IOException e) {
                        LOGGER.warning("datastream DC not found for " + detailPid + ", ignoring statistics");
                    }
                    if (dc != null) {
                        Object dateFromDC = DCUtils.dateFromDC(dc);
                        dateFromDC = dateFromDC != null ? dateFromDC : new JDBCUpdateTemplate.NullObject(String.class);

                        Object dateFromSolr = SElemUtils.selem("str", "datum_str", solrDoc);
                        dateFromSolr = dateFromSolr != null ? dateFromSolr : new JDBCUpdateTemplate.NullObject(String.class);

                        Object languageFromDc = DCUtils.languageFromDC(dc);
                        languageFromDc = languageFromDc != null ? languageFromDc : new JDBCUpdateTemplate.NullObject(String.class);

                        Object title = DCUtils.titleFromDC(dc);
                        title = title != null ? title : new JDBCUpdateTemplate.NullObject(String.class);

                        Object rights = DCUtils.rightsFromDC(dc);
                        rights = rights != null ? rights : new JDBCUpdateTemplate.NullObject(String.class);

                        Document mods = fedoraAccess.getBiblioMods(detailPid);
                        List<String> languagesFromMods = null;

                        Map<String, List<String>> identifiers = null;
                        try {
                            identifiers = ModsUtils.identifiersFromMods(mods);
                        } catch (XPathExpressionException e) {
                            Logger.getLogger(DatabaseStatisticsAccessLogImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                        }

                        try {
                            languagesFromMods = ModsUtils.languagesFromMods(mods);
                        } catch (XPathExpressionException ex) {
                            Logger.getLogger(DatabaseStatisticsAccessLogImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                        }

                        if (!languagesFromMods.isEmpty()) {
                            for (String languageFromMods : languagesFromMods) {
                                InsertDetail insertDetail = new InsertDetail(detailPid, kModel, rights, dateFromDC, dateFromSolr, languageFromMods, title, identifiers, pathIndex);
                                commands.add(insertDetail);
                            }
                        } else {
                            InsertDetail insertDetail = new InsertDetail(detailPid, kModel, rights, dateFromDC, dateFromSolr, languageFromDc, title, identifiers, pathIndex);
                            commands.add(insertDetail);
                        }

                        String[] creatorsFromDC = DCUtils.creatorsFromDC(dc);
                        for (String cr : creatorsFromDC) {
                            InsertAuthor insertAuth = new InsertAuthor(cr);
                            commands.add(insertAuth);
                        }

                        String[] publishersFromDC = DCUtils.publishersFromDC(dc);
                        for (String p : publishersFromDC) {
                            InsertPublisher inserPublisher = new InsertPublisher(p);
                            commands.add(inserPublisher);
                        }
                    }
                }
            }
            //  WRITE TO DATABASE
            JDBCTransactionTemplate transactionTemplate = new JDBCTransactionTemplate(connection, true);
            transactionTemplate.updateWithTransaction(commands);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                DatabaseUtils.tryClose(connection);
            }
        }
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

            private int record_id = -1;

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

                Map<String, Object> record = new HashMap<String, Object>();
                {
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


                Map<String, Object> record = new HashMap<String, Object>();
                {
                    record.put("pid", pid);
                    record.put("date", d);
                    record.put("remote_ip_address", remote);
                    record.put("user", user);
                    record.put("action", action);
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
        String url = requestProvider.get().getRequestURL().toString() + "?" + requestProvider.get().getQueryString();
        return url;
    }


    public static class InsertRecord extends JDBCCommand {

        private LoggedUsersSingleton loggedUserSingleton;
        private Provider<HttpServletRequest> requestProvider;
        private Provider<User> userProvider;
        private String pid;
        private ReportedAction action;

        private boolean dnnt;
        private boolean providedByDnnt;

        private Map evaluateMap;
        private Map userSesionAttributes;
        private String dbversion;

        private String[] dnnt_labels;

        public InsertRecord(String pid, LoggedUsersSingleton loggedUserSingleton, Provider<HttpServletRequest> requestProvider, Provider<User> userProvider, ReportedAction action, boolean dnnt, boolean providedByDnnt, Map evaulateMap, Map userSesionAttributes, String dbversion, List<String> dnnt_labels) {
            super();
            this.loggedUserSingleton = loggedUserSingleton;
            this.requestProvider = requestProvider;
            this.userProvider = userProvider;
            this.pid = pid;
            this.action = action;

            this.dbversion = dbversion;

            this.dnnt = dnnt;
            this.providedByDnnt = providedByDnnt;

            this.evaluateMap = evaulateMap;
            this.userSesionAttributes = userSesionAttributes;

            this.dnnt_labels = dnnt_labels != null ? dnnt_labels.toArray(new String[dnnt_labels.size()]) : new String[0];
        }


        public Object executeJDBCCommand(Connection con) throws SQLException {
            Map<String, Integer> previousResult = (Map<String, Integer>) getPreviousResult();
            if (previousResult == null) previousResult = new HashMap<String, Integer>();

            final StringTemplate statRecord = stGroup.getInstanceOf("insertStatisticRecord");
            String sessionId = requestProvider.get().getSession().getId();
            boolean logged = loggedUserSingleton.isLoggedUser(requestProvider);
            Object user = logged ? userProvider.get().getLoginname() : new JDBCUpdateTemplate.NullObject(String.class);

            String url = disectedURL(requestProvider); //requestProvider.get().getRequestURL().toString() +"?"+requestProvider.get().getQueryString();
            int record_id = new JDBCUpdateTemplate(con, false)
                    .executeUpdate(
                            statRecord.toString(),
                            pid,
                            new java.sql.Timestamp(System.currentTimeMillis()),
                            requestProvider.get().getRemoteAddr(),
                            user,
                            url,
                            action != null ? action.name() : ReportedAction.READ.name(),
                            sessionId,
                            dnnt,
                            providedByDnnt,
                            evaluateMap != null && !evaluateMap.isEmpty() ? new JSONObject(evaluateMap).toString() : new JDBCUpdateTemplate.NullObject(String.class),
                            userSesionAttributes != null && !userSesionAttributes.isEmpty() ? new JSONObject(userSesionAttributes).toString() : new JDBCUpdateTemplate.NullObject(String.class),
                            dbversion,
                            dnnt_labels
                    );


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
        private Object solrDate = null;
        private int pathIndex = 0;
        private Object rights = null;

        private String[] isbn = new String[0];
        private String[] issn = new String[0];
        private String[] ccnb = new String[0];

        public InsertDetail(String detailPid, String kModel, Object rights, Object date, Object solrDate, Object language, Object title, Map<String, List<String>> modsIdents, int pathIndex) {
            super();
            this.detailPid = detailPid;
            this.kModel = kModel;
            this.language = language;
            this.title = title;
            this.date = date;
            this.rights = rights;
            this.pathIndex = pathIndex;
            this.solrDate = solrDate;

            if (modsIdents.containsKey(ISBN_MODS_KEY)) {
                isbn = modsIdents.get(ISBN_MODS_KEY).toArray(new String[modsIdents.get(ISBN_MODS_KEY).size()]);
            }
            if (modsIdents.containsKey(ISSN_MODS_KEY)) {
                issn = modsIdents.get(ISSN_MODS_KEY).toArray(new String[modsIdents.get(ISSN_MODS_KEY).size()]);
            }
            if (modsIdents.containsKey(CCNB_MODS_KEY)) {
                ccnb = modsIdents.get(CCNB_MODS_KEY).toArray(new String[modsIdents.get(CCNB_MODS_KEY).size()]);
            }
        }

        @Override
        public Object executeJDBCCommand(Connection con) throws SQLException {
            Map<String, Integer> previousResult = (Map<String, Integer>) getPreviousResult();
            if (previousResult == null) previousResult = new HashMap<String, Integer>();

            final StringTemplate detail = stGroup.getInstanceOf("insertStatisticRecordDetail");
            String sql = detail.toString();
            int record_id = previousResult.get("record_id");

            int detail_id = new JDBCUpdateTemplate(con, false)
                    .executeUpdate(sql, detailPid, kModel, date, solrDate, rights, language, title, pathIndex, record_id, issn, isbn, ccnb);

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


    public static class InsertPublisher extends JDBCCommand {

        private String publisher;

        public InsertPublisher(String publisher) {
            super();
            this.publisher = publisher;
        }


        @Override
        public Object executeJDBCCommand(Connection con) throws SQLException {
            Map<String, Integer> previousResult = (Map<String, Integer>) getPreviousResult();
            if (previousResult == null) previousResult = new HashMap<String, Integer>();

            final StringTemplate detail = stGroup.getInstanceOf("insertStatisticRecordDetailPublisher");
            String sql = detail.toString();
            int record_id = previousResult.get("record_id");
            int detail_id = previousResult.get("detail_id");

            new JDBCUpdateTemplate(con, false)
                    .executeUpdate(sql, this.publisher, detail_id, record_id);

            return previousResult;
        }
    }


    public static StringTemplateGroup stGroup;

    static {
        InputStream is = DatabaseStatisticsAccessLogImpl.class.getResourceAsStream("res/statistics.stg");
        stGroup = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
    }

    
    
	@Override
	public int cleanData(Date dateFrom, Date dateTo) throws IOException{
        try {
        	// musi se udelat jinak
			return new JDBCUpdateTemplate(connectionProvider.get(), true)
			.executeUpdate("delete from statistics_access_log where date >= ? AND date<=?", new java.sql.Date(dateFrom.getTime()), new java.sql.Date(dateTo.getTime()));
		} catch (SQLException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void refresh() throws IOException {
		try {
			LOGGER.info("Refreshing materialized views (lang)");
			InputStream langIs = StatisticDbInitializer.class.getResourceAsStream("res/refreshlang.sql");
			JDBCUpdateTemplate langTemplate = new JDBCUpdateTemplate(connectionProvider.get(), true);
			langTemplate.setUseReturningKeys(false);
			langTemplate.executeUpdate(IOUtils.readAsString(langIs, Charset.forName("UTF-8"), true));
			
			
			LOGGER.info("Refreshing materialized views (authors)");
			InputStream authorsIs = StatisticDbInitializer.class.getResourceAsStream("res/refreshauthors.sql");
			JDBCUpdateTemplate authorsTemplate = new JDBCUpdateTemplate(connectionProvider.get(), true);
			authorsTemplate.setUseReturningKeys(false);
			authorsTemplate.executeUpdate(IOUtils.readAsString(authorsIs, Charset.forName("UTF-8"), true));

			LOGGER.info("Refreshing materialized views (models)");
			InputStream modelsIs = StatisticDbInitializer.class.getResourceAsStream("res/refreshmodels.sql");
			JDBCUpdateTemplate modelsTemplate = new JDBCUpdateTemplate(connectionProvider.get(), true);
			modelsTemplate.setUseReturningKeys(false);
			modelsTemplate.executeUpdate(IOUtils.readAsString(modelsIs, Charset.forName("UTF-8"), true));

		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
		}

	}
}
