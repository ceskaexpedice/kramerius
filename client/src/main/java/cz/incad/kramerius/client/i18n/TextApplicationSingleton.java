package cz.incad.kramerius.client.i18n;

import static cz.incad.kramerius.client.i18n.LoadJSONVal.getJSONVal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class TextApplicationSingleton {

    public static final TextApplicationSingleton _INSTANCE = new TextApplicationSingleton();
    public static final Logger LOGGER = Logger.getLogger(TextApplicationSingleton.class.getName());

    private Map<Locale, Map<String, String>> texts = new HashMap<Locale,Map<String,String>>();

    public synchronized Object get(Locale locale, String name) throws JSONException {
        if (!texts.containsKey(locale)) {
            loadText(name, locale);
        } else {
            Map<String, String> map = this.texts.get(locale);
            if (!map.containsKey(name)) {
                loadText(name, locale);
            }
        }
        return this.texts.get(locale).get(name);
    }

    synchronized void loadText(String tname, Locale locale) throws JSONException {
        try {
            if (!this.texts.containsKey(locale)) {
                this.texts.put(locale, new HashMap<String, String>());
            }
            String lang = locale.getLanguage();
            String country = locale.getCountry();
            StringBuilder psfix = new StringBuilder();
            psfix.append("&country=").append(country).append("&language=").append(lang);
            String burl = KConfiguration.getInstance().getConfiguration().getString("k4.host") + "/i18n?action=text&name="+tname+"&format=json&";
            burl += psfix.toString();

            String jsonVal = getJSONVal(burl);

            //String escapedVal = StringEscapeUtils.escapeJson(jsonVal);
            JSONObject obj = new JSONObject(jsonVal);
            JSONObject textObject = obj.getJSONObject("text");
            
            Map<String, String> map = this.texts.get(locale);
            map.put(textObject.getString("name"), textObject.getString("value"));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public static TextApplicationSingleton getInstance() {
        return _INSTANCE;
    }
}
