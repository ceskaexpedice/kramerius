package cz.incad.kramerius.statistics.filters;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class IPAddressFilter implements StatisticsFilter {

    public static final String KEY = "statistics.ipaddress";
    
    private String ipAddress;
    
    public boolean hasValue() {
        String value = KConfiguration.getInstance().getConfiguration().getString(KEY,"");
        return !value.trim().equals("");
    }
   
    
    public String getValue() {
        String value = KConfiguration.getInstance().getConfiguration().getString(KEY,"");
        return value;
    }
    
    /*
    public String setValue() {
        String value = KConfiguration.getInstance().getConfiguration().getString(KEY,"");
        return value;
    }
   */
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
