package cz.incad.kramerius.client.resources.merge.validators;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewersDefValidateInput implements ValidateInput<JSONArray> {

    public static final Logger LOGGER = Logger.getLogger(ViewersDefValidateInput.class.getName());

    public static final String ID_KEY ="id";
    public static final String OBJECT_KEY="object";
    

    @Override
    public boolean validate(JSONArray rawOutput) {
        try {
            for (int i = 0,ll=rawOutput.length(); i < ll; i++) {
                Object obj = rawOutput.get(i);
                if (obj instanceof JSONObject) {
                    JSONObject jsonObj = (JSONObject) obj;
                    if (!jsonObj.has(ID_KEY)) return false;
                    Object id = jsonObj.get(ID_KEY);
                    if (!(id instanceof Number)) {
                        return false;
                    }
                    if (!jsonObj.has(OBJECT_KEY)) return false;
                    Object objVal = jsonObj.get(OBJECT_KEY);
                    if (!(objVal instanceof String)) {
                        return false;
                    }
                    return true;
                } else return false;
            }
            return true;
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }
}
