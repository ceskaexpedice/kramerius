package cz.incad.kramerius.services.utils.kubernetes;

import java.util.HashMap;
import java.util.Map;

public class KubernetesEnvSupport {

    public static final String TMSP_URL = "TIMESTAMP_URL";
    public static final String ITERATION_URL = "ITERATION_URL";
    public static final String ITERATION_API_KEY = "ITERATION_API_KEY";
    public static final String ITERATION_APIKEY = "ITERATION_APIKEY";
    public static final String CHECK_URL = "CHECK_URL";
    public static final String DESTINATION_URL = "DESTINATION_URL";
    public static final String CONFIG_SOURCE = "CONFIG_SOURCE";
    public static final String ITERATION_PREFIX = "ITERATION_";
    public static final String CHECK_PREFIX = "CHECK_";
    public static final String DEST_PREFIX = "DESTINATION_";
    
    public static final String PROXY_API_URL = "PROXY_API_URL";
    public static final String REHARVEST_URL = "REHARVEST_URL";
    
    
    public static final String TMSP_PREFIX = "TIMESTAMP_";
    public static final String PROXY_API_PREFIX = "PROXY_API_";
    public static final String REHARVEST_PREFIX = "REHARVEST_";

    public static final String COMPARING_PREFIX = "COMPARING_";
    public static final String FEEDER_PREFIX = "FEEDER_";
    public static final String FEEDER_BATCH_SIZE = "FEEDER_BATCH_SIZE";
    public static final String FEEDER_BATCHSIZE = "FEEDER_BATCHSIZE";
    public static final String DEFAULT_FEEDER_BATCH_SIZE = "45";
    public static final int MIN_FEEDER_BATCH_SIZE = 10;
    
    
    public static void prefixVariables(Map<String, String> env, Map<String, String> templateMap, String prefix) {
        env.keySet().forEach(key -> {
            if (key.startsWith(prefix)) {
                String name = key.substring(prefix.length()).toLowerCase();
                templateMap.put(name, env.get(key));
            }
        });
    }

    public static Map<String, String> timestampMap(Map<String, String> env, Map<String, String> iteration) {
        Map<String, String> timestamps = new HashMap<>();
        prefixVariables(env, timestamps, TMSP_PREFIX);
        if (env.containsKey(TMSP_URL)) {
            timestamps.put("url", normalizeTimestampUrl(env.get(TMSP_URL), iteration.get("dl")));
        }
        return timestamps;
    }

    public static String normalizeTimestampUrl(String timestampUrl, String sourceName) {
        if (timestampUrl == null || timestampUrl.trim().isEmpty()) {
            return timestampUrl;
        }
        String normalized = timestampUrl.trim();
        if ((normalized.endsWith("connected") || normalized.endsWith("connected/"))
                && sourceName != null && !sourceName.trim().isEmpty()) {
            return normalized + (normalized.endsWith("/") ? "" : "/") + sourceName.trim() + "/timestamp";
        }
        return normalized;
    }

    public static Map<String, String> destinationMap(Map<String, String> env) {
        Map<String, String> destination = new HashMap<>();
        prefixVariables(env, destination, DEST_PREFIX);
        if (env.containsKey(DESTINATION_URL)) {
            destination.put("url", env.get(DESTINATION_URL));
        }
        return destination;
    }

    public static Map<String, String> checkMap(Map<String, String> env) {
        Map<String, String> check = new HashMap<>();
        prefixVariables(env, check, CHECK_PREFIX);
        if (env.containsKey(CHECK_URL)) {
            check.put("url", env.get(CHECK_URL));
        }
        return check;
    }

    public static Map<String, String> iterationMap(Map<String, String> env) {
        Map<String, String> iteration = new HashMap<>();
        prefixVariables(env, iteration, ITERATION_PREFIX);
        if (env.containsKey(ITERATION_URL)) {
            iteration.put("url", env.get(ITERATION_URL));
        }
        if (env.containsKey(ITERATION_API_KEY)) {
            iteration.put("apikey", env.get(ITERATION_API_KEY));
        }
        if (env.containsKey(ITERATION_APIKEY)) {
            iteration.put("apikey", env.get(ITERATION_APIKEY));
        }

        return iteration;
    }

    public static Map<String, String> comparingMap(Map<String, String> env) {
        Map<String, String> comparing = new HashMap<>();
        prefixVariables(env, comparing, COMPARING_PREFIX);
        comparing.putIfAbsent("identifier", "pid");
        return comparing;
    }

    public static Map<String, String> feederMap(Map<String, String> env) {
        Map<String, String> feeder = new HashMap<>();
        prefixVariables(env, feeder, FEEDER_PREFIX);
        String batchSize = feeder.getOrDefault("batch_size", feeder.getOrDefault("batchsize", DEFAULT_FEEDER_BATCH_SIZE));
        feeder.put("batchsize", validateFeederBatchSize(batchSize));
        return feeder;
    }

    private static String validateFeederBatchSize(String value) {
        try {
            int batchSize = Integer.parseInt(value);
            if (batchSize < MIN_FEEDER_BATCH_SIZE) {
                throw new IllegalArgumentException(String.format(
                        "%s must be at least %d", FEEDER_BATCH_SIZE, MIN_FEEDER_BATCH_SIZE));
            }
            return String.valueOf(batchSize);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("%s must be a number", FEEDER_BATCH_SIZE), e);
        }
    }

    public static Map<String, String> proxyMap(Map<String, String> env) {
        Map<String, String> proxy = new HashMap<>();
        prefixVariables(env, proxy, PROXY_API_PREFIX);
        if (env.containsKey(PROXY_API_URL)) {
            proxy.put("url", env.get(PROXY_API_URL));
        }
        return proxy;
    }
    public static Map<String, String> reharvestMap(Map<String, String> env) {
        Map<String, String> proxy = new HashMap<>();
        prefixVariables(env, proxy, REHARVEST_PREFIX);
        if (env.containsKey(REHARVEST_URL)) {
            proxy.put("url", env.get(REHARVEST_URL));
        }
        return proxy;
    }

}
