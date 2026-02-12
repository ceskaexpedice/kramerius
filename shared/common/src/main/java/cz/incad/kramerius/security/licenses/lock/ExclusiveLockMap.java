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
package cz.incad.kramerius.security.licenses.lock;

import java.time.Instant;
import java.util.List;

import org.json.JSONObject;

import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.lock.ExclusiveReadersLock.ExclusiveLockType;

// one lock map
public interface ExclusiveLockMap {
    
    public String LOCK_HASH = "lockHash";
    public String LOCK_TYPE = "lockType";
    
    public String getFormatedName();
    
    
    public String getPid();

    public License getAssociatedLicense();
    
    public ExclusiveReadersLock getAssociatedExcelusiveLock();

    public String getIdenityHash();

    public int getMaximumItems();
    
    public ExclusiveLockType getLockType();
    
    
    public List<ExclusiveLockMapItem> getLockItems();

    public ExclusiveLockMapItem findByTokenId(String tokenId);
    
    public void registerItem(ExclusiveLockMapItem item);
    
    public void deregisterItem(ExclusiveLockMapItem item);
    

    public void checkItems(Instant now);
    
    public void refresh(String tokenid) throws ExclusiveMapException;
    
    public void refereshLicense(License l);
    
    public boolean checkAvailabitlity();
    
    public JSONObject toJSONHeaderObject();
    public JSONObject toJSONObject();
}
