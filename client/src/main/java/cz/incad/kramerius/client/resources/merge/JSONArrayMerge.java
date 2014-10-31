package cz.incad.kramerius.client.resources.merge;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;

import cz.incad.kramerius.client.resources.merge.validators.ValidateInput;

/**
 * Merging json arrays functionality
 * @author pavels
 *
 */
public class JSONArrayMerge implements Merge<JSONArray> {

    public static final Logger LOGGER = Logger.getLogger(JSONArrayMerge.class.getName());

    private ValidateInput<JSONArray> validate;
    
    /**
     * Merge two json array inputs
     * @param warInput String from compiled and assembled war
     * @param confInput String from configuration directory
     * @return
     */
    public String merge(String warInput, String confInput) {
        try {
            JSONArray resultArray = new JSONArray();
            JSONArray warArr = new JSONArray(warInput);
            if (this.validate != null && (!this.validate.validate(warArr))) {
                warArr = new JSONArray();
                LOGGER.log(Level.WARNING,"errors in json : "+warArr.toString());
            }

            for (int i = 0,ll= warArr.length(); i < ll; i++) { resultArray.put(warArr.get(i)); }
            try {
                JSONArray confArr = new JSONArray(confInput);
                if (this.validate != null && (!this.validate.validate(confArr))) {
                    confArr = new JSONArray();
                    LOGGER.log(Level.WARNING,"errors in json : "+confArr.toString());
                }
                for (int i = 0,ll= confArr.length(); i < ll; i++) { resultArray.put(confArr.get(i)); }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
            return resultArray.toString();
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        }
    }

    @Override
    public void setValidateInput(ValidateInput<JSONArray> v) {
        this.validate = v;
    }

    @Override
    public ValidateInput<JSONArray> getValidateInput() {
        return this.validate;
    }
}
