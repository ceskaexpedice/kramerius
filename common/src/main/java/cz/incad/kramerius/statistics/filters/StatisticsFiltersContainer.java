package cz.incad.kramerius.statistics.filters;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;


public class StatisticsFiltersContainer {
    
    List<StatisticsFilter> filters;
    
    @Inject
    public StatisticsFiltersContainer(StatisticsFilter[] filters) {
        super();
        this.filters = Arrays.asList(filters);
    }


    @SuppressWarnings("unchecked")
    public <T extends StatisticsFilter> T getFilter(Class<T> clz) {
        for (StatisticsFilter f : filters) {
            if (f.getClass().equals(clz)) {
                return (T) f;
            }
        }
        return null;
    }
    
}
