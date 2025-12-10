package cz.kramerius.searchIndex.indexer;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class SolrConfig {
    public final String baseUrl; //example: localhost:8983/solr
    public final String collection; //example: search
    public final Boolean useHttps;
    public final String login;
    public final String password;

    public SolrConfig(String baseUrl, String collection, Boolean useHttps, String login, String password) {
        this.baseUrl = baseUrl;
        this.collection = collection;
        this.useHttps = useHttps;
        this.login = login;
        this.password = password;
    }

    public SolrConfig() {
        KConfiguration config = KConfiguration.getInstance();
        String host = config.getSolrSearchHost();
        if (host.startsWith("https://")) {
            useHttps = true;
            String[] baseUrlAndCollection = splitToBaseUrlAndCollection(host.substring("https://".length()));
            baseUrl = baseUrlAndCollection[0];
            collection = baseUrlAndCollection[1];
        } else if (host.startsWith("http://")) {
            useHttps = false;
            String[] baseUrlAndCollection = splitToBaseUrlAndCollection(host.substring("http://".length()));
            baseUrl = baseUrlAndCollection[0];
            collection = baseUrlAndCollection[1];
        } else {
            throw new RuntimeException("invalid solr search host: " + host);
        }
        login = config.getSolrSearchLogin();
        password = config.getSolrSearchPassword();
    }

    /**
     * @param urlWithoutProtocol localhost:8983/solr/search or localhost:8983/solr/search/
     * @return
     */
    private String[] splitToBaseUrlAndCollection(String urlWithoutProtocol) {
        urlWithoutProtocol = urlWithoutProtocol.endsWith("/") ? urlWithoutProtocol.substring(0, urlWithoutProtocol.length() - 1) : urlWithoutProtocol;
        int lastSlashPosition = urlWithoutProtocol.lastIndexOf("/");
        return new String[]{urlWithoutProtocol.substring(0, lastSlashPosition), urlWithoutProtocol.substring(lastSlashPosition)};
    }

    @Override
    public String toString() {
        return "SolrConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", collection='" + collection + '\'' +
                ", useHttps=" + useHttps +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
