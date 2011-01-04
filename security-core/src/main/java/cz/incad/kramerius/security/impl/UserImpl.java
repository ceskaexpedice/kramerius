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

import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.User;

public class UserImpl implements User {

    private int id;
    private String firstName;
    private String surName;
    private String loginName;

    private Group[] groups;
    
    public UserImpl(int id, String firstName, String surName, String loginName) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.surName = surName;
        this.loginName = loginName;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public String getSurname() {
        return this.surName;
    }

    @Override
    public String getLoginname() {
        return this.loginName;
    }

    @Override
    public Group[] getGroups() {
        return this.groups;
    }

    public void setGroups(Group[] grps) {
        this.groups = grps;
    }
    
}
