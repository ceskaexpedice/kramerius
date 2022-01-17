package cz.kramerius.searchIndex.indexer;

public class SolrConfig {
    public final String baseUrl;
    public final String collection;
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

}
