package cz.inovatika.sdnnt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.utils.RESTHelper;

public class V5APILicenseFetcher extends LicenseAPIFetcher {

    public static final Logger LOGGER = Logger.getLogger(V5APILicenseFetcher.class.getName());
    
    public V5APILicenseFetcher(String apiUrl, String apiVersion, boolean privateFilter) {
        super(apiUrl, apiVersion, privateFilter);
    }

    @Override
    public Map<String, Map<String, Object>> check(Set<String> pids) throws IOException{
        Map<String, Map<String, Object>> result = new HashMap<>();

        String apiUrl = getApiUrl();
        List<String> processingPids = new ArrayList<>(pids);
        int numberOfIterations = processingPids.size() / BATCH_SIZE;
        numberOfIterations =  (processingPids.size() % BATCH_SIZE == 0) ? numberOfIterations : numberOfIterations +1;
        LOGGER.info("Number of iterations:"+numberOfIterations);
        long checkStart = System.currentTimeMillis();
        for (int i = 0; i < numberOfIterations; i++) {
            int start = i* BATCH_SIZE;
            int end = Math.min((i+1)*BATCH_SIZE, processingPids.size());
            List<String> batchPids = processingPids.subList(start, end);

            if (i%15 == 0) {
                LOGGER.info( String.format("Current iteration is: %d, time: %d ", i, (System.currentTimeMillis() - checkStart)));
            }

            String condition = batchPids.stream().map(p -> {
                return p.replace(":", "\\:");
            }).collect(Collectors.joining(" OR "));

            if (!apiUrl.endsWith("/")) {
                apiUrl = apiUrl + "/";
            }
            
            String encodedCondition = URLEncoder.encode(
                    "PID:(" + condition + ")", "UTF-8");
            
            String encodedFieldList = URLEncoder.encode("PID dnnt-labels datum_str fedora.model dc.title", "UTF-8");
            String url = apiUrl + "search?q=" + encodedCondition + "&wt=json&rows=" + MAX_FETCHED_DOCS
                    + "&fl=" + encodedFieldList;
            //"&fq=" + filter;
            if (this.isPrivateFilter()) {
                String filter = "&dostupnost:private";
                url = url + filter;
            }

            InputStream is = RESTHelper.inputStream(url, null, null);
            String string = IOUtils.toString(is, Charset.forName("UTF-8"));
            //System.out.println(string);
            JSONObject obj = new JSONObject(string);
            JSONObject response = obj.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");
            for (int j = 0; j < docs.length(); j++) {
                JSONObject oneItem = docs.getJSONObject(j);
                String pid = oneItem.getString("PID");
                List<String> licenses = new ArrayList<>();
                JSONArray slicenses = oneItem.optJSONArray("dnnt-labels");
                if (slicenses != null) {
                    for (int k = 0; k < slicenses.length(); k++) { licenses.add(slicenses.getString(k)); }
                }
                
                if (!result.containsKey(pid)) {
                    Map<String, Object> properties =  new HashMap<>();
                    result.put(pid, properties);
                }

                result.get(pid).put(FETCHER_LICENSES_KEY, licenses);

                if (oneItem.has("datum_str")) {
                    result.get(pid).put(FETCHER_DATE_KEY, oneItem.getString("datum_str"));
                }

                if (oneItem.has("fedora.model")) {
                    result.get(pid).put(FETCHER_MODEL_KEY, oneItem.getString("fedora.model"));
                }
                
                if (oneItem.has("dc.title")) {
                    result.get(pid).put(FETCHER_TITLES_KEY, Arrays.asList(oneItem.getString("dc.title")));
                }
                

            }
        }
        return result;
    }
}
