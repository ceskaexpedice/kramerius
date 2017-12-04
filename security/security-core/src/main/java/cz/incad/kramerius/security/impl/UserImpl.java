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

    private static final long serialVersionUID = -7870625906836793952L;
    private int id;
    private String firstName;
    private String surName;
    private String loginName;
    private String email;
    
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
        return this.firstName==null ? "" : this.firstName;
    }

    public void setFirstName(String fname) {
        this.firstName = fname;
    }
    
    public void setSurname(String sname) {
        this.surName = sname;
    }
    
    @Override
    public String getSurname() {
        return this.surName==null ? "" : this.surName;
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
    

    
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + id;
        result = prime * result + ((surName == null) ? 0 : surName.hashCode());
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
        UserImpl other = (UserImpl) obj;
        if (firstName == null) {
            if (other.firstName != null)
                return false;
        } else if (!firstName.equals(other.firstName))
            return false;
        if (id != other.id)
            return false;
        if (surName == null) {
            if (other.surName != null)
                return false;
        } else if (!surName.equals(other.surName))
            return false;
        return true;
    }

    
    
}
