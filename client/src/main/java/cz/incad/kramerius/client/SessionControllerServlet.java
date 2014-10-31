package cz.incad.kramerius.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import biz.sourcecode.base64Coder.Base64Coder;

/**
 * For controlling session keys and values
 * @author pavels
 */
public class SessionControllerServlet extends HttpServlet {

    public static Logger LOGGER = Logger.getLogger(SessionControllerServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fieldName = req.getParameter("name");
        Object jsonObj = req.getSession(true).getAttribute(fieldName);
        resp.setContentType("application/json");
        resp.getWriter().write(jsonObj.toString());
    }
    
    private Object jsonValue(String raw) throws JSONException {
        raw = raw.trim();
        if (raw.startsWith("{")) {
            return new JSONObject(raw);
        } else if (raw.startsWith("[")) {
            return new JSONArray(raw);
        } else {
            return raw;
        }
    }    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
            try {
                String fieldName = req.getParameter("name");
                String encodedField = req.getParameter("encodedfield");
                if (encodedField != null) {
                    byte[] decoded = Base64Coder.decode(encodedField);
                    Object jsonObj = jsonValue(new String(decoded, "UTF-8"));
                    req.getSession(true).setAttribute(fieldName, jsonObj);
                } else {
                    String field = req.getParameter("field");
                    Object jsonObj = jsonValue(field);
                    req.getSession(true).setAttribute(fieldName, jsonObj);
                }
            } catch (JSONException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
            
    }
}
