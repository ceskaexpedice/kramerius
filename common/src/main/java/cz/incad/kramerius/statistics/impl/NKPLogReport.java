package cz.incad.kramerius.statistics.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.statistics.accesslogs.dnnt.DNNTStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.accesslogs.dnnt.date.YearLogFormat;
import cz.incad.kramerius.statistics.accesslogs.utils.SElemUtils;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.accesslogs.database.DatabaseStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.filters.VisibilityFilter;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.Offset;
import org.antlr.stringtemplate.StringTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Creates report for NKP
 *
 */
public class NKPLogReport implements StatisticReport {

    public static final SimpleDateFormat ACCESS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final Logger LOGGER = Logger.getLogger(AuthorReport.class.getName());

    public static final String REPORT_ID = "nkp";


    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    @Inject
    SolrAccess solrAccess;

    @Inject
    @Named("cachedFedoraAccess")
    FedoraAccess fedoraAccess;



    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction reportedAction, StatisticsFiltersContainer filters, Offset rOffset) throws StatisticsReportException {
        throw new UnsupportedOperationException("unsupported for this report");
    }

    @Override
    public List<String> getOptionalValues() {
        return null;
    }

    @Override
    public String getReportId() {
        return REPORT_ID;
    }

    @Override
    public void prepareViews(ReportedAction action, StatisticsFiltersContainer container) throws StatisticsReportException { }

    @Override
    public void processAccessLog(ReportedAction action, StatisticsReportSupport sup, StatisticsFiltersContainer filters) throws StatisticsReportException {
        try {

            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);

            if (dateFilter.getFromDate() != null && dateFilter.getToDate() != null) {
                final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("nkpLogsReport");
                statRecord.setAttribute("action", ReportedAction.READ.name());

                statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
                statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
                statRecord.setAttribute("visibility", visFilter.asMap());


                @SuppressWarnings("rawtypes")
                List params = StatisticUtils.jdbcParams(dateFilter);
                String sql = statRecord.toString();
                Connection conn = connectionProvider.get();

                new StastisticsIteration(sql, params, conn, collectedRecord-> {
                    logReport(collectedRecord, sup);
                }).iterate();

            } else {
                throw new UnsupportedOperationException("Full report is not supported. Please, use dateFrom and dateTo");
            }

        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }

    }

    static class StastisticsIteration extends JDBCQueryTemplate<Object> {


        private Record currentRecord = new Record();

        private Consumer<Record> consumer;
        private String sql;
        private List params;

        public StastisticsIteration(String sql, List params, Connection connection, Consumer<Record> consumer) {
            super(connection);
            this.consumer = consumer;
            this.params = params;
            this.sql = sql;
        }

        public void iterate() {
            executeQuery(sql,params.toArray());
            if (currentRecord != null && currentRecord.pid != null) consumer.accept(currentRecord);
        }

        @Override
        public boolean handleRow(ResultSet rs, List<Object> returnsList) throws SQLException {
            try {
                int recordId = rs.getInt("slrecord_id");
                int detailId = rs.getInt("sddetail_id");
                int authorId = rs.getInt("saauthor_id");
                int publisherId = rs.getInt("sppublisher_id");

                if (currentRecord.isDifferent(recordId)) {
                    if (currentRecord.recordId != -1) {
                        consumer.accept(currentRecord);
                    }
                    currentRecord = Record.load(rs);
                }

                if (currentRecord.lastDetail() == null || currentRecord.lastDetail().isDifferent(detailId)) {
                    currentRecord.details.add(Detail.load(rs));
                }

                if (authorId != -1) {
                    currentRecord.lastDetail().authors.add(Author.load(rs));
                }
                if (publisherId != -1) {
                    currentRecord.lastDetail().publishers.add(Publisher.load(rs));
                }




            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
            return super.handleRow(rs, returnsList);
        }

    }



    void logReport(Record record, StatisticsReportSupport sup) {

        Map map = record.toMap();

        // publshers or solrdate
        if (!map.containsKey("publishers") || !map.containsKey("solrDate")) {
            try {
                boolean disbleFedoraAccess = KConfiguration.getInstance().getConfiguration().getBoolean("nkp.logs.disablefedoraaccess", false);
                if (!disbleFedoraAccess) {
                    // only one place where we are connecting
                    Document solrDoc = solrAccess.getSolrDataDocument(record.pid);
                    if (solrDoc != null) {

                        map.put("solrDate", new YearLogFormat().format(SElemUtils.selem("str", "datum_str", solrDoc)));

                        ObjectPidsPath[] paths = solrAccess.getPath(null, solrDoc);
                        List<String> dcPublishers = DNNTStatisticsAccessLogImpl.dcPublishers(paths, fedoraAccess);
                        if (!dcPublishers.isEmpty()) {
                            JSONArray publishersArray = new JSONArray();
                            for (int i=0,ll=dcPublishers.size();i<ll;i++) {
                                publishersArray.put(dcPublishers.get(i));
                            }
                            map.put("publishers",publishersArray);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }

        }
        sup.processReportRecord(map);
    }

    @Override
    public boolean verifyFilters(ReportedAction action, StatisticsFiltersContainer filters) {
        DateFilter dateFilter = filters.getFilter(DateFilter.class);
        return dateFilter.getToDate() != null && dateFilter.getFromDate() != null;
    }


    static class Record {

        private int recordId=-1;
        private String pid;
        private String date;
        private String remoteIpAddress;
        private String user;
        private String requestedUrl;
        private boolean dnnt;
        private boolean providedbydnnt;
        private String evaluateMap;
        private String userSessionAttributes;

        private List<Detail> details = new ArrayList<>();

        Map toMap() {
            Map map = new HashMap();
            map.put("pid", this.pid);
            map.put("date", this.date);
            map.put("remoteAddr", this.remoteIpAddress);
            if (this.user != null) map.put("username", this.user);
            map.put("dnnt",this.dnnt);
            map.put("providedByDnnt", this.providedbydnnt);
            if (this.evaluateMap != null) {
                JSONObject evalmap =  new JSONObject(this.evaluateMap);
                for (Object key : evalmap.keySet()) {
                    map.put(key.toString(), evalmap.get(key.toString()));
                }
            }
            if (this.userSessionAttributes != null) {
                JSONObject uSessionMap =  new JSONObject(userSessionAttributes);
                for (Object key : uSessionMap.keySet()) {
                    map.put(key.toString(), uSessionMap.get(key.toString()));
                }
            }

            if (!this.details.isEmpty()) {
                // filter branch 0
                List<Detail> firstBranch = this.details.stream().filter(detail -> {
                    return detail.branchId == 0;
                }).collect(Collectors.toList());

                List<Detail> secondBranch = this.details.stream().filter(detail -> {
                    return detail.branchId == 1;
                }).collect(Collectors.toList());

                map.put("rootTitle", firstBranch.get(firstBranch.size() -1).title);
                map.put("dcTitle",  firstBranch.get(0).title);

                map.put("rootPid", firstBranch.get(firstBranch.size() -1).pid);
                map.put("rootModel", firstBranch.get(firstBranch.size() -1).model);

                map.put("policy", firstBranch.get(0).rights);

                List<String> pidPaths = new ArrayList<>();
                List<String> modelPaths = new ArrayList<>();

                List<String> firstBranchPids = firstBranch.stream().map(detail -> {
                    return detail.pid;
                }).collect(Collectors.toList());
                Collections.reverse(firstBranchPids);
                pidPaths.add(firstBranchPids.stream().collect(Collectors.joining("/")));

                if(!secondBranch.isEmpty()) {
                    List<String> secondBranchPids = secondBranch.stream().map(detail -> {
                        return detail.pid;
                    }).collect(Collectors.toList());
                    Collections.reverse(secondBranchPids);
                    pidPaths.add(secondBranchPids.stream().collect(Collectors.joining("/")));
                }

                List<String> firstBranchModels = firstBranch.stream().map(detail -> {
                    return detail.model;
                }).collect(Collectors.toList());
                Collections.reverse(firstBranchModels);
                modelPaths.add(firstBranchModels.stream().collect(Collectors.joining("/")));

                if(!secondBranch.isEmpty()) {
                    List<String> secondBranchModels = secondBranch.stream().map(detail -> {
                        return detail.model;
                    }).collect(Collectors.toList());
                    Collections.reverse(secondBranchModels);
                    modelPaths.add(secondBranchModels.stream().collect(Collectors.joining("/")));
                }


                //map.put("models_path", )
                map.put("models_path", modelPaths);
                map.put("pids_path", pidPaths);

                Optional<String> issuedDate = details.stream().map(detail -> {
                    return detail.issuedDate;
                }).filter(Objects::nonNull).findFirst();

                if (issuedDate.isPresent()) {
                    map.put("publishedDate", issuedDate.get());
                }

                Optional<String> solrDate = details.stream().map(detail -> {
                    return detail.solrDate;
                }).filter(Objects::nonNull).findFirst();

                if (solrDate.isPresent()) {
                    map.put("solrDate", solrDate.get());
                }

                List<String> publishers = details.stream().map(detail -> {
                    return detail.publishers;
                }).flatMap(Collection::stream).map(publisher -> {
                    return publisher.publisher;
                }).filter(Objects::nonNull).distinct().collect(Collectors.toList());

                if (!publishers.isEmpty()) {
                    map.put("publishers", publishers);
                }

                List<String> authors = details.stream().map(detail -> {
                    return detail.authors;
                }).flatMap(Collection::stream).map(author -> {
                    return author.author;
                }).filter(Objects::nonNull).distinct().collect(Collectors.toList());

                if (!authors.isEmpty()) {
                    map.put("authors", authors);
                }
            }
            return map;
        }



        private static  Record load(ResultSet rs) throws SQLException {
            Record nrecord = new Record();
            nrecord.recordId= rs.getInt("slrecord_id");
            nrecord.pid = rs.getString("slpid");
            nrecord.date = ACCESS_DATE_FORMAT.format(rs.getTimestamp("sldate"));
            nrecord.remoteIpAddress=rs.getString("slremote_ip_address");
            nrecord.user=rs.getString("slUSER");
            nrecord.dnnt=rs.getBoolean("sldnnt");
            nrecord.providedbydnnt=rs.getBoolean("slprovidedByDnnt");
            nrecord.evaluateMap=rs.getString("slevaluatemap");
            nrecord.userSessionAttributes=rs.getString("slusersessionattributes");
            return nrecord;
        }

        public boolean isDifferent(int rId) {
            return this.recordId != rId;
        }


        public Detail lastDetail() {
            return details.isEmpty() ? null : details.get(details.size() -1);
        }
    }

    static class Detail {

        int detailId=-1;
        String pid;
        String model;
        String issuedDate;
        String solrDate;
        String rights;
        String title;
        int branchId;

        private List<Author> authors = new ArrayList<>();
        private List<Publisher> publishers = new ArrayList<>();

        private static Detail load(ResultSet rs) throws SQLException {
            Detail detail = new Detail();
            detail.detailId = rs.getInt("sddetail_id");
            detail.pid = rs.getString("sdpid");
            detail.model = rs.getString("sdmodel");
            detail.issuedDate = new YearLogFormat().format(rs.getString("sdissued_date"));
            detail.solrDate = new YearLogFormat().format(rs.getString("sdsolr_date"));
            String rights = rs.getString("sdrights");
            detail.rights= rights != null ? rights.contains(":") ? rights.split(":")[1] : rights : null;
            detail.title= rs.getString("sdtitle");
            detail.branchId = rs.getInt("sdbranch_id");
            return detail;
        }

        public Author lastAuthor() {
            return authors.isEmpty() ? null : authors.get(authors.size() -1);
        }

        public Publisher lastPublisher() {
            return publishers.isEmpty() ? null : publishers.get(publishers.size() -1);
        }

        public boolean isDifferent(int dId) {
            return this.detailId != dId;
        }
    }

    static class Author {

        int authorId=-1;
        String author;

        private static Author load(ResultSet rs) throws SQLException {
            int authorId = rs.getInt("saauthor_id");
            if (authorId != -1) {
                Author author = new Author();
                author.authorId = rs.getInt("saauthor_id");
                author.author = rs.getString("saauthor_name");
                return author;
            } else return null;
        }

        public boolean isDifferent(int aId) {
            return authorId != aId;
        }

    }

    static class Publisher {

        int publisherId=-1;
        String publisher;


        private static Publisher load(ResultSet rs) throws SQLException {
            int publisherId = rs.getInt("sppublisher_id");
            if (publisherId != -1) {
                Publisher publisher = new Publisher();
                publisher.publisherId = rs.getInt("sppublisher_id");
                publisher.publisher = rs.getString("sppublisher_name");
                return publisher;
            } else return null;
        }

        public boolean isDifferent(int pId) {
            return publisherId != pId;
        }

    }
}
