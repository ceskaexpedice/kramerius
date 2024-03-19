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
package cz.incad.kramerius;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.impl.DatabaseLicensesManagerImpl;
import cz.incad.kramerius.security.licenses.impl.lock.ExclusiveLockMapsImpl;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;

public class TestDBConnectionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Connection.class).annotatedWith(Names.named("kramerius4")).toProvider(ConProvider4T.class);
        //move
        bind(SolrAccess.class).annotatedWith(Names.named("new-index")).to(SolrAccessImplNewIndex.class).in(Scopes.SINGLETON);

        bind(LicensesManager.class).to(DatabaseLicensesManagerImpl.class);
        bind(ExclusiveLockMaps.class).to(ExclusiveLockMapsImpl.class).asEagerSingleton();

    }
}
