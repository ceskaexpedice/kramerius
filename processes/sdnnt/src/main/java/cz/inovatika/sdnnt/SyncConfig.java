package cz.inovatika.sdnnt;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class SyncConfig {
    
    public static final String DEFAULT_VERSION = "v7";
    
    private String sdnntEndpoint;
    
    private String baseUrl;
    private String version;
    private String acronym;
    
    private String syncCollection;
    private String syncSolrHost;
    
    public SyncConfig(String baseUrl, String version, String acronym,  String syncSolrHost, String syncCollection) {
        super();
        this.baseUrl = baseUrl;
        this.version = version;
        this.acronym = acronym;
        this.syncCollection = syncCollection;
        this.syncSolrHost = syncSolrHost;
    }
    
    public SyncConfig() {
        String sdnntHost  = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
        
        String[] splitted = sdnntHost.split("/");
        String collection = splitted.length > 0 ? splitted[splitted.length -1] : null;
        if (collection != null) {
            int index = sdnntHost.indexOf(collection);
            if (index > -1) { sdnntHost = sdnntHost.substring(0, index); }
        }
        
        this.syncSolrHost = sdnntHost;
        this.syncCollection = collection;
        
        if (sdnntHost == null) {
            throw new IllegalStateException("Missing configuration key 'solrSdnntHost'");
        }

        
        this.version = KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.version","v7");
        

        String localApi = KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.local.api", KConfiguration.getInstance().getConfiguration().getString("api.point"));
        this.baseUrl = localApi;

        this.acronym  = KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.acronym");
        if (acronym == null) {
            throw new IllegalStateException("Missing configuration key 'acronym'");
        }
        
        this.sdnntEndpoint = KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.endpoint","https://sdnnt.nkp.cz/sdnnt/api/v1.0/lists/changes");
        if (acronym == null) {
            throw new IllegalStateException("Missing configuration key 'sdnnt.check.endpoint'");
        }
    }
    
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getAcronym() {
        return acronym;
    }
    
    public String getSyncCollection() {
        return syncCollection;
    }
    
    public String getSyncSolrHost() {
        return syncSolrHost;
    }
    
    public String getSdnntEndpoint() {
        return sdnntEndpoint;
    }
}
