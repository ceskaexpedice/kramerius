package cz.incad.kramerius.client.resources.merge.validators;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MenuDefValidateInput implements ValidateInput<JSONArray>{

    public static final Logger LOGGER = Logger.getLogger(MenuDefValidateInput.class.getName());
    
    public static final String[] KEYS = new String[] {"name","i18nkey","object"};
    
    @Override
    public boolean validate(JSONArray rawOutput) {
        try {
            for (int i = 0,ll=rawOutput.length(); i < ll; i++) {
                Object obj = rawOutput.get(i);
                if (obj instanceof JSONObject) {
                    JSONObject jsonObj = (JSONObject) obj;
                    for (String key : KEYS) {
                        if (!jsonObj.has(key)) {
                            LOGGER.log(Level.SEVERE,"expecting key '"+key+"'");
                        }
                    }
                } else return false;
            }
            return true;
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }
}
