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
package cz.incad.kramerius.security;

/**
 * Enumeration for special objects used in security system
 */
public enum SpecialObjects {
    
	/** Represents whole repository */
    REPOSITORY("1");
    
    private String uuid;

    private SpecialObjects(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public static SpecialObjects findSpecialObject(String uuid) {
        for (SpecialObjects specialObject : values()) {
            if (specialObject.getUuid().equals(uuid))  {
                return specialObject;
            }
        }
        return null;
        
    }
    
    public static boolean isSpecialObject(String uuid) {
        return findSpecialObject(uuid) != null;
    }
    
    
    
    public static void main(String[] args) {
        System.out.println(REPOSITORY.name());
    }
}
