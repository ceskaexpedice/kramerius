package cz.incad.kramerius.statistics.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectModelsPath;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


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
    @Named("securedFedoraAccess")
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

            if (dateFilter.getFromDate() != null && dateFilter.getToDate() != null) {
                final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("nkpLogsReport");
                statRecord.setAttribute("action", ReportedAction.READ.name());

                statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
                statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);

                @SuppressWarnings("rawtypes")
                List params = StatisticUtils.jdbcParams(dateFilter);
                //statRecord.setAttribute("paging", true);
                String sql = statRecord.toString();
                Connection conn = connectionProvider.get();
                new JDBCQueryTemplate<Map<String,Object>>(conn) {
                    @Override
                    public boolean handleRow(ResultSet rs, List<Map<String,Object>> returnsList) throws SQLException {
                        String pid = rs.getString("pid");

                        Map<String, Object> map = null;
                        try {
                            Document solrDoc = solrAccess.getSolrDataDocument(pid);

                            map = new HashMap<>();

                            map.put("pid",pid);
                            Timestamp date = rs.getTimestamp("date");

                            map.put("date",ACCESS_DATE_FORMAT.format(date));
                            map.put("remoteAddr",rs.getString("remote_ip_address"));
                            map.put("username",rs.getString("user"));
                            //map.put("email","-not-defined-");
                            map.put("dnnt",rs.getBoolean("dnnt"));
                            map.put("providedByDnnt",rs.getBoolean("providedByDnnt"));

                            String evaluatemap = rs.getString("evaluatemap");
                            if (evaluatemap != null) {
                                JSONObject evalmap =  new JSONObject(evaluatemap);
                                for (Object key : evalmap.keySet()) {
                                    map.put(key.toString(), evalmap.get(key.toString()));

                                }
                            }

                            String usersessionattributes = rs.getString("usersessionattributes");
                            if (usersessionattributes != null) {
                                JSONObject uSessionMap =  new JSONObject(usersessionattributes);
                                for (Object key :
                                        uSessionMap.keySet()) {
                                    map.put(key.toString(), uSessionMap.get(key.toString()));
                                }
                            }

                            String rights = rs.getString("rights");
                            if (rights.contains(":")) {
                                map.put("policy", rights.split(":")[1]);
                            } else {
                                map.put("policy",rights);
                            }

                            ObjectPidsPath[] paths = solrAccess.getPath(null, solrDoc);
                            ObjectModelsPath[] mpaths = solrAccess.getPathOfModels(solrDoc);

                            map.put("rootTitle", SElemUtils.selem("str", "root_title", solrDoc));
                            map.put("rootPid", SElemUtils.selem("str", "root_pid", solrDoc));
                            map.put("dcTitle", SElemUtils.selem("str", "dc.title", solrDoc));

                            map.put("solrDate", new YearLogFormat().format(SElemUtils.selem("str", "datum_str", solrDoc)));
                            String modsDate = DNNTStatisticsAccessLogImpl.findModsDate(paths, fedoraAccess);
                            if (modsDate != null) map.put("publishedDate", new YearLogFormat().format(modsDate));

                            List<String> sAuthors = DNNTStatisticsAccessLogImpl.solrAuthors(SElemUtils.selem("str", "root_pid", solrDoc), solrAccess);
                            if (!sAuthors.isEmpty()) {
                                JSONArray authorsArray = new JSONArray();
                                for (int i=0,ll=sAuthors.size();i<ll;i++) {
                                    authorsArray.put(sAuthors.get(i));
                                }
                                map.put("authors",authorsArray);
                            }


                            List<String> dcPublishers = DNNTStatisticsAccessLogImpl.dcPublishers(paths, fedoraAccess);
                            if (!dcPublishers.isEmpty()) {
                                JSONArray publishersArray = new JSONArray();
                                for (int i=0,ll=dcPublishers.size();i<ll;i++) {
                                    publishersArray.put(dcPublishers.get(i));
                                }
                                map.put("publishers",publishersArray);
                            }


                            JSONArray pidsArray = new JSONArray();
                            for (int i = 0; i < paths.length; i++) {
                                pidsArray.put(Arrays.stream(paths[i].getPathFromRootToLeaf()).collect(Collectors.joining("/")));
                            }
                            map.put("pids_path",pidsArray);

                            JSONArray modelsArray = new JSONArray();
                            for (int i = 0; i < mpaths.length; i++) {
                                modelsArray.put(Arrays.stream(mpaths[i].getPathFromRootToLeaf()).collect(Collectors.joining("/")));
                            }
                            map.put("models_path",modelsArray);

                            if (paths.length > 0) {
                                String[] pathFromRootToLeaf = paths[0].getPathFromRootToLeaf();
                                if (pathFromRootToLeaf.length > 0) {
                                    map.put("rootPid",pathFromRootToLeaf[0]);
                                }
                            }

                            if (mpaths.length > 0) {
                                String[] mpathFromRootToLeaf = mpaths[0].getPathFromRootToLeaf();
                                if (mpathFromRootToLeaf.length > 0) {
                                    map.put("rootModel",mpathFromRootToLeaf[0]);
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        }
                        sup.processReportRecord(map);
                        return super.handleRow(rs, returnsList);
                    }
                }.executeQuery(sql.toString(),params.toArray());

            } else {
                throw new UnsupportedOperationException("Full report is not supported. Please, use dateFrom and dateTo");
            }

        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }

    }



    @Override
    public boolean verifyFilters(ReportedAction action, StatisticsFiltersContainer filters) {
        DateFilter dateFilter = filters.getFilter(DateFilter.class);
        return dateFilter.getToDate() != null && dateFilter.getFromDate() != null;
    }


    public Map<String, Object> toMap(JSONObject object) {
        Map<String, Object> results = new HashMap<String, Object>();
        object.keySet().forEach(key ->{
            Object o = object.get(key.toString());
            results.put(key.toString(), o);
        });
        return results;
    }
}
