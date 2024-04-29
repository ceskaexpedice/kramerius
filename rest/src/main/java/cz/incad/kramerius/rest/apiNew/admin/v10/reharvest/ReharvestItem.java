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

public class ReharvestItem {
    
//    static enum ReharvestItemState {
//        open, closed;
//    }
    
    //public static FastDateFormat FORMAT =  FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", TimeZone.getTimeZone("UTC"));    
    
    
    private String id;
    private String name;
    private String state;
    private String type;
    private List<String> pids= new ArrayList<>();
    private Instant timestamp = Instant.now();

    
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
    
    public JSONObject toJSON() {
        JSONObject obj  = new JSONObject();
        obj.put("id", this.id);
        obj.put("name", this.name);
        if (this.state != null) {
            obj.put("state", this.state);
        }
        if (this.type != null) {
            obj.put("type", this.type);
        }
        
        JSONArray jsonArray = new JSONArray();
        this.pids.forEach(jsonArray::put);
        obj.put("pids", jsonArray);
        
        obj.put("timestamp",DateTimeFormatter.ISO_INSTANT.format(this.timestamp));

        return obj;
    }
    
    public static ReharvestItem fromJSON(JSONObject json) throws ParseException {
        String id = json.getString("id");
        String name= json.getString("name");
        JSONArray array= json.getJSONArray("pids");
        List<String> pids = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            pids.add(array.getString(i));
        }

        String state = json.optString("state");
        String type = json.optString("type");
        ReharvestItem item = new ReharvestItem(id);
        item.setName(name);
        item.setPids(pids);
        
        item.setState(StringUtils.isAnyString(state) ? state : "open");
        item.setType(StringUtils.isAnyString(type) ? type : "root.pid");
        
        if (json.has("timestamp")) {
            String timestamp = json.getString("timestamp");
            item.setTimestamp( Instant.from(DateTimeFormatter.ISO_INSTANT.parse(timestamp)));
        }
        
        return item;
    }
}
