package cz.incad.kramerius.services.utils.kubernetes;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class KubernetesEnvSupportTest {

    @Test
    public void prefixVariablesCopiesOnlyMatchingPrefixAndLowercasesNames() {
        Map<String, String> env = new HashMap<>();
        env.put("ITERATION_URL", "https://iteration.example");
        env.put("ITERATION_API_KEY", "secret");
        env.put("CHECK_URL", "https://check.example");

        Map<String, String> values = new HashMap<>();
        KubernetesEnvSupport.prefixVariables(env, values, KubernetesEnvSupport.ITERATION_PREFIX);

        Assert.assertEquals(2, values.size());
        Assert.assertEquals("https://iteration.example", values.get("url"));
        Assert.assertEquals("secret", values.get("api_key"));
    }

    @Test
    public void iterationMapUsesDedicatedUrlAndApiKeyNames() {
        Map<String, String> env = new HashMap<>();
        env.put(KubernetesEnvSupport.ITERATION_URL, "https://iteration.example");
        env.put(KubernetesEnvSupport.ITERATION_API_KEY, "secret");
        env.put("ITERATION_BATCH_SIZE", "100");

        Map<String, String> values = KubernetesEnvSupport.iterationMap(env);

        Assert.assertEquals("https://iteration.example", values.get("url"));
        Assert.assertEquals("secret", values.get("apikey"));
        Assert.assertEquals("100", values.get("batch_size"));
    }

    @Test
    public void destinationCheckProxyAndReharvestMapsUseExplicitUrlVariables() {
        Map<String, String> env = new HashMap<>();
        env.put(KubernetesEnvSupport.DESTINATION_URL, "https://dest.example");
        env.put(KubernetesEnvSupport.CHECK_URL, "https://check.example");
        env.put(KubernetesEnvSupport.PROXY_API_URL, "https://proxy.example");
        env.put(KubernetesEnvSupport.REHARVEST_URL, "https://reharvest.example");

        Assert.assertEquals("https://dest.example", KubernetesEnvSupport.destinationMap(env).get("url"));
        Assert.assertEquals("https://check.example", KubernetesEnvSupport.checkMap(env).get("url"));
        Assert.assertEquals("https://proxy.example", KubernetesEnvSupport.proxyMap(env).get("url"));
        Assert.assertEquals("https://reharvest.example", KubernetesEnvSupport.reharvestMap(env).get("url"));
    }

    @Test
    public void comparingMapKeepsOnlyComparingPrefixedValues() {
        Map<String, String> env = new HashMap<>();
        env.put("COMPARING_ROWS", "500");
        env.put("ITERATION_ROWS", "100");

        Map<String, String> values = KubernetesEnvSupport.comparingMap(env);

        Assert.assertEquals(2, values.size());
        Assert.assertEquals("500", values.get("rows"));
        Assert.assertEquals("pid", values.get("identifier"));
    }

    @Test
    public void feederMapDefaultsBatchSizeTo45() {
        Map<String, String> values = KubernetesEnvSupport.feederMap(new HashMap<>());

        Assert.assertEquals("45", values.get("batchsize"));
    }

    @Test
    public void feederMapUsesBatchSizeFromEnv() {
        Map<String, String> env = new HashMap<>();
        env.put(KubernetesEnvSupport.FEEDER_BATCH_SIZE, "80");

        Map<String, String> values = KubernetesEnvSupport.feederMap(env);

        Assert.assertEquals("80", values.get("batchsize"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void feederMapRejectsBatchSizeLowerThan10() {
        Map<String, String> env = new HashMap<>();
        env.put(KubernetesEnvSupport.FEEDER_BATCH_SIZE, "9");

        KubernetesEnvSupport.feederMap(env);
    }

    @Test
    public void timestampMapUsesTimestampUrlAndIterationSupportsBothApiKeyNames() {
        Map<String, String> env = new HashMap<>();
        env.put(KubernetesEnvSupport.TMSP_URL, "https://timestamp.example");
        env.put(KubernetesEnvSupport.ITERATION_APIKEY, "legacy-secret");

        Assert.assertEquals("https://timestamp.example", KubernetesEnvSupport.timestampMap(env, new HashMap<>()).get("url"));
        Assert.assertEquals("legacy-secret", KubernetesEnvSupport.iterationMap(env).get("apikey"));
    }
}
