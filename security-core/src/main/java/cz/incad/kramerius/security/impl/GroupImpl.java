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

import cz.incad.kramerius.security.Group;

public class GroupImpl  implements Group, Serializable {

    private int id;
    private String gname;
    
    
    public GroupImpl(int id, String gname) {
        super();
        this.id = id;
        this.gname = gname;
    }


    @Override
    public int getId() {
        return this.id;
    }


    @Override
    public String getName() {
        return this.gname;
    }
    
}
