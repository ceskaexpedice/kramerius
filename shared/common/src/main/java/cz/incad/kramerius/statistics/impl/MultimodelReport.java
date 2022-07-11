package cz.incad.kramerius.statistics.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.statistics.accesslogs.database.DatabaseStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.IdentifiersFilter;
import cz.incad.kramerius.statistics.filters.LicenseFilter;
import cz.incad.kramerius.statistics.filters.ModelFilter;
import cz.incad.kramerius.statistics.filters.MultimodelFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.filters.VisibilityFilter;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.Offset;

public class MultimodelReport implements StatisticReport {
    private static final List<String> SUPPORTED_MODELS = Arrays.asList("monograph", "periodical", "article", "convolute", "map", "graphic", "archive",
            "manuscript", "soundrecording", "collection");

    public static final Logger LOGGER = Logger.getLogger(MultimodelReport.class.getName());

    public static final String REPORT_ID = "multimodel";

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction reportedAction, StatisticsFiltersContainer filters,
            Offset rOffset) throws StatisticsReportException {

        MultimodelFilter mfilter = new MultimodelFilter();
        mfilter.setModels(SUPPORTED_MODELS);

        Map<String, Object> multimodels = new HashMap<>();
        Map<String, Object> sums = new HashMap<>();

        mfilter.getModels().stream().forEach(model -> {
            ModelFilter modelFilter = new ModelFilter();
            modelFilter.setModel(model);

            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
            LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);
            IdentifiersFilter idFilter = filters.getFilter(IdentifiersFilter.class);

            StatisticsFiltersContainer container = new StatisticsFiltersContainer(
                    new StatisticsFilter[] { dateFilter, visFilter, licFilter, modelFilter, idFilter });

            ModelStatisticReport report = new ModelStatisticReport();
            report.solrAccess = this.solrAccess;

            List<Map<String, Object>> reportPage = report.getReportPage(reportedAction, container, rOffset);
            reportPage.stream().forEach(it -> {
                if (it.containsKey("sum")) {
                    Integer sum = (Integer) it.get("sum");
                    if (sum > 0) {
                        sums.put(model, (Integer) it.get("sum"));
                    }
                    it.remove("sum");
                }
            });

            reportPage = reportPage.stream().filter(it -> {
                return !it.isEmpty();
            }).collect(Collectors.toList());

            if (!reportPage.isEmpty()) {
                multimodels.put(model, reportPage);
            }
        });
        multimodels.put("sums", sums);
        return Arrays.asList(multimodels);
    }

    @Override
    public List<String> getOptionalValues() {
//        final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectModels");
//        String sql = statRecord.toString();
//        Connection conn = connectionProvider.get();
//        List<String> returns = new JDBCQueryTemplate<String>(conn) {
//            @Override
//            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
//                String model = rs.getString("model");
//                returnsList.add(model);
//                return super.handleRow(rs, returnsList);
//            }
//        }.executeQuery(sql);
//        try {
//            LOGGER.fine(String.format("Test statistics connection.isClosed() : %b", conn.isClosed()));
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return returns;
        return new ArrayList<>();
    }

    @Override
    public String getReportId() {
        return REPORT_ID;
    }

    @Override
    public void prepareViews(ReportedAction action, StatisticsFiltersContainer container)
            throws StatisticsReportException {
        // TODO Auto-generated method stub

    }

    @Override
    public void processAccessLog(ReportedAction action, StatisticsReportSupport sup, StatisticsFiltersContainer filters)
            throws StatisticsReportException {
//        try {
//            ModelFilter modelFilter = filters.getFilter(ModelFilter.class);
//            DateFilter dateFilter = filters.getFilter(DateFilter.class);
//            VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
//            LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);
//            
//            final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup
//                    .getInstanceOf("selectModelReport");
//            
//            final StringTemplate counts = DatabaseStatisticsAccessLogImpl.stGroup
//                    .getInstanceOf("selectModelReportCounts");
//            
//            statRecord.setAttribute("model", modelFilter.getModel());
//            statRecord.setAttribute("action", action != null ? action.name() : null);
//            statRecord.setAttribute("paging", false);
//            
//            statRecord.setAttribute("fromDefined", dateFilter.getFromDate() != null);
//            statRecord.setAttribute("toDefined", dateFilter.getToDate() != null);
//            statRecord.setAttribute("visibility", visFilter.asMap());
//            
//            statRecord.setAttribute("licenseDefined", licFilter.getLicence() != null);
//            //statRecord.setAttribute("license", licFilter.getLicence());
//
//            counts.setAttribute("model", modelFilter.getModel());
//            counts.setAttribute("action", action != null ? action.name() : null);
//            counts.setAttribute("paging", false);
//            counts.setAttribute("fromDefined", dateFilter.getFromDate() != null);
//            counts.setAttribute("toDefined", dateFilter.getToDate() != null);
//            counts.setAttribute("visibility", visFilter.asMap());
//            counts.setAttribute("licenseDefined", licFilter.getLicence() != null);
//
//            @SuppressWarnings("rawtypes")
//            List params = StatisticUtils.jdbcParams(dateFilter);
//            String sql = statRecord.toString();
//            Connection conn = connectionProvider.get();
//
//            new JDBCQueryTemplate<Map<String, Object>>(conn) {
//                @Override
//                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
//                    Map<String, Object> val = new HashMap<String, Object>();
//                    val.put(COUNT_KEY, rs.getInt("count"));
//                    val.put(PID_KEY, rs.getString("pid"));
//                    val.put(TITLE_KEY, rs.getString("title"));
//                    val.put(MODEL_KEY, rs.getString("model"));
//
//
//                    sup.processReportRecord(val);
//                    returnsList.add(val);
//
//                    return super.handleRow(rs, returnsList);
//                }
//            }.executeQuery(sql,params.toArray());
//            
//            List<Map<String,Object>> sum = new JDBCQueryTemplate<Map<String, Object>>(connectionProvider.get()) {
//                @Override
//                public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
//                    Map<String, Object> val = new HashMap<>();
//                    val.put(modelFilter.getModel(), rs.getInt("sum"));
//                    returnsList.add(val);
//                    return super.handleRow(rs, returnsList);
//                }
//            }.executeQuery(counts.toString(), StatisticUtils.jdbcParams(dateFilter, licFilter,null).toArray());
//
//            LOGGER.fine(String.format("Test statistics connection.isClosed() : %b", conn.isClosed()));
//        } catch (ParseException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//            throw new StatisticsReportException(e);
//        } catch (SQLException ex) {
//            Logger.getLogger(ModelStatisticReport.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    public List<String> verifyFilters(ReportedAction action, StatisticsFiltersContainer container) {
        List<String> list = new ArrayList<>();
        DateFilter dateFilter = container.getFilter(DateFilter.class);
        VerificationUtils.dateVerification(list, dateFilter.getRawFromDate());
        VerificationUtils.dateVerification(list, dateFilter.getRawToDate());
        return list;
    }

}
