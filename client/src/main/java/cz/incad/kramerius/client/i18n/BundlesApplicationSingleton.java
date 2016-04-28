package cz.incad.kramerius.client.i18n;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.client.utils.ApiCallsHelp;
import cz.incad.kramerius.service.impl.ResourceBundleServiceImpl.ResourceClassLoader;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;

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

            ResourceBundle fromClient =  ResourceBundle.getBundle(bname, locale, new ClientResourceClassLoader());
            
            String lang = locale.getLanguage();
            String country = locale.getCountry();
            StringBuilder psfix = new StringBuilder();
            psfix.append("&country=").append(country).append("&language=").append(lang);
            String burl = KConfiguration.getInstance().getConfiguration().getString("k4.host") + "/i18n?action=bundle&name="+bname+"&format=json&";
            burl += psfix.toString();

            String jsonVal = ApiCallsHelp.getJSON(burl);

            JSONObject bundle = mergeBundles(fromClient, new JSONObject(jsonVal).getJSONObject("bundle"));
            Map<String, BundleContent> contents = loadBundle(bundle);
            bundles.put(locale, contents);

            List<String> k = new ArrayList<String>();
            Iterator bKeys = bundle.keys();
            while(bKeys.hasNext()) {
                k.add(bKeys.next().toString());
            }
            this.keys.put(locale, k);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    static JSONObject mergeBundles(ResourceBundle fromClient, JSONObject jsonObj) throws JSONException {
        JSONObject retval = new JSONObject();
        Set<String> allkeys =  new HashSet<String>();
        allkeys.addAll(fromClient.keySet());
        for (Iterator iterator = jsonObj.keys(); iterator.hasNext();) {
            String string = (String) iterator.next();
            if (!allkeys.contains(string)) {
                allkeys.add(string);
            }
        }
        for (String k : allkeys) {
            if (jsonObj.has(k)) {
                retval.put(k, jsonObj.get(k));
            } else {
                retval.put(k, fromClient.getString(k));
            }
        }
        
        return retval;
    }

    static Map<String, BundleContent> loadBundle(JSONObject bundle) throws JSONException {
        Map<String, BundleContent> contents = new HashMap<String, BundleContent>();
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

    public static class ClientResourceClassLoader extends ClassLoader {

        /**
         * Charset used when reading a properties file.
         */
        private static final String CHARSET = "UTF-8";

        /**
         * Buffer size used when reading a properties file.
         */
        private static final int BUFFER_SIZE = 2000;


        public ClientResourceClassLoader() {
            super();
        }

        static InputStream readUTFStreamToEscapedASCII(InputStream is)
                throws IOException {
            Reader reader = new InputStreamReader(is, CHARSET);

            StringBuilder builder = new StringBuilder(BUFFER_SIZE);
            char[] buffer = new char[BUFFER_SIZE];

            while (true) {
                int length = reader.read(buffer);

                if (length < 0)
                    break;

                for (int i = 0; i < length; i++) {
                    char ch = buffer[i];

                    if (ch <= '\u007f') {
                        builder.append(ch);
                        continue;
                    }

                    builder.append(String.format("\\u%04x", (int) ch));
                }
            }
            reader.close();
            byte[] resourceContent = builder.toString().getBytes();
            return new ByteArrayInputStream(resourceContent);
        }

        
        
        @Override
        protected URL findResource(String name) {
            LOGGER.info("find resource name '"+name+"'");

            if (name.endsWith("_en.properties"))
                name = name.substring(0,
                        name.length() - "_en.properties".length())
                        + ".properties";
            
            return this.getClass().getClassLoader().getResource(name);
        }

        
        @Override
        public InputStream getResourceAsStream(String name) {
            try {
                InputStream istr = super.getResourceAsStream(name);
                return readUTFStreamToEscapedASCII(istr);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                return null;
            }
        }
    }
    
}
