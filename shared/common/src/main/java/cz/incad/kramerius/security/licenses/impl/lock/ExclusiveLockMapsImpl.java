/*
 * Copyright (C) Mar 11, 2024 Pavel Stastny
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLock;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMap;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;

public class ExclusiveLockMapsImpl implements ExclusiveLockMaps {

    private Map<String, ExclusiveLockMap> maps = new HashMap<>();
    
    
    @Override
    public synchronized ExclusiveLockMap findOrCreateByHash(String hash, License l, String pid, String fmtName) {
        if (!this.maps.containsKey(hash)) {
            this.maps.put(hash, new ExclusiveLockMapImpl(l,pid, hash, fmtName));
        }
        return this.maps.get(hash);
    }


    @Override
    public synchronized ExclusiveLockMap findHash(String hash) {
        return this.maps.get(hash);
    }


    @Override
    public List<String> getAllHashes() {
        return new ArrayList<>( this.maps.keySet());
    }


    @Override
    public void refreshLicense(License license) {
        maps.keySet().forEach(key-> {
            maps.get(key).refereshLicense(license);
        });
    }
}
