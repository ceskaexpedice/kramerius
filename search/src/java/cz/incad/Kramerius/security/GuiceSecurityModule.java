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
package cz.incad.Kramerius.security;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletScopes;

import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.DatabaseRightsManager;
import cz.incad.kramerius.security.impl.DatabaseUserManager;
import cz.incad.kramerius.security.impl.RightCriteriumContextFactoryImpl;

public class GuiceSecurityModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(IsActionAllowed.class).to(IsActionAllowedFromRequest.class);
        bind(RightsManager.class).to(DatabaseRightsManager.class);
        bind(UserManager.class).to(DatabaseUserManager.class);
        bind(RightCriteriumContextFactory.class).to(RightCriteriumContextFactoryImpl.class);
        bind(User.class).toProvider(CurrentLoggedUserProvider.class);
    }
}
