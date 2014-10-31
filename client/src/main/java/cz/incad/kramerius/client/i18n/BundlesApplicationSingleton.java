package cz.incad.kramerius.client.i18n;

import static cz.incad.kramerius.client.tools.K5Configuration.getK5ConfigurationInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.client.tools.BasicAuthenticationFilter;

public class BundlesApplicationSingleton {

    public static final String BUNDLE_NAME="k5client";

    public static final BundlesApplicationSingleton _INSTANCE = new BundlesApplicationSingleton();
    public static final Logger LOGGER = Logger.getLogger(BundlesApplicationSingleton.class.getName());

    private Map<Locale, Map<String, BundleContent>> bundles = new HashMap<Locale,Map<String,BundleContent>>();
    private Map<Locale, List<String>> keys = new HashMap<Locale, List<String>>();
    
    public static BundlesApplicationSingleton getInstance() {
        return _INSTANCE;
    }
    
    synchronized void loadBundle(String bname, Locale locale) throws JSONException {
        try {
            String lang = locale.getLanguage();
            String country = locale.getCountry();
            StringBuilder psfix = new StringBuilder();
            psfix.append("&country=").append(country).append("&language=").append(lang);
            String burl = getK5ConfigurationInstance().getConfigurationObject().getString("k4.host") + "/i18n?action=bundle&name="+bname+"&format=json&";
            burl += psfix.toString();

            String jsonVal = LoadJSONVal.getJSONVal(burl);
            Map<String, BundleContent> contents = loadBundle(jsonVal);
            bundles.put(locale, contents);

            JSONObject jsonObj = new JSONObject(jsonVal);
            JSONObject bundle = jsonObj.getJSONObject("bundle");
            
            List<String> k = new ArrayList<String>();
            Iterator bKeys = bundle.keys();
            while(bKeys.hasNext()) {
                k.add(bKeys.next().toString());
            }
            this.keys.put(locale, k);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    static Map<String, BundleContent> loadBundle(String jsonVal) throws JSONException {
        Map<String, BundleContent> contents = new HashMap<String, BundleContent>();
        
        JSONObject jsonObj = new JSONObject(jsonVal);
        JSONObject bundle = jsonObj.getJSONObject("bundle");
        
        Iterator keys = bundle.keys();
        while(keys.hasNext()) {
            String k = keys.next().toString();
            StringTokenizer tokenizer = new StringTokenizer(k,".");
            String firstToken = tokenizer.nextToken();
            BundleContent childrenCont = null;
            if (!contents.containsKey(firstToken)) {
                BundleContent master = new BundleContent(firstToken);
                contents.put(firstToken, master);
            }
            BundleContent cont = contents.get(firstToken);
            childrenCont = cont.findOrCreateChildren(k);
            childrenCont.setValue(bundle.getString(k));
        }
        return contents;
    }

    public synchronized List<String> getKeys(Locale locale) throws JSONException {
        if (!bundles.containsKey(locale)) {
            loadBundle(BUNDLE_NAME, locale);
        }
        return this.keys.get(locale);
    }
    
    public synchronized Object get(Locale locale, String key) throws JSONException {
        if (!bundles.containsKey(locale)) {
            loadBundle(BUNDLE_NAME, locale);
        }
        StringTokenizer tokenizer = new StringTokenizer(key,".");
        Map<String, BundleContent> map = bundles.get(locale);
        String tokenized = tokenizer.nextToken();
        BundleContent bundleContent = map.get(tokenized);
        if (bundleContent == null) throw new IllegalStateException("key '"+tokenized+"' expecting");
        return bundleContent.get(key);
    }
}
