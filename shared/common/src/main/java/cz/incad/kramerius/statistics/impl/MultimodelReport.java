package cz.incad.kramerius.statistics.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class MultimodelReport implements StatisticReport {
    public static final Logger LOGGER = Logger.getLogger(MultimodelReport.class.getName());

    public static final String REPORT_ID = "multimodel";

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

			reportPage = reportPage.stream().filter(it -> {return !it.isEmpty();}).collect(Collectors.toList());
			
			if (!reportPage.isEmpty()) {
				multimodels.put(model, reportPage);
			}
			//reportPage.stream().forEach(retvals::add);
		});
		
		multimodels.put("sums", sums);
		
		return Arrays.asList(multimodels);
	}

	@Override
	public List<String> getOptionalValues() {
        final StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectModels");
        String sql = statRecord.toString();
        Connection conn = connectionProvider.get();
        List<String> returns = new JDBCQueryTemplate<String>(conn) {
            @Override
            public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                String model = rs.getString("model");
                returnsList.add(model);
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(sql);
        try {
            LOGGER.fine(String.format("Test statistics connection.isClosed() : %b", conn.isClosed()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returns;
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
			StatisticsFiltersContainer container) throws StatisticsReportException {
		// TODO Auto-generated method stub
		
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
