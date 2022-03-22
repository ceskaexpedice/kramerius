package cz.incad.kramerius.rest.apiNew.admin.v70.collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class Collection {
    public String pid;
    public String nameCz;
    public String nameEn;
    public String descriptionCz;
    public String descriptionEn;
    public String contentCz;
    public String contentEn;
    public LocalDateTime created;
    public LocalDateTime modified;
    public Boolean standalone;

    public List<String> items;

    @Override
    public String toString() {
        return "Collection{" +
                "pid='" + pid + '\'' +
                ", nameCz='" + nameCz + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", descriptionCz='" + descriptionCz + '\'' +
                ", descriptionEn='" + descriptionEn + '\'' +
                ", contentCz='" + contentCz + '\'' +
                ", contentEn='" + contentEn + '\'' +
                ", created=" + created +
                ", modified=" + modified +
                ", standalone=" + standalone +
                '}';
    }

    public Collection() {
    }

    public Collection(Collection original) {
        this.pid = original.pid;
        this.nameCz = original.nameCz;
        this.nameEn = original.nameEn;
        this.descriptionCz = original.descriptionCz;
        this.descriptionEn = original.descriptionEn;
        this.contentCz = original.contentCz;
        this.contentEn = original.contentEn;
        this.created = original.created;
        this.modified = original.modified;
        this.standalone = original.standalone;
        this.items = original.items;
    }

    public Collection(JSONObject definition) throws JSONException {
        System.out.println(definition.toString(1));
        if (definition.has("pid")) {
            this.pid = definition.getString("pid");
        }
        if (definition.has("name_cze")) {
            this.nameCz = definition.getString("name_cze").trim();
        }
        if (definition.has("name_eng")) {
            this.nameEn = definition.getString("name_eng").trim();
        }
        if (definition.has("description_cze")) {
            this.descriptionCz = definition.getString("description_cze").trim();
        }
        if (definition.has("description_eng")) {
            this.descriptionEn = definition.getString("description_eng").trim();
        }
        if (definition.has("content_cze")) {
            this.contentCz = definition.getString("content_cze").trim();
        }
        if (definition.has("content_eng")) {
            this.contentEn = definition.getString("content_eng").trim();
        }
        if (definition.has("standalone")) {
            this.standalone = definition.getBoolean("standalone");
        }
    }

    Collection withUpdatedDataModifiableByClient(Collection updateSource) {
        Collection updated = new Collection(this);
        updated.nameCz = updateSource.nameCz;
        updated.nameEn = updateSource.nameEn;
        updated.descriptionCz = updateSource.descriptionCz;
        updated.descriptionEn = updateSource.descriptionEn;
        updated.contentCz = updateSource.contentCz;
        updated.contentEn = updateSource.contentEn;
        updated.standalone = updateSource.standalone;
        return updated;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("pid", pid);
        json.put("name_cze", nameCz);
        json.put("name_eng", nameEn);
        json.put("description_cze", descriptionCz);
        json.put("description_eng", descriptionEn);
        json.put("content_cze", contentCz);
        json.put("content_eng", contentEn);
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
        return Objects.equals(nameCz, that.nameCz) &&
                Objects.equals(nameEn, that.nameEn) &&
                Objects.equals(descriptionCz, that.descriptionCz) &&
                Objects.equals(descriptionEn, that.descriptionEn) &&
                Objects.equals(contentCz, that.contentCz) &&
                Objects.equals(contentEn, that.contentEn) &&
                Objects.equals(standalone, that.standalone);
    }


}
