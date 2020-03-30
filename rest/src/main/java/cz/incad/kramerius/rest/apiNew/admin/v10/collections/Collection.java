package cz.incad.kramerius.rest.apiNew.admin.v10.collections;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;

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

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("pid", pid);
        json.put("name", name);
        json.put("description", description);
        json.put("content", content);
        //TODO
        //json.put("", );
        //json.put("", );
        return json;
    }
}
