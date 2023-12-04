package cz.incad.kramerius.rest.apiNew.admin.v70.collections;

import org.apache.commons.collections4.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 
 * Collection data object 
 */
public final class Collection {
    
    public static enum ThumbnailbStateEnum {
        thumb, content, none;
    }
    
    /** pid */
    public String pid;
    
    /** Localized names */
    public String nameUndefined;
    public Map<String, String> names = new HashMap<>();
        
    /** Localized descriptions */
    public Map<String, String> descriptions = new HashMap<>();
    
    /** Localized keywords **/
    public Map<String, List<String>> keywords = new HashMap<>();
    
    public String descriptionUndefined;
    
    /** Localized contents */
    public Map<String, String> contents = new HashMap<>();
    public String contentUndefined;
    
    public LocalDateTime created;
    public LocalDateTime modified;
    public Boolean standalone;
    
    /** Content; pids of objects */
    public List<String> items;
    public List<CutItem> clippingItems;
    
    
    public String author;
    
    public ThumbnailbStateEnum thumbnailInfo = ThumbnailbStateEnum.none;
    
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
        this.names = original.names;
        this.descriptions = original.descriptions;
        this.contents = original.contents;
        
        this.created = original.created;
        this.modified = original.modified;
        this.standalone = original.standalone;
        this.items = original.items;

        this.author = original.author;
        
        this.keywords = original.keywords;
        
        this.thumbnailInfo = original.thumbnailInfo;
        this.clippingItems = original.clippingItems;
    }

    public Collection(JSONObject definition) throws JSONException {
        if (definition.has("pid")) {
            this.pid = definition.getString("pid");
        }

        if (definition.has("author")) {
            this.author = definition.getString("author");
        }

        mapLoadFromJSONString(definition,"names", this.names);
        mapLoadFromJSONString(definition,"descriptions", this.descriptions);
        mapLoadFromJSONString(definition,"contents", this.contents);
        
        mapLoadFromArray(definition, "keywords", this.keywords);
        
        if (definition.has("standalone")) {
            this.standalone = definition.getBoolean("standalone");
        }
        
        if (definition.has("thumbnail")) {
            this.thumbnailInfo = ThumbnailbStateEnum.valueOf(definition.optString("thumbnail"));
        }

        if (definition.has("clippingitems")) {
            List<CutItem> items = new ArrayList<>();
            JSONArray jsonArray = definition.getJSONArray("clippingitems");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject clippingDef = jsonArray.getJSONObject(i);
                items.add(CutItem.fromJSONObject(clippingDef));
            }
            this.clippingItems = items;
        }
    }

    private void mapLoadFromArray(JSONObject definition, String mkey, Map<String,List<String>> map) {
        Iso639Converter converter = new Iso639Converter();
        
        if (definition.has(mkey)) {
            JSONObject subObj = definition.getJSONObject(mkey);
            subObj.keySet().forEach(key-> {
                if (converter.isConvertable(key.toString())) {
                    List<String> list = converter.convert(key.toString());
                    list.forEach(remappedKey-> {
                        List<String> vals = new ArrayList<>();
                        
                        JSONArray jsonArr =  subObj.getJSONArray(key.toString());
                        for (int i = 0; i < jsonArr.length(); i++) { vals.add(jsonArr.getString(i));}
                        
                        map.put(remappedKey.toString(), vals);
                    });
                } else {
                    List<String> vals = new ArrayList<>();
                    
                    JSONArray jsonArr =  subObj.getJSONArray(key.toString());
                    for (int i = 0; i < jsonArr.length(); i++) { vals.add(jsonArr.getString(i));}

                    map.put(key.toString(), vals);
                }
            });
        }
    }
    
    private void mapLoadFromJSONString(JSONObject definition, String mkey, Map<String,String> map) {
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
        updated.names = updateSource.names;
        updated.descriptions = updateSource.descriptions;
        updated.contents = updateSource.contents;
        updated.keywords = updateSource.keywords;
        updated.standalone = updateSource.standalone;
        
        updated.author = updateSource.author;
        
        return updated;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("pid", pid);
        json.put("author", this.author);

        json.put("thumbnail", this.thumbnailInfo.name());

        simpleMapToObj(json, "names", this.names);
        simpleMapToObj(json, "descriptions", this.descriptions);
        simpleMapToObj(json, "contents", this.contents);
        arrayMapToObj(json, "keywords", this.keywords);
        
        
        
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
        
        if (this.clippingItems != null && this.clippingItems.size()>0) {
            JSONArray jsonArr = new JSONArray();
            this.clippingItems.stream().forEach(itm-> {
                JSONObject itmJSON = itm.toJSON();
                jsonArr.put(itmJSON);
            });
            json.put("clippingitems", jsonArr);
        }
        return json;
    }

    private void simpleMapToObj(JSONObject json, String masterKey, Map<String, String> map) {
        if (map != null && map.size()>0) {
            JSONObject obj = new JSONObject();
            map.keySet().forEach(key-> {
                obj.put(key, map.get(key));
            });
            json.put(masterKey, obj);
            
        }
    }


    private void arrayMapToObj(JSONObject json, String masterKey, Map<String, List<String>> map) {
        if (map != null && map.size()>0) {
            JSONObject obj = new JSONObject();
            map.keySet().forEach(key-> {
                List<String> vals = map.get(key);
                JSONArray jsonArr = new JSONArray();
                vals.stream().forEach(jsonArr::put);
                obj.put(key, jsonArr);
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
                Objects.equals(standalone, that.standalone) && 
                Objects.equals(keywords, that.keywords) && 
                Objects.equals(author, that.author);
    }


}
