package cz.incad.kramerius.statistics.filters;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class IPAddressFilter implements StatisticsFilter {

    public static final String KEY = "statistics.ipaddress";
    
    
    public boolean hasValue() {
        String value = KConfiguration.getInstance().getConfiguration().getString(KEY,"");
        return !value.trim().equals("");
    }
    
    
    public String getValue() {
        String value = KConfiguration.getInstance().getConfiguration().getString(KEY,"");
        return value;
    }
    
}
