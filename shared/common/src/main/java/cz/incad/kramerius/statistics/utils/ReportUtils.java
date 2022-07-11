package cz.incad.kramerius.statistics.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.IdentifiersFilter;
import cz.incad.kramerius.statistics.filters.LicenseFilter;
import cz.incad.kramerius.statistics.filters.ModelFilter;

public class ReportUtils {

    public static Logger LOGGER = Logger.getLogger(ReportUtils.class.getName());

    private ReportUtils() {}

    //            StringBuilder builder = new StringBuilder("q=*");

    public static void enhanceLicense(StringBuilder builder, LicenseFilter licFilter) {
        if (licFilter != null && licFilter.getLicence() != null) {
            builder.append("&fq=provided_by_license:"+licFilter.getLicence());
        }
    }
    
    public static void enhanceIdentifiers(StringBuilder builder, IdentifiersFilter iFilter) {
        if (iFilter != null && iFilter.getIdentifier() != null) {
            try {
                String encoded = URLEncoder.encode("(all_identifiers:\""+iFilter.getIdentifier() +"\" OR all_pids:\""+iFilter.getIdentifier()+"\")", "UTF-8");
                builder.append("&fq="+encoded);
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }

        }
    }
    
    public static void enhanceDateFilter(StringBuilder builder, DateFilter dateFilter) {
        if (dateFilter != null && dateFilter.getFromDate() != null && dateFilter.getToDate() != null) {
            try {
                String encoded = URLEncoder.encode("date:["+dateFilter.getFromDate()+" TO "+dateFilter.getToDate()+"]", "UTF-8");
                builder.append("&fq="+encoded);
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
    }
    
    public static void enhanceModelFilter(StringBuilder builder, ModelFilter modelFilter) {
        if (modelFilter.getModel() != null ) {
            builder.append("&fq=all_models:"+modelFilter.getModel());
        }
    }

    public static void facetIterate(String facetField, String resultStr, Consumer<Pair<Object, Object>> consumer) {
            JSONObject result = new JSONObject(resultStr);
            if (result.has("facet_counts")) {
                if (result.getJSONObject("facet_counts").has("facet_fields")) {
                    JSONObject facetFields = result.getJSONObject("facet_counts").getJSONObject("facet_fields");
                    if (facetFields.has(facetField)) {
                        JSONArray jsonArray = facetFields.getJSONArray(facetField);
                        int i = 0;
                        while(i < jsonArray.length()) {
                            Object key = jsonArray.get(i++);
                            Object value = jsonArray.get(i++);
                            
                            consumer.accept(Pair.of(key, value));
    //                        
    //                        
    //                        Map<String, Object> map = new HashMap<String, Object>();
    //                        map.put(key.toString(), value);
    //
    //                        langs.add(map);
                        }
                    }
                }
            }
        }
}
