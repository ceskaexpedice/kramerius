package cz.incad.kramerius.client.tools.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.config.DefaultKey;
import org.json.JSONException;

import cz.incad.kramerius.client.i18n.BundlesApplicationSingleton;
import cz.incad.kramerius.client.tools.I18NTool;
import cz.incad.kramerius.client.tools.utils.LocalesDisect;

public class JSONDictionary {

    public static final Logger LOGGER = Logger.getLogger(I18NTool.class.getName());

    private HttpServletRequest req;
    private HttpServletResponse resp;
    private  BundlesApplicationSingleton _instance =  BundlesApplicationSingleton.getInstance();

    private Locale locale;

    public void configure(Map props) {
        req = (HttpServletRequest) props.get("request");
        resp = (HttpServletResponse) props.get("response");
        this.locale = LocalesDisect.findLocale(this.req);
        resp.setContentType("application/json; charset=utf-8");
    }

    public List<String> getKeys() {
        try {
            return _instance.getKeys(this.locale);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<String>();
        }
    }
    
    public Object get(String key) {
        try {
            Object retval = _instance.get(this.locale, key);
            return retval;
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "!"+key+"!";
        }
    }

}
