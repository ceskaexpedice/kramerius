package cz.incad.kramerius.statistics.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.StatisticsReportSupport;
import cz.incad.kramerius.statistics.filters.*;
import cz.incad.kramerius.utils.database.Offset;

import java.sql.Connection;
import java.util.*;
import java.util.logging.Logger;

//TODO: Vyhodit
public class AnnualStatisticsReport extends AbstractStatisticsReport implements StatisticReport {



    public static final Logger LOGGER = Logger.getLogger(AnnualStatisticsReport.class.getName());

    public static final String REPORT_ID = "annual";

    private Provider<Connection> connectionProvider;

    @Inject
    public AnnualStatisticsReport(@Named("kramerius4") Provider<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<Map<String, Object>> getReportPage(ReportedAction reportedAction, StatisticsFiltersContainer filters, Offset rOffset) throws StatisticsReportException {
        throw new UnsupportedOperationException("unsupported");
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
    public void processAccessLog(ReportedAction action, StatisticsReportSupport sup, StatisticsFiltersContainer container) throws StatisticsReportException {
//        MultimodelFilter multimodel = container.getFilter(MultimodelFilter.class);
//        List<String> models = multimodel.getModels();
//        for (String model : models) {
//            ModelFilter modelFilter = new ModelFilter();
//            modelFilter.setModel(model);
//            StatisticsFilter[] filters = new StatisticsFilter[]{
//                    modelFilter,
//                    getDateFilter(container.getFilter(AnnualYearFilter.class)),
//                    container.getFilter(IPAddressFilter.class),
//                    container.getFilter(VisibilityFilter.class),
//                    container.getFilter(UniqueIPAddressesFilter.class)
//            };
//            ModelStatisticReport report = new ModelStatisticReport();
//            report.connectionProvider = connectionProvider;
//            report.processAccessLog(action, sup, new StatisticsFiltersContainer(filters));
//        }
    }

    public static StatisticsFilter getDateFilter(AnnualYearFilter afilter) {

        DateFilter filter = new DateFilter();
        Calendar start = Calendar.getInstance();
        start.set(Calendar.YEAR, Integer.parseInt(afilter.getAnnualYear()));
        start.set(Calendar.MONTH, Calendar.JANUARY);
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        filter.setFromDate(TIMESTAMP_FORMAT.format(start.getTime()));


        Calendar stop = Calendar.getInstance();
        stop.set(Calendar.YEAR, Integer.parseInt(afilter.getAnnualYear()));
        stop.set(Calendar.MONTH, Calendar.DECEMBER);
        stop.set(Calendar.DAY_OF_MONTH, 31);
        stop.set(Calendar.HOUR, 23);
        stop.set(Calendar.MINUTE, 59);
        stop.set(Calendar.SECOND, 59);
        stop.set(Calendar.MILLISECOND, 999);

        filter.setToDate(TIMESTAMP_FORMAT.format(stop.getTime()));

        return filter;
    }

	@Override
	public List<String> verifyFilters(ReportedAction action, StatisticsFiltersContainer container) {
    	List<String> list = new ArrayList<>();
    	AnnualYearFilter modelFilter = container.getFilter(AnnualYearFilter.class);
        if (modelFilter.getAnnualYear() == null)  {
        	list.add("annualYeer is mandatory");
        }
		return list;
	}
}
