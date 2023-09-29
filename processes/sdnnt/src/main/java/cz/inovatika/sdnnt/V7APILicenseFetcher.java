package cz.inovatika.sdnnt;

import java.io.File;
import java.io.FileOutputStream;
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

import cz.incad.kramerius.security.licenses.impl.embedded.cz.CzechEmbeddedLicenses;
import cz.incad.kramerius.utils.RESTHelper;

public class V7APILicenseFetcher extends LicenseAPIFetcher{

    public V7APILicenseFetcher(String baseUrl, String apiVersion, boolean privateFilter) {
        super(baseUrl, apiVersion, privateFilter);
    }

    
    
    public Map<String, Map<String, Object>> check(Set<String> pids) throws IOException{
        int maxsize = pids.size();
        int cumulative = 0;        
        
        Map<String, Map<String, Object>> result = new HashMap<>();
        
        String baseUrl = getApiUrl();
        List<String> processingPids = new ArrayList<>(pids);
        int numberOfIterations = processingPids.size() / BATCH_SIZE;
        numberOfIterations =  (processingPids.size() % BATCH_SIZE == 0) ? numberOfIterations : numberOfIterations +1;
        for (int i = 0; i < numberOfIterations; i++) {
            int start = i* BATCH_SIZE;
            int end = Math.min((i+1)*BATCH_SIZE, processingPids.size());
            List<String> batchPids = processingPids.subList(start, end);
            cumulative = cumulative+batchPids.size();

            String condition = batchPids.stream().map(p -> {
                return  '"'+ p +'"';
            }).collect(Collectors.joining(" OR "));

            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }
            
            String encodedCondition = URLEncoder.encode(
                    "pid:(" + condition + ")", "UTF-8");
            
            
            String encodedFieldList = URLEncoder.encode("pid licenses date.str model titles.search  licenses_of_ancestors", "UTF-8");
            String url = baseUrl + (baseUrl.endsWith("/") ?  "":"/") +"search?q=" + encodedCondition + "&wt=json&rows=" + MAX_FETCHED_DOCS
                    + "&fl=" + encodedFieldList;

            String filter = URLEncoder.encode("(licenses:"+CzechEmbeddedLicenses.ONSITE_LICENSE.getName()+" OR accessibility:private)","UTF-8");
            if (this.isPrivateFilter()) {
                url = url +"&fq=" + filter;
            }
            
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
                
                
                //TODO: Discuss 
                JSONArray ancLicenses = oneItem.optJSONArray("licenses_of_ancestors");
                if (ancLicenses != null) {
                    for (int k = 0; k < ancLicenses.length(); k++) { licenses.add(ancLicenses.getString(k)); }
                }
        
                if (!result.containsKey(pid)) {
                    Map<String, Object> properties =  new HashMap<>();
                    result.put(pid, properties);
                }

                result.get(pid).put(FETCHER_LICENSES_KEY, licenses);

                if (oneItem.has("date.str")) {
                    result.get(pid).put(FETCHER_DATE_KEY, oneItem.getString("date.str"));
                }

                if (oneItem.has("model")) {
                    result.get(pid).put(FETCHER_MODEL_KEY, oneItem.getString("model"));
                }
                if (oneItem.has("titles.search")) {
                    JSONArray optJSONArray = oneItem.optJSONArray("titles.search");
                    List<String> titles = new ArrayList<>();
                    for (int k = 0; k < optJSONArray.length(); k++) {
                        titles.add(optJSONArray.getString(k));
                    }
                    result.get(pid).put(FETCHER_TITLES_KEY, titles);
                }
                
                //result.put(pid, licenses);
            }
        }
        return result;
    }
}
