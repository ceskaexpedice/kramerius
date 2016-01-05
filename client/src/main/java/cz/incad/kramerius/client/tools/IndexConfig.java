/*
 * Copyright (C) 2014 Alberto Hernandez
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.client.tools;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class IndexConfig {
    public static final Logger LOGGER = Logger.getLogger(IndexConfig.class.getName());
    private static IndexConfig _sharedInstance = null;
    private final JSONObject fieldsConfig;
    private final JSONObject mappings;
    
    public synchronized static IndexConfig getInstance() throws IOException, JSONException {
        if (_sharedInstance == null) {
            _sharedInstance = new IndexConfig();
        }
        return _sharedInstance;
    }
    
    public synchronized static void resetInstance() {
        _sharedInstance = null;
    }
    
    public IndexConfig() throws IOException, JSONException{
        String path = System.getProperty("user.home")+File.separator+".kramerius4" + File.separator + "k5client" + File.separator + "fields.json";
//        File f = new File(path);
//        if (!f.exists() || !f.canRead()) {
//            f = FileUtils.toFile(IndexConfig.class.getResource("/cz/incad/kramerius/client/tools/fields.json"));
//        }
        File fdef = FileUtils.toFile(IndexConfig.class.getResource("/cz/incad/kramerius/client/tools/fields.json"));
        String json = FileUtils.readFileToString(fdef, "UTF-8");
        fieldsConfig = new JSONObject(json);
        
        File f = new File(path);
        if (f.exists() && f.canRead()) {
            json = FileUtils.readFileToString(f, "UTF-8");
            JSONObject confCustom = new JSONObject(json);
            Iterator keys = confCustom.keys();
            while (keys.hasNext() ) {
                String key = (String) keys.next();
                LOGGER.log(Level.INFO, "key {0} will be overrided", key);
                fieldsConfig.put(key, confCustom.get(key));
            }
        }
        
        mappings = fieldsConfig.getJSONObject("mappings");
    }
    
    public String getMappedField(String field){
            return mappings.optString(field, field);
    }
    
    public String getMappings(){
        return mappings.toString();
    }
    
    public JSONObject getJSON(){
        return fieldsConfig;
    }
}
