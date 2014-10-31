package cz.incad.kramerius.client.tools.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.client.i18n.BundlesApplicationSingleton;
import cz.incad.kramerius.client.i18n.TextApplicationSingleton;
import cz.incad.kramerius.client.tools.utils.LocalesDisect;

public class JSONTexts {

    public static final Logger LOGGER = Logger.getLogger(JSONTexts.class.getName());

    private HttpServletRequest req;
    private HttpServletResponse resp;
    private TextApplicationSingleton _instance =  TextApplicationSingleton.getInstance();

    private Locale locale;
    private String textName;

    public void configure(Map props) {
        req = (HttpServletRequest) props.get("request");
        resp = (HttpServletResponse) props.get("response");
        this.locale = LocalesDisect.findLocale(this.req);
        this.textName = req.getParameter("text");
        resp.setContentType("application/json; charset=utf-8");
    }


    private JSONObject jsonObject(String value) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(this.textName, value);
            return jsonObj;
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new JSONObject();
        }
    }
    
    public String getText() {
        if (this.textName != null) {
            try {
                Object retval = _instance.get(this.locale, this.textName);
                JSONObject jsonObj =jsonObject(retval.toString());
                return jsonObj.toString();
            } catch (JSONException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                JSONObject jsonObj =jsonObject("!"+this.textName+"!");
                return jsonObj.toString();
            }
        } else {
            return new JSONObject().toString();
        }
    }
}
