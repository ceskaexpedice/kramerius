package cz.incad.kramerius.client.tools;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.config.DefaultKey;
import org.json.JSONException;

import cz.incad.kramerius.client.i18n.BundlesApplicationSingleton;
import cz.incad.kramerius.client.i18n.TextApplicationSingleton;
import cz.incad.kramerius.client.tools.utils.LocalesDisect;

@DefaultKey("texts")
public class TextsTool {

    public static final Logger LOGGER = Logger.getLogger(TextsTool.class.getName());

    private HttpServletRequest req;
    private  TextApplicationSingleton _instance =  TextApplicationSingleton.getInstance();

    private Locale locale;

    public void configure(Map props) {
        req = (HttpServletRequest) props.get("request");
        this.locale = LocalesDisect.findLocale(this.req);
    }

    public String getLanguage() {
        return this.locale.getLanguage();
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
