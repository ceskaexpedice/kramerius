package cz.incad.kramerius.rest.apiNew.admin.v10.collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class Collection {
    public String pid;
    public String name;
    public String description;
    public String content;
    public LocalDateTime created;
    public LocalDateTime modified;
    public Boolean standalone;

    public List<String> items;

    @Override
    public String toString() {
        return "Collection{" +
                "pid='" + pid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", content='" + content + '\'' +
                ", created=" + created +
                ", modified=" + modified +
                ", standalone=" + standalone +
                '}';
    }

    public Collection() {
    }

    public Collection(Collection original) {
        this.pid = original.pid;
        this.name = original.name;
        this.description = original.description;
        this.content = original.content;
        this.created = original.created;
        this.modified = original.modified;
        this.standalone = original.standalone;
        this.items = original.items;
    }

    public Collection(JSONObject definition) throws JSONException {
        if (definition.has("pid")) {
            this.pid = definition.getString("pid");
        }
        if (definition.has("name")) {
            this.name = definition.getString("name").trim();
        }
        if (definition.has("description")) {
            this.description = definition.getString("description").trim();
        }
        if (definition.has("content")) {
            this.content = definition.getString("content").trim();
        }
        if (definition.has("standalone")) {
            this.standalone = definition.getBoolean("standalone");
        }
    }

    Collection withUpdatedDataModifiableByClient(Collection updateSource) {
        Collection updated = new Collection(this);
        updated.name = updateSource.name;
        updated.description = updateSource.description;
        updated.content = updateSource.content;
        updated.standalone = updateSource.standalone;
        return updated;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("pid", pid);
        json.put("name", name);
        json.put("description", description);
        json.put("content", content);
        if (created != null) {
            json.put("created", created.toString());
        }
        if (modified != null) {
            json.put("modified", modified.toString());
        }
        json.put("standalone", standalone == null ? false : standalone);
        if (items != null) {
            JSONArray itemsJson = new JSONArray();
            for (String item : items) {
                itemsJson.put(item);
            }
            json.put("items", items);
        }
        return json;
    }

    public boolean equalsInDataModifiableByClient(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collection that = (Collection) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(content, that.content) &&
                Objects.equals(standalone, that.standalone);
    }


}
