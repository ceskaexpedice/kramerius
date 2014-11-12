package cz.incad.kramerius.client.tools;

import static cz.incad.kramerius.client.utils.ApiCallsHelp.getJSON;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.client.utils.ApiCallsHelp;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;

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
                String api = K5Configuration.getK5ConfigurationInstance().getConfigurationObject().getString("api.point");
                if (!api.endsWith("/")) {
                    api += "/";
                }
                String jsoned = getJSON(api+"item/"+pid+"");
                this.itemObject = new JSONObject(jsoned);
            }
        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
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
                return this.itemObject.getString("title");
            } else return "";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "";
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
    /*
    public static void main(String[] args) throws UnsupportedEncodingException {
        String str = "uuid%253A5035a48a-5e2e-486c-8127-2fa650842e46";
        String string = URLDecoder.decode(str,"UTF-8");
        System.out.println(string);
        String nstring = URLDecoder.decode(string,"UTF-8");
        System.out.println(nstring);
    }*/
    
}
