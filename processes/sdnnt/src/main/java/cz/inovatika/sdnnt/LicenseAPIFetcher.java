package cz.inovatika.sdnnt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public abstract class LicenseAPIFetcher {
    
    public static final int BATCH_SIZE = 90;
    public static final int MAX_FETCHED_DOCS = 90;
    
    private String apiVersion;
    private String baseUrl;
    
    public LicenseAPIFetcher(String baseUrl,String apiVersion) {
        super();
        this.apiVersion = apiVersion;
        this.baseUrl = baseUrl;
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public String getApiUrl() {
        return baseUrl;
    }
    
    
    public abstract  Map<String, List<String>> check(Set<String> pids) throws IOException;

    public static enum Versions {
        
        v5 {

            @Override
            public LicenseAPIFetcher build(String apiUrl, String apiVersion) {
                return new V5APILicenseFetcher(apiUrl,apiVersion);
            }
            
        },
        
        v7 {

            @Override
            public LicenseAPIFetcher build(String apiUrl, String apiVersion) {
                return new V5APILicenseFetcher(apiUrl,apiVersion);
            }
            
        };
        
        public abstract LicenseAPIFetcher build(String apiUrl,String apiVersion);
    }
    
}
