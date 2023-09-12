package cz.incad.kramerius.rest.apiNew.admin.v70.collections;

import org.apache.commons.collections4.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Collection {

    public String pid;
    
    public String nameUndefined;
    public Map<String, String> names = new HashMap<>();
    
//    public String descriptionCz;
//    public String descriptionEn;
    
    public Map<String, String> descriptions = new HashMap<>();
    public String descriptionUndefined;
    
//    public String contentCz;
//    public String contentEn;

    public Map<String, String> contents = new HashMap<>();
    public String contentUndefined;
    
    public LocalDateTime created;
    public LocalDateTime modified;
    public Boolean standalone;

    public List<String> items;
    
    

    @Override
    public String toString() {
        return "Collection [pid=" + pid + ", names=" + names + ", descriptions=" + descriptions + ", contents="
                + contents + ", created=" + created + ", modified=" + modified + ", standalone=" + standalone
                + ", items=" + items + "]";
    }

    public Collection() {
    }

    public Collection(Collection original) {
        this.pid = original.pid;
//        this.nameCz = original.nameCz;
//        this.nameEn = original.nameEn;
        this.names = original.names;
        
//        this.descriptionCz = original.descriptionCz;
//        this.descriptionEn = original.descriptionEn;
        this.descriptions = original.descriptions;
        
//        this.contentCz = original.contentCz;
//        this.contentEn = original.contentEn;
        this.contents = original.contents;
        
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
//        if (definition.has("name_cze")) {
//            this.nameCz = definition.getString("name_cze").trim();
//        }
//        if (definition.has("name_eng")) {
//            this.nameEn = definition.getString("name_eng").trim();
//        }
        mapLoadFromJSON(definition,"names", this.names);
        
//        if (this.nameCz != null) this.names.put("cze", this.nameCz);
//        if (this.nameEn != null) this.names.put("eng", this.nameCz);
        
        
//        if (definition.has("description_cze")) {
//            this.descriptionCz = definition.getString("description_cze").trim();
//        }
//        if (definition.has("description_eng")) {
//            this.descriptionEn = definition.getString("description_eng").trim();
//        }
        
        mapLoadFromJSON(definition,"descriptions", this.descriptions);
//        if (this.descriptionCz != null) this.descriptions.put("cze", this.descriptionCz);
//        if (this.descriptionEn != null) this.descriptions.put("eng", this.descriptionEn);
//
//        
//        if (definition.has("content_cze")) {
//            this.contentCz = definition.getString("content_cze").trim();
//        }
//        if (definition.has("content_eng")) {
//            this.contentEn = definition.getString("content_eng").trim();
//        }

        mapLoadFromJSON(definition,"contents", this.contents);

//        if (this.contentCz != null) this.contents.put("cze", this.contentCz);
//        if (this.contentEn != null) this.contents.put("eng", this.contentEn);

        
        if (definition.has("standalone")) {
            this.standalone = definition.getBoolean("standalone");
        }
        
        
    }

    private void mapLoadFromJSON(JSONObject definition, String mkey, Map<String,String> map) {
        Iso639Converter converter = new Iso639Converter();
        
        if (definition.has(mkey)) {
            JSONObject subObj = definition.getJSONObject(mkey);
            subObj.keySet().forEach(key-> {
                if (converter.isConvertable(key.toString())) {
                    List<String> list = converter.convert(key.toString());
                    list.forEach(remappedKey-> {
                        map.put(remappedKey.toString(), subObj.getString(key.toString()));
                    });
                } else {
                    map.put(key.toString(), subObj.getString(key.toString()));
                    
                }
            });
        }
    }

    Collection withUpdatedDataModifiableByClient(Collection updateSource) {
        Collection updated = new Collection(this);
//        updated.nameCz = updateSource.nameCz;
//        updated.nameEn = updateSource.nameEn;
        updated.names = updateSource.names;
        
//        updated.descriptionCz = updateSource.descriptionCz;
//        updated.descriptionEn = updateSource.descriptionEn;
        updated.descriptions = updateSource.descriptions;
        
//        updated.contentCz = updateSource.contentCz;
//        updated.contentEn = updateSource.contentEn;
        updated.contents = updateSource.contents;
        
        updated.standalone = updateSource.standalone;
        return updated;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("pid", pid);

//        json.put("name_cze", nameCz);
//        json.put("name_eng", nameEn);
            
        mapToObj(json, "names", this.names);
        
//        json.put("description_cze", descriptionCz);
//        json.put("description_eng", descriptionEn);

        mapToObj(json, "descriptions", this.descriptions);

        
//        json.put("content_cze", contentCz);
//        json.put("content_eng", contentEn);

        mapToObj(json, "contents", this.contents);

        
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

    private void mapToObj(JSONObject json, String masterKey, Map<String, String> map) {
        if (map != null && map.size()>0) {
            JSONObject obj = new JSONObject();
            map.keySet().forEach(key-> {
                obj.put(key, map.get(key));
            });
            json.put(masterKey, obj);
            
        }
    }

    
    public boolean equalsInDataModifiableByClient(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collection that = (Collection) o;
        return Objects.equals(names, that.names) &&
                Objects.equals(descriptions, that.descriptions) &&
                Objects.equals(contents, that.contents) &&
                Objects.equals(standalone, that.standalone);
    }


}
