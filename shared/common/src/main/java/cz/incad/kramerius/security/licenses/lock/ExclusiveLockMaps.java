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
package cz.incad.kramerius.security.licenses.lock;

import java.util.List;

import cz.incad.kramerius.security.licenses.License;

public interface ExclusiveLockMaps {
    
    public ExclusiveLockMap findOrCreateByHash(String hash, License license, String pid, String fmtName);
    
    public ExclusiveLockMap findHash(String hash);
    
    public List<String> getAllHashes();

    public void refreshLicense(License license);
}
