package cz.incad.kramerius.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SupportedLibraries {
    
    public static final Logger LOGGER = Logger.getLogger(SupportedLibraries.class.getName());
    
    private List<String> codes = new ArrayList<>();
    
    public SupportedLibraries()  {
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream("registr");
            String str = IOUtils.toString(resourceAsStream, "UTF-8");
            JSONArray jsonObj = new JSONArray(str);
            for (int i = 0; i < jsonObj.length(); i++) {
                JSONObject libObject = jsonObj.getJSONObject(i);
                String code = libObject.optString("code");
                if (code != null) codes.add(code);
                
            }
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    public List<String> getCodes() {
        return codes;
    }
    
    public Pair<String,String> divideLibraryAndLicense(String cdklicense) {
        for (String code : codes) {
            if (cdklicense.startsWith(code+"_")) {
                int index = cdklicense.indexOf(code+"_")+(code).length();
                return Pair.of(cdklicense.substring(0, index), cdklicense.substring(index+1));
            }
        }
        return null;
    }
    
}
