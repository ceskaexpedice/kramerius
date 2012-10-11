/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.kramerius.security;

/**
 * Default role names enumeration
 * @author pavels
 */
public enum DefaultRoles {

    /** role represents all logged and notlogged users */
    COMMON_USERS("common_users"),
    /** role represents all k4 admins */
    K4_ADMINS("k4_admins"), 
    /** role represents all public registred users */
    PUBLIC_USERS("public_users");

    private String name;

    private DefaultRoles(String name) {
        this.name = name;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public static DefaultRoles findByName(String rname) {
        DefaultRoles[] values = values();
        for (DefaultRoles vr : values) {
            if (vr.getName().equals(rname)) return vr;
        }
        return null;
    }
}
