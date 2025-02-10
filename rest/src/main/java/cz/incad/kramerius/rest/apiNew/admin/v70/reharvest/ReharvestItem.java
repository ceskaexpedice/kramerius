package cz.incad.kramerius.rest.apiNew.admin.v70.reharvest;

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
 * Represents an item that requires reharvesting in the digital library system.
 * This class stores metadata about a document that should be updated due to various reasons
 * such as outdated content, deletion, or conflicts.
 * It allows conversion between object representation and JSON format.
 *
 * @author Pavel Šťastný
 */
public class ReharvestItem {

    /**
     * Enumeration of reharvest operation types.
     * Defines the scope of reharvesting such as entire document, children, or specific entities.
     */
    public static enum TypeOfReharvset {

        /** Complete reharvest of the entire title */
        root,

        /** Reharvest of the title and its children */
        children,

        /** The item was removed from CDK, reharvest its children */
        new_children,

        /** The root item was removed from CDK, requiring reharvest */
        new_root,

        /** Reharvest only a specific PID */
        only_pid, // pouze jeden pid

        /** Delete a single PID */
        delete_pid,

        /** Delete the entire document tree */
        delete_tree;
    }

    
    public static final String TIMESTAMP_KEYWORD = "indexed";
    public static final String PID_KEYWORD = "pid";
    public static final String OWN_PID_PATH = "own_pid_path";
    public static final String ROOT_PID = "root.pid";
    public static final String TYPE_KEYWORD = "type";
    
    public static final String STATE_KEYWORD = "state";
    public static final String NAME_KEYWORD = "name";
    public static final String ID_KEYWORD = "id";
    public static final String POD_NAME_KEYWORD = "pod";
    
    public static final String LIBRARIES_KEYWORD = "libraries";
    public static final String ERROR_MESSAGE_KEYWORD="error";
    
    private String id;
    private String name;
    private String state;
    private String pid;
    private String rootPid;
    private String ownPidPath;
    private TypeOfReharvset typeOfReharvest = TypeOfReharvset.root;
    
    
    private Instant timestamp = Instant.now();
    private String podname;
    
    private List<String> libraries = new ArrayList<>();


    /**
     * Constructs a new ReharvestItem.
     *
     * @param id Unique identifier.
     * @param name Name of the document.
     * @param state Current state of the document.
     * @param pid Persistent identifier.
     * @param ownPidPath Path to the document within the system.
     */
    public ReharvestItem(String id, String name, String state, String pid, String ownPidPath) {
        super();
        this.id = id;
        this.name = name;
        this.state = state;
        this.pid = pid;
        this.ownPidPath = ownPidPath;
    }

    /**
     * Constructs a ReharvestItem with an ID only.
     *
     * @param id Unique identifier.
     */
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
    public String getPid() {
        return this.pid;
    }
    public void setPid(String pid) {
        this.pid = pid;
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
    public void setPodname(String podname) {
        this.podname = podname;
    }
    public String getPodname() {
        return podname;
    }
    public String getOwnPidPath() {
        return ownPidPath;
    }
    public void setOwnPidPath(String ownPidPath) {
        this.ownPidPath = ownPidPath;
    }
    public TypeOfReharvset getTypeOfReharvest() {
        return typeOfReharvest;
    }
    public void setTypeOfReharvest(TypeOfReharvset typeOfReharvest) {
        this.typeOfReharvest = typeOfReharvest;
    }
    public String getRootPid() {
        return rootPid;
    }
    public void setRootPid(String rootPid) {
        this.rootPid = rootPid;
    }


    /**
     * Converts the ReharvestItem into a JSON representation.
     *
     * @return JSONObject containing the item's properties.
     */
    public JSONObject toJSON() {
        JSONObject obj  = new JSONObject();
        obj.put(ID_KEYWORD, this.id);
        obj.put(NAME_KEYWORD, this.name);
        if (this.state != null) {
            obj.put(STATE_KEYWORD, this.state);
        }
        obj.put(PID_KEYWORD, this.pid);
        
        if (!this.libraries.isEmpty()) {
            JSONArray libsArray = new JSONArray();
            this.libraries.forEach(libsArray::put);
        }
        
        if (this.podname != null) {
            obj.put(POD_NAME_KEYWORD, this.podname);
        }
        
        if (this.ownPidPath != null) {
            obj.put(OWN_PID_PATH, this.ownPidPath);
        }
        
        if (this.rootPid != null) {
            obj.put(ROOT_PID, this.rootPid);
        }
        
        if (this.libraries != null && !this.libraries.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            this.libraries.forEach(jsonArray::put);
            obj.put(LIBRARIES_KEYWORD, jsonArray);
        }
        
        obj.put(TYPE_KEYWORD, this.typeOfReharvest.name());
        obj.put(TIMESTAMP_KEYWORD,DateTimeFormatter.ISO_INSTANT.format(this.timestamp));

        return obj;
    }

    /**
     * Creates a ReharvestItem from a JSON object.
     *
     * @param json The JSONObject containing item data.
     * @return A new ReharvestItem object populated from JSON data.
     * @throws ParseException If parsing timestamp fails.
     */
    public static ReharvestItem fromJSON(JSONObject json) throws ParseException {
        String id = json.getString(ID_KEYWORD);
        String name= json.getString(NAME_KEYWORD);
        String pid =  json.getString(PID_KEYWORD);
        String ownPidPath = json.optString(OWN_PID_PATH);
        String rootPid = json.optString(ROOT_PID);
        
        String state = json.optString(STATE_KEYWORD);
        ReharvestItem item = new ReharvestItem(id);
        item.setName(name);
        item.setPid(pid);
        item.setOwnPidPath(ownPidPath);
        item.setRootPid(rootPid);
        
        
        item.setState(StringUtils.isAnyString(state) ? state : "open");
        //item.setType(StringUtils.isAnyString(type) ? type : "root.pid");
        
        if (json.has(TIMESTAMP_KEYWORD)) {
            String timestamp = json.getString(TIMESTAMP_KEYWORD);
            item.setTimestamp( Instant.from(DateTimeFormatter.ISO_INSTANT.parse(timestamp)));
        }
        
        if (json.has(POD_NAME_KEYWORD)) {
            item.setPodname(json.getString(POD_NAME_KEYWORD));
        }
        
        if (json.has(TYPE_KEYWORD)) {
            String tt = json.getString(TYPE_KEYWORD);
            TypeOfReharvset typeOfHarvest = TypeOfReharvset.valueOf(tt);
            item.setTypeOfReharvest(typeOfHarvest);
        }

        if (json.has(LIBRARIES_KEYWORD)) {
            JSONArray libsArray = json.getJSONArray(LIBRARIES_KEYWORD);
            List<String> libs = new ArrayList<>();
            for (int i = 0; i < libsArray.length(); i++) { libs.add(libsArray.getString(i));}
            item.setLibraries(libs);
        }
        return item;
    }
}
