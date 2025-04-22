package cz.incad.kramerius.rest.apiNew.admin.v70.reharvest;

import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.utils.StringUtils;

/**
 * Represents a reharvest task with different types and states.
 * A reharvest task can be in one of several states (open, running, failed, finished)
 * and can perform different types of reharvest operations (full reharvest, partial, deletion, etc.).
 *
 * @author Pavel Šťastný
 */
public class ReharvestItem {

    /**
     * Enum representing the type of reharvest operation.
     */
    public static enum TypeOfReharvset {

        /** Complete reharvest of the entire title */
        root {
            @Override
            public boolean isNewHarvest() {
                return false;
            }

            @Override
            public boolean isDeletingReharvest() {
                return false;
            }
        },

        /** Reharvest children */
        children {
            @Override
            public boolean isNewHarvest() {
                return false;
            }

            @Override
            public boolean isDeletingReharvest() {
                return false;
            }
        },
        /** Object was deleted and needs to fetch information about children and title */
        new_children {
            @Override
            public boolean isNewHarvest() {
                return true;
            }

            @Override
            public boolean isDeletingReharvest() {
                return false;
            }
        },
        /** Object was deleted and needs to fetch information about the root title*/
        new_root {
            @Override
            public boolean isNewHarvest() {
                return true;
            }

            @Override
            public boolean isDeletingReharvest() {
                return false;
            }
        },
        /** Only a single PID */
        only_pid {
            @Override
            public boolean isNewHarvest() {
                return false;
            }

            @Override
            public boolean isDeletingReharvest() {
                return false;
            }
        },
        /** Delete a specific PID */
        delete_pid {
            @Override
            public boolean isNewHarvest() {
                return false;
            }

            @Override
            public boolean isDeletingReharvest() {
                return true;
            }
        },
        /** Delete an entire subtree */
        delete_tree {
            @Override
            public boolean isNewHarvest() {
                return false;
            }

            @Override
            public boolean isDeletingReharvest() {
                return true;
            }
        },

        /** Delete root pid */
        delete_root {
            @Override
            public boolean isNewHarvest() {
                return false;
            }

            @Override
            public boolean isDeletingReharvest() {
                return true;
            }
        };

        public abstract boolean isNewHarvest();
        public abstract boolean isDeletingReharvest();
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
    public static final String CONFLICT_ID_KEYWORD = "conflict_id";

    /** Unique identifier for the reharvest task */
    private String id;
    /** Name of the reharvest task */
    private String name;
    /** Current state of the reharvest task */
    private String state;
    /** PID associated with this task */
    private String pid;
    /** Root PID if applicable */
    private String rootPid;
    /** Path to the own pid path */
    private String ownPidPath;
    /** Type of reharvest operation */
    private TypeOfReharvset typeOfReharvest = TypeOfReharvset.root;
    /** Timestamp of the last update */
    private Instant timestamp = Instant.now();
    /** Name of the pod */
    private String podname;
    /** List of libraries associated with this reharvest */
    private List<String> libraries = new ArrayList<>();

    private String conflictId;

    /**
     * Constructor initializing a reharvest task with required attributes.
     *
     * @param id Unique identifier
     * @param name Name of the task
     * @param state Current state
     * @param pid PID associated with the task
     * @param ownPidPath Path to the PID
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
     * Constructor initializing a reharvest task with only an ID.
     *
     * @param id Unique identifier
     * Constructs a ReharvestItem with an ID only.
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


    public String getConflictId() {
        return conflictId;
    }

    public void setConflictId(String conflictId) {
        this.conflictId = conflictId;
    }

    /**
     * Converts the object to a JSON representation.
     *
     * @return JSONObject representing the reharvest item
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

        if (this.conflictId != null) {
            obj.put(CONFLICT_ID_KEYWORD, this.conflictId);
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
     * Constructs a ReharvestItem object from a JSON representation.
     *
     * @param json JSONObject representing the reharvest item
     * @return ReharvestItem instance
     * @throws ParseException If the timestamp format is incorrect
     */
    public static ReharvestItem fromJSON(JSONObject json) throws ParseException {
        String id = json.getString(ID_KEYWORD);
        String name= json.optString(NAME_KEYWORD);
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
        if (json.has(CONFLICT_ID_KEYWORD)) {
            String tt = json.getString(CONFLICT_ID_KEYWORD);
            item.setConflictId(tt);
        }

        if (json.has(LIBRARIES_KEYWORD)) {
            JSONArray libsArray = json.getJSONArray(LIBRARIES_KEYWORD);
            List<String> libs = new ArrayList<>();
            for (int i = 0; i < libsArray.length(); i++) { libs.add(libsArray.getString(i));}
            item.setLibraries(libs);
        }
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReharvestItem item = (ReharvestItem) o;
        return Objects.equals(id, item.id) && Objects.equals(name, item.name) && Objects.equals(state, item.state) && Objects.equals(pid, item.pid) && Objects.equals(rootPid, item.rootPid) && Objects.equals(ownPidPath, item.ownPidPath) && typeOfReharvest == item.typeOfReharvest && Objects.equals(timestamp, item.timestamp) && Objects.equals(podname, item.podname) && Objects.equals(libraries, item.libraries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, state, pid, rootPid, ownPidPath, typeOfReharvest, timestamp, podname, libraries);
    }

    @Override
    public String toString() {
        return "ReharvestItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                ", pid='" + pid + '\'' +
                ", rootPid='" + rootPid + '\'' +
                ", ownPidPath='" + ownPidPath + '\'' +
                ", typeOfReharvest=" + typeOfReharvest +
                ", timestamp=" + timestamp +
                ", podname='" + podname + '\'' +
                ", libraries=" + libraries +
                '}';
    }
}
