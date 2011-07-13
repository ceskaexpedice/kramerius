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

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;

public class UserImpl implements User {

    /**
     * 
     */
    private static final long serialVersionUID = -7870625906836793952L;
    private int id;
    private String firstName;
    private String surName;
    private String loginName;
    
    private int personalAdminId;

    private Role[] groups;
    
    public UserImpl(int id, String firstName, String surName, String loginName, int personalAdminId) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.surName = surName;
        this.loginName = loginName;
        this.personalAdminId = personalAdminId;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String fname) {
        this.firstName = fname;
    }
    
    public void setSurname(String sname) {
        this.surName = sname;
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
    public Role[] getGroups() {
        return this.groups;
    }

    public void setGroups(Role[] grps) {
        this.groups = grps;
    }

    public int getPersonalAdminId() {
        return personalAdminId;
    }

    public void setPersonalAdminId(int personalAdminId) {
        this.personalAdminId = personalAdminId;
    }

    @Override
    public boolean isAdministratorForGivenGroup(int personalAdminId) {
        Role[] grps = getGroups();
        for (Role grp : grps) {
            if (grp.getId() == personalAdminId) return true;
        }
        return false;
    }
    

    
    
    @Override
    public boolean hasSuperAdministratorRole() {
        Role[] groups = this.getGroups();
        for (Role group : groups) {
            if (group.getPersonalAdminId() <= 0 ) {
                return true;
            }
        }
        return false;
    }

    
    
}
