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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;


public class AbstractUserWrapper implements User, Role {

    public static final AbstractUserWrapper ALL_USERS_ITEM = new AbstractUserWrapper(null);
    
    private AbstractUser user;
    private boolean selected = false;
    private ResourceBundle resourceBundle;
    
    public AbstractUserWrapper(AbstractUser user) {
        super();
        this.user = user;
    }

    @Override
    public int getId() {
        return this.user.getId();
    }

    @Override
    public String getName() {
        return ((Role)this.user).getName();
    }

    @Override
    public String getFirstName() {
        return ((User)this.user).getFirstName();
    }

    @Override
    public String getSurname() {
        return ((User)this.user).getSurname();
    }

    @Override
    public String getLoginname() {
        return ((User)this.user).getLoginname();
    }

    
    @Override
    public Role[] getGroups() {
        return ((User)this.user).getGroups();
    }

    public String getInputId() {
        if (this == ALL_USERS_ITEM) return "all";
        if (this.user instanceof Role) {
            return getName();
        } else {
            return getLoginname();
        }
        
    }
    
    public String getTypeOfUser() {
        return (this.user instanceof Role) ? "group":
            "user";
    }
    
    public AbstractUser getWrappedValue() {
        return this.user;
    }
    
    
    @Override
    public String getEmail() {
        return ((User)this.user).getEmail();
    }

    @Override
    public Boolean isPersonalAdminDefined() {
        return getPersonalAdminId() > 0;
    }

    //TODO: I18N
    public String getOptionValue() {
        //<option value="$k$" selected>$k$</option>
        if (this == ALL_USERS_ITEM) return "Vsichni uzivatele";
        if (this.user instanceof Role) {
            return getName();
        } else {
            return getFirstName()+" "+getSurname();
        }
    }
    
    //TODO: I18N
    @Override
    public String toString() {
        if (this == ALL_USERS_ITEM) return "all";
        if (this.user instanceof Role) {
            if (getName().equals("common_users")) {
                return "Vsichni uzivatele <img src=\"img/rights-person.png\"></img><img src=\"img/rights-group.png\"></img>";
            } else {
                return getName() +" <img src=\"img/rights-group.png\"></img>";
            }
        } else {
            return getFirstName()+" "+getSurname()+" <img src=\"img/rights-person.png\"></img>";
        }
    }

    public static List<AbstractUserWrapper> wrap(List<AbstractUser> users, boolean b) {
        List<Integer> gids = new ArrayList<Integer>();
        List<Integer> uids = new ArrayList<Integer>();
        List<AbstractUserWrapper> wrappers = new ArrayList<AbstractUserWrapper>();
        for (AbstractUser abstractUser : users) {
            if (abstractUser instanceof Role) {
                if (!gids.contains(abstractUser.getId())) {
                    gids.add(abstractUser.getId());
                    wrappers.add(new AbstractUserWrapper(abstractUser));
                }
            } else if (abstractUser instanceof User) {
                if (!uids.contains(abstractUser.getId())) {
                    uids.add(abstractUser.getId());
                    wrappers.add(new AbstractUserWrapper(abstractUser));
                }
            }
        }
        if (b) {
            wrappers.add(0, ALL_USERS_ITEM);
        }
        return wrappers;
    }

    @Override
    public int getPersonalAdminId() {
        return this.user.getPersonalAdminId();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean isAdministratorForGivenGroup(int personalAdminId) {
        return ((User)this.user).isAdministratorForGivenGroup(personalAdminId);
    }

    @Override
    public boolean hasSuperAdministratorRole() {
        return ((User)this.user).hasSuperAdministratorRole();
    }
}
