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

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.statistics.accesslogs.database.DatabaseStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.LicenseFilter;
import cz.incad.kramerius.statistics.filters.ModelFilter;
import cz.incad.kramerius.statistics.filters.MultimodelFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.filters.VisibilityFilter;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.Offset;

public class ModelSummaryReport implements StatisticReport {
	
	public static final Logger LOGGER = Logger.getLogger(ModelSummaryReport.class.getName());
	
    public static final String REPORT_ID = "summary";

	@Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;
	@Override
	public List<Map<String, Object>> getReportPage(ReportedAction reportedAction, StatisticsFiltersContainer filters,
			Offset rOffset) throws StatisticsReportException {
		MultimodelFilter mfilter= new MultimodelFilter();
		mfilter.setModels(Arrays.asList("monograph",
									"periodical", 
									"article",
									"convolute", 
									"map",
									"graphic",
									"archive",
									"manuscript",
									"soundrecording",
									"collection"
		));
	
		Map<String, Object> multimodels = new HashMap<>();
		Map<String, Object> sums = new HashMap<>();
		
		mfilter.getModels().stream().forEach(model-> {
			ModelFilter modelFilter = new ModelFilter();
			modelFilter.setModel(model);
			
			DateFilter dateFilter = filters.getFilter(DateFilter.class);
            VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
            LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);
        
            StatisticsFiltersContainer container = new StatisticsFiltersContainer(new  StatisticsFilter[] {dateFilter, visFilter, licFilter, modelFilter});

			ModelStatisticReport report = new ModelStatisticReport();
			report.connectionProvider = connectionProvider;

			List<Map<String,Object>> reportPage = report.getReportPage(reportedAction, container, rOffset);
			reportPage.stream().forEach(it-> {
				if(it.containsKey("sum")) {
					Integer sum = (Integer) it.get("sum");
					if (sum > 0 ) {
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
	public List<String> getOptionalValues() {
		// TODO Auto-generated method stub
		return null;
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
	public void processAccessLog(ReportedAction action, StatisticsReportSupport sup,
			StatisticsFiltersContainer filters) throws StatisticsReportException {
        try {
        	
    		MultimodelFilter mfilter= new MultimodelFilter();
    		mfilter.setModels(Arrays.asList("monograph",
    									"periodical", 
    									"article",
    									"convolute", 
    									"map",
    									"graphic",
    									"archive",
    									"manuscript",
    									"soundrecording",
    									"collection"
    		));
    	
    		
    		mfilter.getModels().stream().forEach(model-> {
    			ModelFilter modelFilter = new ModelFilter();
    			modelFilter.setModel(model);
    			
    			DateFilter dateFilter = filters.getFilter(DateFilter.class);
                VisibilityFilter visFilter = filters.getFilter(VisibilityFilter.class);
                LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);

                
                
                final StringTemplate counts = DatabaseStatisticsAccessLogImpl.stGroup
                        .getInstanceOf("selectModelReportCounts");

                counts.setAttribute("model", modelFilter.getModel());
                counts.setAttribute("action", action != null ? action.name() : null);
                counts.setAttribute("paging", false);
                counts.setAttribute("fromDefined", dateFilter.getFromDate() != null);
                counts.setAttribute("toDefined", dateFilter.getToDate() != null);
                counts.setAttribute("visibility", visFilter.asMap());
                counts.setAttribute("licenseDefined", licFilter.getLicence() != null);

                
                try {
					new JDBCQueryTemplate<Map<String, Object>>(connectionProvider.get()) {
					    @Override
					    public boolean handleRow(ResultSet rs, List<Map<String, Object>> returnsList) throws SQLException {
					        Map<String, Object> val = new HashMap<>();
					        val.put(modelFilter.getModel(), rs.getInt("sum"));

					        sup.processReportRecord(val);
					        
					        //returnsList.add(val);
					        return super.handleRow(rs, returnsList);
					    }
					}.executeQuery(counts.toString(), StatisticUtils.jdbcParams(dateFilter, licFilter,null).toArray());
				} catch (ParseException e) {
		            LOGGER.log(Level.SEVERE, e.getMessage(), e);
				}
    		});
        	
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new StatisticsReportException(e);
        }
		
	}

	@Override
	public List<String> verifyFilters(ReportedAction action, StatisticsFiltersContainer container) {
    	List<String> list = new ArrayList<>();
		DateFilter dateFilter = container.getFilter(DateFilter.class);
		VerificationUtils.dateVerification(list, dateFilter.getFromDate());
		VerificationUtils.dateVerification(list, dateFilter.getToDate());
        return list;
	}

}
