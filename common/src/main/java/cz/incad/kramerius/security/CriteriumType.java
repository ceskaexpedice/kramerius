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

import cz.incad.kramerius.security.impl.ClassRightCriterium;
import cz.incad.kramerius.security.impl.RightCriteriumParamsImpl;

public enum CriteriumType {
    
    SCRIPT("script",0) {
        @Override
        public RightCriterium createCriterium(int critId, int critParamId, String qname, String shortDesc, String longDesc,Object[] objs) {
            try {
                Class<? extends RightCriterium> clz = (Class<? extends RightCriterium>) Class.forName(qname);
                ClassRightCriterium crc = new ClassRightCriterium(clz, critId);
                
                RightCriteriumParams params = new RightCriteriumParamsImpl(critParamId);
                params.setObjects(objs);
                params.setLongDescription(longDesc);
                params.setShortDescription(shortDesc);
                //crc.setObjects(objs);
                return crc;
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public RightCriterium createCriteriumWithoutParams(int critId, String qname) {
            try {
                Class<? extends RightCriterium> clz = (Class<? extends RightCriterium>) Class.forName(qname);
                ClassRightCriterium crc = new ClassRightCriterium(clz, critId);
                return crc;
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        
        
    } ,
    
    CLASS("class",1) {
        @Override
        public RightCriterium createCriterium(int critId, int critParamId, String qname, String shortDesc, String longDesc,Object[] objs) {
            try {
                Class<? extends RightCriterium> clz = (Class<? extends RightCriterium>) Class.forName(qname);
                ClassRightCriterium crc = new ClassRightCriterium(clz, critId);
                RightCriteriumParams params = new RightCriteriumParamsImpl(critParamId);
                params.setObjects(objs);
                params.setLongDescription(longDesc);
                params.setShortDescription(shortDesc);
                //crc.setObjects(objs);
                crc.setCriteriumParams(params);
                return crc;
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public RightCriterium createCriteriumWithoutParams(int critId, String qname) {
            try {
                Class<? extends RightCriterium> clz = (Class<? extends RightCriterium>) Class.forName(qname);
                ClassRightCriterium crc = new ClassRightCriterium(clz, critId);
                return crc;
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        
        
    };
    
    private String name;
    private int val;
    
    private CriteriumType(String name, int val) {
        this.name = name;
        this.val = val;
    }
    public String getName() {
        return name;
    }
    public int getVal() {
        return val;
    }
    
    public abstract RightCriterium createCriterium(int critId, int critParamId, String qname, String shortDesc, String longDesc,Object[] objs);
    public abstract RightCriterium createCriteriumWithoutParams(int critId, String qname);
    
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
