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
package cz.incad.Kramerius.security.strenderers;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import cz.incad.kramerius.security.Role;

public class RoleWrapper implements Role {
    
    public static final RoleWrapper NON_ROLE = new RoleWrapper(null);
    
    private Role role;
    private boolean selected = false;
    private ResourceBundle resourceBundle;
    private boolean canbedeleted = true;

    private RoleWrapper roleAdministrator = null;
    
    public RoleWrapper(Role rl) {
        super();
        this.role = rl;
    }
    
    

    public RoleWrapper(Role role, boolean canbedeleted) {
        super();
        this.role = role;
        this.canbedeleted = canbedeleted;
    }




    @Override
    public int getId() {
        return this.role != null ? this.role .getId() : -1;
    }

    @Override
    public String getName() {
        return this.role != null ? this.role.getName() : "-none-";
    }

    //TODO: I18N
    @Override
    public String toString() {
        return getName();
    }

    
    public static List<RoleWrapper> wrap(List<Role> grps, boolean b) {
        List<RoleWrapper> wrappers = new ArrayList<RoleWrapper>();
        for (Role grp : grps) {
            wrappers.add(new RoleWrapper(grp));
        }
        if (b) {
            wrappers.add(0, NON_ROLE);
        }
        return wrappers;
    }

    @Override
    public int getPersonalAdminId() {
        return this.role != null ? this.role.getPersonalAdminId() : -1;
    }
    

    public boolean isSelected() {
        return selected;
    }
    

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    
    
    public boolean isCanbedeleted() {
        return canbedeleted;
    }



    public void setCanbedeleted(boolean canbedeleted) {
        this.canbedeleted = canbedeleted;
    }



    @Override
    public Boolean isPersonalAdminDefined() {
        return this.role.isPersonalAdminDefined();
    }



    public RoleWrapper getRoleAdministrator() {
        return roleAdministrator;
    }



    public void setRoleAdministrator(RoleWrapper roleAdministrator) {
        this.roleAdministrator = roleAdministrator;
    }

    
}