/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.users.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.NotActivatedUsersSingleton;

public class NotActivatedUsersSingletonImpl implements NotActivatedUsersSingleton {
    
    private Map<String, User> map = new HashMap<String, User>();
    
    @Override
    public String addNotActivatedUser(User user) {
        String randomUUID = UUID.randomUUID().toString();
        map.put(randomUUID, user);
        return randomUUID;
    }

    @Override
    public void removeNotActivatedUser(User user, String key) {
        this.map.remove(key);
    }

    public User getNotActivatedUser(String key) {
        return this.map.get(key);
    }
    
}
