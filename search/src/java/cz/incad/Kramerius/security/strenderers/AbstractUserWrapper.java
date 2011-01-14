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

import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.User;

public class AbstractUserWrapper implements User, Group {

    private AbstractUser user;
    
    
    
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
        return ((Group)this.user).getName();
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
    public Group[] getGroups() {
        return ((User)this.user).getGroups();
    }

    public String getInputId() {
        if (this.user instanceof Group) {
            return getName();
        } else {
            return getLoginname();
        }
        
    }
    
    public String getTypeOfUser() {
        return (this.user instanceof Group) ? "group":
            "user";
    }
    
    @Override
    public String toString() {
        if (this.user instanceof Group) {
            return getName() +" (Skupina) <img src=\"img/rights-group.png\"></img>";
        } else {
            return getFirstName()+" "+getSurname()+" (Uzivatel) <img src=\"img/rights-person.png\"></img>";
        }
    }
    
}
