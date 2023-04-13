package cz.inovatika.sdnnt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.utils.RESTHelper;

public class V7APILicenseFetcher extends LicenseAPIFetcher{

    public V7APILicenseFetcher(String baseUrl, String apiVersion) {
        super(baseUrl, apiVersion);
    }

    @Override
    public Map<String, List<String>> check(Set<String> pids) throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        
        String baseUrl = getApiUrl();
        List<String> processingPids = new ArrayList<>(pids);
        int numberOfIterations = processingPids.size() / BATCH_SIZE;
        numberOfIterations =  (processingPids.size() % BATCH_SIZE == 0) ? numberOfIterations : numberOfIterations +1;
        for (int i = 0; i < numberOfIterations; i++) {
            int start = i* BATCH_SIZE;
            int end = Math.min((i+1)*BATCH_SIZE, processingPids.size());
            List<String> batchPids = processingPids.subList(start, end);

            String condition = batchPids.stream().map(p -> {
                return p.replace(":", "\\:");
            }).collect(Collectors.joining(" OR "));

            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }
            
            String encodedCondition = URLEncoder.encode(
                    "pid:(" + condition + ")", "UTF-8");
            
            String encodedFieldList = URLEncoder.encode("pid licenses", "UTF-8");
            String url = baseUrl + "api/client/v7.0/search?q=" + encodedCondition + "&wt=json&rows=" + MAX_FETCHED_DOCS
                    + "&fl=" + encodedFieldList;
            
            InputStream is = RESTHelper.inputStream(url, null, null);
            String string = IOUtils.toString(is, Charset.forName("UTF-8"));
            JSONObject obj = new JSONObject(string);
            JSONObject response = obj.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");
            for (int j = 0; j < docs.length(); j++) {
                JSONObject oneItem = docs.getJSONObject(j);
                String pid = oneItem.getString("pid");
                List<String> licenses = new ArrayList<>();
                JSONArray slicenses = oneItem.optJSONArray("licenses");
                if (slicenses != null) {
                    for (int k = 0; k < slicenses.length(); k++) { licenses.add(slicenses.getString(k)); }
                }
                
                result.put(pid, licenses);
            }
        }
        return result;
    }
}
