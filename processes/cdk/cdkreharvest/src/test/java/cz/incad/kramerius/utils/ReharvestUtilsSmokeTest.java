package cz.incad.kramerius.utils;

import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReharvestUtilsSmokeTest {

    @Test
    public void findPidByTypeAgainstLocalSolrCloudReturnsExpectedPids() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("runSmokeTests"));

        String solrUrl = smokeSolrUrl();
        Assume.assumeTrue(solrUrl != null && !solrUrl.isBlank());

        ReharvestItem item = ReharvestItem.fromJSON(new JSONObject(ITestsConstants.MONOGRAPH_DELETE_TREE));
        Map<String, String> iteration = new HashMap<>();
        iteration.put("url", solrUrl);
        iteration.put("type", "CURSOR");
        iteration.put("rows", "4000");

        try (CloseableHttpClient client = ITestsConstants.buildApacheClient()) {
            List<Pair<String, String>> pairs = ReharvestUtils.findPidByType(iteration, client, item, 4000);
            Assert.assertFalse(pairs.isEmpty());
            Assert.assertTrue(pairs.stream().anyMatch(pair ->
                    "uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f".equals(pair.getLeft())));
            Assert.assertTrue(pairs.stream().anyMatch(pair ->
                    "uuid:001a52c0-8591-11ec-b93a-5ef3fc9bb22f".equals(pair.getLeft())));
            Assert.assertEquals(3276, pairs.size());
        }
    }

    private static String smokeSolrUrl() {
        String fromProperty = System.getProperty("smoke.solr.url");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String fromEnv = System.getenv("SMOKE_SOLR_URL");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return ITestsConstants.URL_LOCAL_SOLR;
    }
}
