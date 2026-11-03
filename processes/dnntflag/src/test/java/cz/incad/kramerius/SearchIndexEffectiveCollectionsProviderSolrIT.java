package cz.incad.kramerius;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.json.JSONObject;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Integration check against a locally running Solr search_vc3 core. */
@Ignore
public class SearchIndexEffectiveCollectionsProviderSolrIT {
    private static final String SOLR_SELECT_URL = "http://localhost:8983/solr/search_vc3/select";
    private static final String TARGET_PID = "uuid:7164a8b0-31d4-4412-b2a5-8bd504d3c367";

    @Test
    public void readsEffectiveCollectionsFromLocalSearchV3() throws Exception {
        java.util.Set<String> items = new SearchIndexEffectiveCollectionsProvider(localSolrAccess())
                .getAffectedCollections(TARGET_PID);

        Assert.assertFalse("Expected collection memberships for " + TARGET_PID, items.isEmpty());
    }

    private SolrAccess localSolrAccess() {
        return (SolrAccess) java.lang.reflect.Proxy.newProxyInstance(
                SolrAccess.class.getClassLoader(), new Class<?>[]{SolrAccess.class}, (proxy, method, args) -> {
                    if ("getSolrDataByPid".equals(method.getName()) && args.length == 2) {
                        String pidQuery = "pid:\"" + ((String) args[0]).replace(":", "\\:") + "\"";
                        String query = "q=" + encode(pidQuery) + "&fl=" + encode((String) args[1]);
                        return XMLUtils.parseDocument(new ByteArrayInputStream(
                                request(query, "xml").getBytes(StandardCharsets.UTF_8)));
                    }
                    if ("requestWithSelectReturningJson".equals(method.getName()) && args.length == 2) {
                        return new JSONObject(request((String) args[0], "json"));
                    }
                    throw new UnsupportedOperationException("Unsupported SolrAccess method: " + method.getName());
                });
    }

    private String request(String query, String responseType) throws IOException {
        URL url = new URL(SOLR_SELECT_URL + "?" + query + "&wt=" + responseType);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(30000);
        connection.setRequestMethod("GET");
        try {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Solr returned HTTP " + status + " for " + url);
            }
            try (java.util.Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())
                    .useDelimiter("\\A")) {
                return scanner.hasNext() ? scanner.next() : "";
            }
        } finally {
            connection.disconnect();
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
