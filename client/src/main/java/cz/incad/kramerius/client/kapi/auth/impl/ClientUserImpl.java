/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.client.kapi.auth.impl;

import cz.incad.kramerius.client.kapi.auth.ClientUser;

public class ClientUserImpl extends AbstractUser implements ClientUser {

    private String firstName;
    private String surname;
    
    public ClientUserImpl(String uname, String up) {
        super(uname, up);
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public String getSurname() {
        return this.surname;
    }

    @Override
    public void updateInformation(String fname, String sname) {
        this.firstName = fname;
        this.surname = sname;
    }

    @Override
    public void updatePassword(String pswd) {
        super.updatePassword(pswd);
    }
}
