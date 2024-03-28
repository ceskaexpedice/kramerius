/*
 * Copyright (C) Mar 10, 2024 Pavel Stastny
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
package cz.incad.kramerius.security.licenses.impl.lock;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLock;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMap;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMapItem;
import cz.incad.kramerius.security.licenses.lock.ExclusiveMapException;
import cz.incad.kramerius.security.licenses.utils.LicenseTOJSONSupport;

public class ExclusiveLockMapImpl implements ExclusiveLockMap {

    private String formattedName;
    private String pid;
    private License license;
    private String hash;
    private int maxItemsForPid;
    
    private Map<String, ExclusiveLockMapItem> itemsMap = new HashMap<>();
    private List<ExclusiveLockMapItem> items = new ArrayList<>();

    public ExclusiveLockMapImpl(License license, String pid, String hash,String fmtName)  {
        super();
        this.license = license;
        this.pid = pid;
        this.formattedName = fmtName;
        this.hash = hash;
        this.refereshLicense(license);
    }
    
    @Override
    public String getFormatedName() {
        return this.formattedName;
    }

    @Override
    public ExclusiveLock getAssociatedExcelusiveLock() {
        return this.license.getExclusiveLock();
    }
    
    @Override
    public String getPid() {
        return this.pid;
    }

    @Override
    public String getIdenityHash() {
        return this.hash;
    }

    @Override
    public int getMaximumItems() {
        return this.maxItemsForPid;
    }

    @Override
    public synchronized List<ExclusiveLockMapItem> getLockItems() {
        return this.items;
    }

    @Override
    public synchronized void registerItem(ExclusiveLockMapItem item) {
        this.itemsMap.put(item.getTokenId(), item);
        this.items.add(item);
    }

    @Override
    public synchronized void deregisterItem(ExclusiveLockMapItem item) {
        this.itemsMap.remove(item.getTokenId());
        this.items.remove(item);
    }

    
    

    @Override
    public ExclusiveLockMapItem findByTokenId(String tokenId) {
        return this.itemsMap.get(tokenId);
    }

    @Override
    public void checkItems(Instant now) {
        //check validity 
        List<ExclusiveLockMapItem> nitems = new ArrayList<>(this.items);
        for (ExclusiveLockMapItem nitem : nitems) {
            if (!nitem.isValid(now)) {
                this.deregisterItem(nitem);
            }
        }
        while (this.maxItemsForPid < this.items.size()) {
            this.items.remove(this.items.size()-1);
        }
    }

    

    @Override
    public boolean checkAvailabitlity() {
        this.checkItems(Instant.now());
        return (this.items.size() < this.maxItemsForPid);
    }

    @Override
    public void refresh(String tokenid)  throws ExclusiveMapException {
        ExclusiveLockMapItem exclusiveLockMapItem = this.itemsMap.get(tokenid);
        if (exclusiveLockMapItem != null) {
            Instant now = Instant.now();
            if (exclusiveLockMapItem.isValid(now)) {
                exclusiveLockMapItem.refresh(now);
            } else {
                this.deregisterItem(exclusiveLockMapItem);
            }
        } else {
            throw new ExclusiveMapException(String.format("cannot find item by token %s", tokenid));
        }
    }

    @Override
    public License getAssociatedLicense() {
        return this.license;
    }

    @Override
    public void refereshLicense(License l)  {
        if (l.getName().equals(this.license.getName())) {
            this.license = l;
            this.maxItemsForPid = license.exclusiveLockPresent() ? license.getExclusiveLock().getMaxReaders() : 0;
            this.checkItems(Instant.now());
        }
    }

    @Override
    public JSONObject toJSONHeaderObject() {
        JSONObject obj = new JSONObject();
        obj.put("formattedName", this.formattedName);
        obj.put("hash", this.hash);
        obj.put("pid", this.pid);
        obj.put("maxItems", this.maxItemsForPid);
        if (this.getAssociatedLicense() != null) {
            JSONObject licJSON = LicenseTOJSONSupport.licenseToJSON(this.getAssociatedLicense());
            obj.put("license", licJSON);
            
        }
        return obj;
        
    }
    
    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = toJSONHeaderObject();
        JSONArray jsonArray = new JSONArray();
        List<ExclusiveLockMapItem> lockItems = getLockItems();
        lockItems.forEach(li-> {
            jsonArray.put(li.toJSONObject());
        });
        
        obj.put("items", jsonArray);
        return obj;
    }
}

