package cz.incad.kramerius.client.tools;

import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.client.kapi.auth.CallUserController;

public class SessionController {

    public static final Logger LOGGER = Logger.getLogger(SessionController.class.getName());
    
    protected HttpServletRequest req;

    public void configure(Map props) {
        req = (HttpServletRequest) props.get("request");
    }

    public boolean isJSONAware(Object val)  {
        return (val instanceof JSONObject || val instanceof JSONArray);
    }
    
    
    
    public String getSessionFieldsJSONRepresentation() {
        JSONObject jsonObj = new JSONObject();
        HttpSession session = req.getSession(true);
        Enumeration allAttrs = session.getAttributeNames();
        while(allAttrs.hasMoreElements()) {
            String name = (String) allAttrs.nextElement();
            Object attribute = session.getAttribute(name);
            if (isJSONAware(attribute)) {
                try {
                    jsonObj.put(name, attribute);
                } catch (JSONException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
        }
        return jsonObj.toString();
    }
    
}
