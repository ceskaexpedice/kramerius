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
package cz.incad.kramerius.security.impl;

import java.io.Serializable;

import cz.incad.kramerius.security.Role;

public class RoleImpl  implements Role, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2191143201581065880L;
    private int id;
    private String gname;
    private int personalAdminId;
    
    
    public RoleImpl(int id, String gname, int personalAdminId) {
        super();
        this.id = id;
        this.gname = gname;
        this.personalAdminId = personalAdminId;
    }


    @Override
    public int getId() {
        return this.id;
    }


    @Override
    public String getName() {
        return this.gname;
    }

    
    @Override
    public int getPersonalAdminId() {
        return this.personalAdminId;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RoleImpl other = (RoleImpl) obj;
        if (id != other.id)
            return false;
        return true;
    }


    @Override
    public Boolean isPersonalAdminDefined() {
        return this.getPersonalAdminId() > 0;
    }
}

