package cz.incad.Kramerius.security.strenderers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.JSONObject;

import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.Collection.Description;

public class CollectionsWrapper {

 
    private Collection col;
    private Locale loc;
    public CollectionsWrapper(Collection col, Locale loc) {
        super();
        this.col = col;
        this.loc = loc;
    }

    
    public String getPid() {
        return this.col.getPid();
    }
    
    
    public String getLabel() {
        String language = this.loc.getLanguage();
        Description lookup = this.col.lookup(language);
        if (lookup != null) {
            return lookup.getText();
        }
        return this.col.lookup("cs").getText();
    }
}
