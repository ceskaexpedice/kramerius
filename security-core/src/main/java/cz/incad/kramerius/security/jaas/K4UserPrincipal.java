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
package cz.incad.kramerius.security.jaas;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;

public class K4UserPrincipal implements Principal {

    private User user;
    private List<Role> groups = new ArrayList<Role>();

    public K4UserPrincipal(User user) {
        super();
        this.user = user;
    }

    @Override
    public String getName() {
        return this.user.getLoginname();
    }


    public User getUser() {
        return user;
    }
    
    public void addGroup(Role group) {
        this.groups.add(group);
    }
    
    public void removeGroup(Role group) {
        this.groups.remove(group);
    }
}
