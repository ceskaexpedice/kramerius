package cz.incad.Kramerius.security.strenderers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.JSONObject;

public class CollectionsWrapper {

 
    private JSONObject json;
    private Locale loc;
    public CollectionsWrapper(JSONObject json, Locale loc) {
        super();
        this.json = json;
        this.loc = loc;
    }

    
    public String getPid() {
        return this.json.getString("pid");
    }
    
    
    public String getLabel() {
        String language = this.loc.getLanguage();
        JSONObject descs = this.json.getJSONObject("descs");
        if (descs.has(language)) {
            return descs.getString(language);
        }
        return descs.getString("cs");
    }
}
