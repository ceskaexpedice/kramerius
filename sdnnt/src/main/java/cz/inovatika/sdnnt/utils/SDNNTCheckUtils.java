package cz.inovatika.sdnnt.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.utils.RESTHelper;
import cz.inovatika.sdnnt.SDNNTCheck;

public class SDNNTCheckUtils {
	
	public static final String DNNTT_LICENSE = "dnntt";
	public static final String DNNTO_LICENSE = "dnnto";

	public static final List<String> AVAILABLE_LICENSES = Arrays.asList(DNNTT_LICENSE , DNNTO_LICENSE);

	public static final List<String> SKIPPING_MODELS = Arrays.asList("periodical","monograph");

	private SDNNTCheckUtils() {}
	
	
	private static void checkAgainstSolr(String solrEndpoint, List<String> checkingPids, List<String> filter, Consumer<JSONObject> consumer) throws IOException {
		String condition = checkingPids.stream().filter(Objects::nonNull).map(p -> {
	        return p.replace(":", "\\:");
	    }).collect(Collectors.joining(" OR "));
	
	    String encodedCondition = URLEncoder.encode("PID:(" + condition + ")", "UTF-8");
	    String encodedFieldList = URLEncoder.encode("PID dostupnost dnnt-labels fedora.model","UTF-8");
	    
	    String url = solrEndpoint + "/api/v5.0/search?q=" +  encodedCondition + "&wt=json&rows="+checkingPids.size()+"&fl="+encodedFieldList;
	    
	    for (int i = 0; i < filter.size(); i++) {
			url = url+"&fl="+filter.get(i);
		}
	    
	    InputStream inputStream = RESTHelper.inputStream(url, "", "");
	    JSONObject responseFromSolr = new JSONObject(IOUtils.toString(inputStream));
	    JSONObject responseObject = responseFromSolr.getJSONObject("response");
		JSONArray docs = responseObject.getJSONArray("docs");
	
		for (int i = 0; i < docs.length(); i++) {

			JSONObject docObject = docs.getJSONObject(i);
			consumer.accept(docObject);
		}
	}
	
	public static void checkAgainstSolrIsLincenseRemoved(String solrEndpoint, List<String> checkingPids, Map<String, List<String>> licenseRemove) throws IOException {
		checkAgainstSolr(solrEndpoint, checkingPids, Arrays.asList( URLEncoder.encode("dnnt-labels:dnnto OR dnnt-labels:dnntt","UTF-8")), (docObject)->{
			JSONArray labels = docObject.optJSONArray("dnnt-labels");
			String fedoraModel = docObject.optString("fedora.model");
			
			if (fedoraModel != null && !SKIPPING_MODELS.contains(fedoraModel)) {
	    		// dostupnost
	        		if (labels != null) {
	        			for (int i = 0; i < labels.length(); i++) {
	        				String label= labels.getString(i);
	        				if (AVAILABLE_LICENSES.contains(label)) {
		        				if (!licenseRemove.containsKey(label)) {
		        					licenseRemove.put(label, new ArrayList<>());
		        				}
		        				licenseRemove.get(label).add(docObject.getString("PID"));
	        					
	        				}
						}
	    		}
			}
		});
	}
	
	public static void checkAgainstSolrIsLicensePresent(String solrEndpoint, List<String> checkingPids, String checkingLicense,  List<String> missingLicense,Map<String, List<String>> licenseRemove) throws IOException {
		checkAgainstSolr(solrEndpoint, checkingPids, new ArrayList<>(), (docObject)->{
			JSONArray labels = docObject.optJSONArray("dnnt-labels");
			String dostupnost = docObject.optString("dostupnost");
			String fedoraModel = docObject.optString("fedora.model");
			
			if (fedoraModel != null && !SKIPPING_MODELS.contains(fedoraModel)) {
	    		// dostupnost
	    		if (dostupnost != null && dostupnost.equals("private")) {
	        		if (labels != null) {
	        			List<String> licenses = new ArrayList<>();
	        			for (int j = 0; j < labels.length(); j++) { licenses.add(labels.getString(j)); }
	        			if (!licenses.contains(checkingLicense)) {
	        				String pid = docObject.getString("PID");
	        				missingLicense.add(pid);
	        			}
	        			// odebira dnntt licenci 
	        			if (checkingLicense.equals("dnnto") && licenses.contains("dnntt")) {
	        				if (!licenseRemove.containsKey("dnntt")) {
	        					licenseRemove.put("dnntt", new ArrayList<String>());
	        				}
	        				licenseRemove.get("dnntt").add(docObject.getString("PID"));
	        			}
	        			
	        		} else {
	        			missingLicense.add(docObject.getString("PID"));
	        		}
	    		}
			}
		});
	}
	
}
