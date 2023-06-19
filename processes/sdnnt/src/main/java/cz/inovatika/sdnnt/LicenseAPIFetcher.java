package cz.inovatika.sdnnt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public abstract class LicenseAPIFetcher {
    
    public static final String FETCHER_LICENSES_KEY = "licenses";
    public static final String FETCHER_DATE_KEY = "date";
    public static final String FETCHER_MODEL_KEY = "model";
    public static final String FETCHER_TITLES_KEY = "titles";

    public static final int BATCH_SIZE = 90;
    public static final int MAX_FETCHED_DOCS = 90;
    
    private String apiVersion;
    private String baseUrl;
    
    private boolean privateFilter = true;
    
    public LicenseAPIFetcher(String baseUrl,String apiVersion, boolean privateFilter) {
        super();
        this.apiVersion = apiVersion;
        this.baseUrl = baseUrl;
        this.privateFilter = privateFilter;
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public String getApiUrl() {
        return baseUrl;
    }
    
    public boolean isPrivateFilter() {
        return privateFilter;
    }
    
    public abstract  Map<String, Map<String, Object>> check(Set<String> pids) throws IOException;

    public static enum Versions {
        
        v5 {
            @Override
            public LicenseAPIFetcher build(String apiUrl, String apiVersion, boolean privateFilter) {
                return new V5APILicenseFetcher(apiUrl,apiVersion, privateFilter);
            }
            
        },
        
        v7 {
            @Override
            public LicenseAPIFetcher build(String apiUrl, String apiVersion, boolean privateFilter) {
                return new V7APILicenseFetcher(apiUrl,apiVersion, privateFilter);
            }
            
        };
        
        public abstract LicenseAPIFetcher build(String apiUrl,String apiVersion, boolean privateFilter);
    }
    
}
