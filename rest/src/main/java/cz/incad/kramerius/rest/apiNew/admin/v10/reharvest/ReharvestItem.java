package cz.incad.kramerius.rest.apiNew.admin.v10.reharvest;

import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.utils.StringUtils;

/** 
 * Represents new Reharvest item
 * @author happy
 */
public class ReharvestItem {
    
//    static enum ReharvestItemState {
//        open, closed;
//    }

    //public static FastDateFormat FORMAT =  FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", TimeZone.getTimeZone("UTC"));    
    
    
    private static final String TIMESTAMP_KEYWORD = "timestamp";
    private static final String PIDS_KEYWORD = "pids";
    private static final String TYPE_KEYWORD = "type";
    private static final String STATE_KEYWORD = "state";
    private static final String NAME_KEYWORD = "name";
    private static final String ID_KEYWORD = "id";
    private static final String POD_NAME_KEYWORD = "pod";
    
    // unique identifier 
    private String id;
    private String name;
    private String state;
    private String type;
    private List<String> pids= new ArrayList<>();
    private Instant timestamp = Instant.now();
    private String podname;
    
    private List<String> libraries = new ArrayList<>();
    
    
    public ReharvestItem(String id, String name, String state, List<String> pids) {
        super();
        this.id = id;
        this.name = name;
        this.state = state;
        this.pids = pids;
    }

    public ReharvestItem(String id) {
        super();
        this.id = id;
    }
    
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public List<String> getPids() {
        return pids;
    }
    
    public void setPids(List<String> pids) {
        this.pids = pids;
    }
    
    public List<String> getLibraries() {
        return libraries;
    }
    
    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    
    public void setPodname(String podname) {
        this.podname = podname;
    }
    
    public String getPodname() {
        return podname;
    }
    
    
    public JSONObject toJSON() {
        JSONObject obj  = new JSONObject();
        obj.put(ID_KEYWORD, this.id);
        obj.put(NAME_KEYWORD, this.name);
        if (this.state != null) {
            obj.put(STATE_KEYWORD, this.state);
        }
        if (this.type != null) {
            obj.put(TYPE_KEYWORD, this.type);
        }
        
        JSONArray jsonArray = new JSONArray();
        this.pids.forEach(jsonArray::put);
        obj.put(PIDS_KEYWORD, jsonArray);
        
        if (!this.libraries.isEmpty()) {
            JSONArray libsArray = new JSONArray();
            this.libraries.forEach(libsArray::put);
        }
        
        if (this.podname != null) {
            obj.put(POD_NAME_KEYWORD, this.podname);
        }
        
        obj.put(TIMESTAMP_KEYWORD,DateTimeFormatter.ISO_INSTANT.format(this.timestamp));

        return obj;
    }
    
    public static ReharvestItem fromJSON(JSONObject json) throws ParseException {
        String id = json.getString(ID_KEYWORD);
        String name= json.getString(NAME_KEYWORD);
        JSONArray array= json.getJSONArray(PIDS_KEYWORD);
        List<String> pids = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            pids.add(array.getString(i));
        }

        String state = json.optString(STATE_KEYWORD);
        String type = json.optString(TYPE_KEYWORD);
        ReharvestItem item = new ReharvestItem(id);
        item.setName(name);
        item.setPids(pids);
        
        item.setState(StringUtils.isAnyString(state) ? state : "open");
        item.setType(StringUtils.isAnyString(type) ? type : "root.pid");
        
        if (json.has(TIMESTAMP_KEYWORD)) {
            String timestamp = json.getString(TIMESTAMP_KEYWORD);
            item.setTimestamp( Instant.from(DateTimeFormatter.ISO_INSTANT.parse(timestamp)));
        }
        
        if (json.has(POD_NAME_KEYWORD)) {
            item.setPodname(json.getString(POD_NAME_KEYWORD));
        }
        
        return item;
    }
}
