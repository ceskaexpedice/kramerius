package cz.incad.kramerius.statistics.impl;

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
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.Offset;

/**
 *  Agregovany report -> Modely a pocty 
 * @author happy
 *
 */
public class ModelSummaryReport implements StatisticReport {

    private static final List<String> SUPPORTED_MODELS = Arrays.asList("monograph", "periodical", "article", "convolute", "map", "graphic", "archive",
            "manuscript", "soundrecording", "collection");

    public static final Logger LOGGER = Logger.getLogger(ModelSummaryReport.class.getName());

    public static final String REPORT_ID = "summary";

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
                    new StatisticsFilter[] { dateFilter, visFilter, licFilter, modelFilter,idFilter });

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
        });
        
        multimodels.put("sums", sums);
        return Arrays.asList(multimodels);
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
        
        
        MultimodelFilter mfilter = new MultimodelFilter();
        mfilter.setModels(SUPPORTED_MODELS);

//        Map<String, Object> multimodels = new HashMap<>();
//        Map<String, Object> sums = new HashMap<>();

        mfilter.getModels().stream().forEach(model -> {
            ModelFilter modelFilter = new ModelFilter();
            modelFilter.setModel(model);

            DateFilter dateFilter = filters.getFilter(DateFilter.class);
            VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
            LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);
            IdentifiersFilter idFilter = filters.getFilter(IdentifiersFilter.class);

            StatisticsFiltersContainer container = new StatisticsFiltersContainer(
                    new StatisticsFilter[] { dateFilter, visFilter, licFilter, modelFilter,idFilter });

            ModelStatisticReport report = new ModelStatisticReport();
            report.solrAccess = this.solrAccess;
            
            List<Map<String, Object>> reportPage = report.getReportPage(action, container, null);
            reportPage.stream().forEach(it -> {
                if (it.containsKey("sum")) {
                    Integer sum = (Integer) it.get("sum");
                    if (sum > 0) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put(COUNT_KEY, (Integer) it.get("sum"));
                        map.put(MODEL_KEY, model);
                        sup.processReportRecord(map);
                    }
                }
            });
        });
    }

    
    
    @Override
    public boolean convertToObject() {
        return true;
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
