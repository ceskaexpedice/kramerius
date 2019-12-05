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
 * Criterium type enum
 * @author pavels
 */
public enum CriteriumType {
    
    /** script type criterium */
    SCRIPT("script",0),    

    /** class type criterium */
    CLASS("class",1);
    
    private String name;
    private int val;
    
    private CriteriumType(String name, int val) {
        this.name = name;
        this.val = val;
    }
    
    /**
     * Criterium type name
     * @return
     */
    public String getName() {
        return name;
    }
    
    /**
     * Db type discriminator
     * @return
     */
    public int getVal() {
        return val;
    }
    
    /**
     * Find criterium type by db value discriminator
     * @param value raw value
     * @return
     */
    public static CriteriumType findByValue(int value) {
        CriteriumType[] vals = CriteriumType.values();
        for (CriteriumType critType : vals) {
            if(critType.getVal() == value) {
                return critType;
            }
        }
        throw new IllegalStateException("no type found ");
    }
}
