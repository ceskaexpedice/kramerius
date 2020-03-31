package cz.incad.kramerius.rest.apiNew.admin.v10.collections;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Collection {
    public String pid;
    public String name;
    public String description;
    public String content;
    public LocalDateTime created;
    public LocalDateTime modified;
    //TODO: priznak, jestli je "vlastni" nebo tak nejak, tj. jestli muze byt zobrazena na nejvyssi urovni, i kdyz je treba podsbirkou

    @Override
    public String toString() {
        return "Collection{" +
                "pid='" + pid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", content='" + content + '\'' +
                ", created=" + created +
                ", modified=" + modified +
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
        //TODO
        //this.created = definition.getString("");
        //this.created = definition.getString("");
    }

    Collection withUpdatedTexts(Collection updateSource) {
        Collection updated = new Collection(this);
        updated.name = updateSource.name;
        updated.description = updateSource.description;
        updated.content = updateSource.content;
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
        return json;
    }

    public boolean equalsInTexts(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collection that = (Collection) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(content, that.content);
    }

}
