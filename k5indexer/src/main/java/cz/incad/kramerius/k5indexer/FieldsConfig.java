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
package cz.incad.kramerius.k5indexer;

import cz.incad.kramerius.Constants;
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
public class FieldsConfig {
    private static FieldsConfig _sharedInstance = null;
    private final JSONObject fieldsConfig;
    private final JSONObject mappings;
    private final JSONObject streams;
    
    public synchronized static FieldsConfig getInstance() throws IOException, JSONException {
        if (_sharedInstance == null) {
            _sharedInstance = new FieldsConfig();
        }
        return _sharedInstance;
    }
    
    public FieldsConfig() throws IOException, JSONException{
        String path = Constants.WORKING_DIR + File.separator + "k5indexer" + File.separator + "fields.json";
        File f = new File(path);
        if (!f.exists() || !f.canRead()) {
            f = FileUtils.toFile(KrameriusDocument.class.getResource("/cz/incad/kramerius/k5indexer/res/fields.json"));
        }
        String json = FileUtils.readFileToString(f, "UTF-8");
        fieldsConfig = new JSONObject(json);
        mappings = fieldsConfig.getJSONObject("mappings");
        streams = fieldsConfig.getJSONObject("datastreams");
    }
    
    public String getMappedField(String field){
            return mappings.optString(field, field);
    }
    
    public JSONObject getDataStream(String dsname) throws JSONException{
            return streams.getJSONObject(dsname);
    }
    
    public Iterator dataStreams(){
        return streams.keys();
    }
}
