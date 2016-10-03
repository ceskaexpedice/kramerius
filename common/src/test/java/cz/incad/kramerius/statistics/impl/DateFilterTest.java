package cz.incad.kramerius.statistics.impl;

import org.antlr.stringtemplate.StringTemplate;
import org.junit.Test;

public class DateFilterTest {
    @Test
    public void testTemplate() {
    	StringTemplate dateFilter = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("dateFilter");
        dateFilter.setAttribute("fromDefined", false);
        dateFilter.setAttribute("toDefined", false);
        System.out.println(dateFilter.toString());

    	dateFilter = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("dateFilter");
        dateFilter.setAttribute("fromDefined", true);
        dateFilter.setAttribute("toDefined", true);
        System.out.println(dateFilter.toString());

    	dateFilter = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("dateFilter");
        dateFilter.setAttribute("fromDefined", false);
        dateFilter.setAttribute("toDefined", true);
        System.out.println(dateFilter.toString());


    	dateFilter = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("dateFilter");
        dateFilter.setAttribute("fromDefined", true);
        dateFilter.setAttribute("toDefined", false);
        System.out.println(dateFilter.toString());

    }
}
