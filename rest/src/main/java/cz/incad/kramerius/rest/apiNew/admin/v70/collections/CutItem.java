/*
 * Copyright (C) Nov 19, 2023 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.apiNew.admin.v70.collections;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.RepositoryApi;

public class CutItem {
    
    public static final Logger LOGGER = Logger.getLogger(CutItem.class.getName());
    
    private boolean generatedThumbnail;
    private String name;
    private String description;
    private String url;
    
    
    public CutItem() {}

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    
    public void setGeneratedThumbnail(boolean generatedThumbnail) {
        this.generatedThumbnail = generatedThumbnail;
    }
    
    public boolean containsGeneratedThumbnail() {
        return generatedThumbnail;
    }
    
        
    
    
    public String getThumbnailmd5() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(url.getBytes());
        byte[] digest = md.digest();
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            hexString.append(String.format("%02x", b));
        }
        return "IMG_"+hexString.toString().toUpperCase();
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public static final List<CutItem> fromJSONArray(JSONArray jsonArray) {
        List<CutItem> items = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject clippingDef = jsonArray.getJSONObject(i);
            items.add(CutItem.fromJSONObject(clippingDef));
        }
        return items;
    }
    
    public static final CutItem fromJSONObject(JSONObject obj) {
        CutItem item = new CutItem();
        if (obj.has("name")) {
            item.setName(obj.getString("name"));
        }
        
        if (obj.has("description")) {
            item.setDescription(obj.getString("description"));
        }
        
        if (obj.has("description")) {
            item.setDescription(obj.getString("description"));
        }
        
        if (obj.has("url")) {
            
            item.setUrl(obj.getString("url"));
        }
        
        return item;
        
    }
    
    
    @Override
    public int hashCode() {
        return Objects.hash(description, name, url);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CutItem other = (CutItem) obj;
        return Objects.equals(description, other.description) && Objects.equals(name, other.name)
                && Objects.equals(url, other.url);
    }

    @Override
    public String toString() {
        return "ClippingItem [name=" + name + ", description=" + description + ", url=" + url + "]";
    }

    public JSONObject toJSON() {
        try {
            JSONObject object = new JSONObject();
            object.put("name", this.name);
            object.put("description", this.description);
            object.put("url", this.url);
            if (this.generatedThumbnail) {
                try {
                    String thumb = getThumbnailmd5();
                    object.put("thumb", thumb);
                } catch(Exception ex) {
                    LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
                    
                }
            }
            return object;
        } catch (JSONException /*| NoSuchAlgorithmException */e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new JSONObject();
        }
    }
    
    public void initGeneratedThumbnail(AkubraRepository akubraRepository, String pid) throws NoSuchAlgorithmException, RepositoryException, IOException {
        if (akubraRepository.datastreamExists(pid, getThumbnailmd5())) {
            this.setGeneratedThumbnail(true);
        }
    }

}
