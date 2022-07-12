package cz.incad.kramerius.statistics.impl;

import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.IdentifiersFilter;
import cz.incad.kramerius.statistics.filters.LicenseFilter;
import cz.incad.kramerius.statistics.filters.ModelFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.utils.ReportUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public abstract class AbstractStatisticsReport implements StatisticReport {

    protected String logsEndpoint() {
        String loggerPoint = KConfiguration.getInstance().getProperty("k7.log.solr.point","http://localhost:8983/solr/logs");
        String selectEndpoint = loggerPoint + (loggerPoint.endsWith("/") ? "" : "/" ) +"";
        return selectEndpoint;
    }

    protected void applyFilters(StatisticsFiltersContainer filters, StringBuilder builder) {
        DateFilter dateFilter = filters.getFilter(DateFilter.class);
        LicenseFilter licFilter = filters.getFilter(LicenseFilter.class);
        IdentifiersFilter idFilter = filters.getFilter(IdentifiersFilter.class);
        ModelFilter modelFilter = filters.getFilter(ModelFilter.class);
        
        //StringBuilder builder = new StringBuilder("q=*");
        ReportUtils.enhanceLicense(builder, licFilter);
        ReportUtils.enhanceDateFilter(builder, dateFilter);
        ReportUtils.enhanceIdentifiers(builder, idFilter);
        ReportUtils.enhanceModelFilter(builder, modelFilter);
        
        //return builder;
    }

    @Override
    public boolean convertToObject() {
        return false;
    }

    
}
