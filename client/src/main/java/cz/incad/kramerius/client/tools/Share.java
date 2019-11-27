package cz.incad.kramerius.client.tools;

import static cz.incad.kramerius.client.utils.ApiCallsHelp.getJSON;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class Share {

    public static Logger LOGGER = Logger.getLogger(Share.class.getName());

    private JSONObject itemObject = null;
    private String applicationUrl;
    
    public void configure(Map props) {
        try {
            HttpServletRequest req = (HttpServletRequest) props.get("request");
            String pid = req.getParameter("pid");
            this.applicationUrl = ApplicationURL.applicationURL(req);
            if (pid != null && StringUtils.isAnyString(pid)) {
                JSONObject rJsonObject = jsonFromAPI(pid);
                this.itemObject = rJsonObject;
            }
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }


    protected JSONObject jsonFromAPI(String pid) {
        String api = KConfiguration.getInstance().getConfiguration().getString("api.point");
        if (!api.endsWith("/")) {
            api += "/";
        }
        String jsoned = getJSON(api+"item/"+pid+"");
        JSONObject rJsonObject = new JSONObject(jsoned);
        return rJsonObject;
    }
    
    
    public String getRootTitle() {
        try {
            if (this.itemObject != null) {
                return this.itemObject.getString("root_title");
            } else return "";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "";
        }
    }
    
    public String getTitle() {
        try {
            if (this.itemObject != null) {
                List<String> titles = new ArrayList<String>();
                JSONArray jsonArray = selectContext(this.itemObject.getJSONArray("context"));
                if (jsonArray != null) {
                    for (int i = 0,ll=jsonArray.length(); i < ll; i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);
                        String pid = jsonObj.getString("pid");
                        titles.add(normalizedTitleFromGivenJSON(pid));
                    }
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < titles.size(); i++) {
                        if (i > 0) builder.append(" > ");
                        builder.append(titles.get(i));
                    }
                    return builder.toString();
                } else {
                    return this.itemObject.getString("title");
                }
            } else return "";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "";
        }
    }
    
    private JSONArray selectContext(JSONArray jsonArray) {
        if (jsonArray.length() > 0) return jsonArray.getJSONArray(0);
        else return null;
    }


    private String normalizedTitleFromGivenJSON(String pid) {
        JSONObject jsonFromAPI = jsonFromAPI(pid);
        if (jsonFromAPI.getString("model").equals("periodicalitem")) {
            JSONObject jsonObject = jsonFromAPI.getJSONObject("details");
            if (jsonObject.has("date")) {
                return jsonObject.getString("date");
            }
            if (jsonObject.has("issueNumber")) {
                return jsonObject.getString("issueNumber");
            }
            if (jsonObject.has("partNumber")) {
                return jsonObject.getString("partNumber");
            }
            return jsonFromAPI.getString("title");
        } else if  (jsonFromAPI.getString("model").equals("periodicalvolume")) {
            JSONObject jsonObject = jsonFromAPI.getJSONObject("details");
            if (jsonObject.has("year")) {
                return jsonObject.getString("year");
            }
            return jsonFromAPI.getString("title");
        } else {
            String title = jsonFromAPI.getString("title");
            if (StringUtils.isAnyString(title)) {
                return title.length() > 20 ? title.substring(0, 20)+ " ... " : title;
            } else return "";
        }
    }

    public String getModel() {
        try {
            if (this.itemObject != null) {
                return this.itemObject.getString("model"); // I18N
            } else return "";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "";
        }
    }

    public String getItemUri() {
        try {
            if (this.itemObject != null) {
                String encoded = URLEncoder.encode(this.itemObject.getString("pid"),"UTF-8");
                //http://vmkramerius.incad.cz:8080/client/index.vm?page=doc#!uuid%3A062e63f0-3a33-4c3a-ad68-7378ba565f92
                return this.applicationUrl +(this.applicationUrl.endsWith("/") ? "" : "/") + "index.vm?page=doc&_escaped_fragment_="+encoded;
                //return this.applicationUrl +(this.applicationUrl.endsWith("/") ? "" : "/") + "index.vm?page=doc#!"+encoded;
            } else return "";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "";
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "";
        }
    }


    public String getItemThumbUri() {
        try {
            if (this.itemObject != null) {
                String encoded = URLEncoder.encode(this.itemObject.getString("pid"),"UTF-8");
                return this.applicationUrl +(this.applicationUrl.endsWith("/") ? "" : "/") + "api/item/"+encoded+"/thumb";
            } else return "";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "";
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "";
        }
    }

    public String getDescription() {
        return this.getRootTitle() +","+this.getModel();
    }

    public String getDetails() {
        try {
            if (this.itemObject != null) {
                return this.itemObject.getString("model"); // I18N
            } else return "";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "";
        }
    }
}
