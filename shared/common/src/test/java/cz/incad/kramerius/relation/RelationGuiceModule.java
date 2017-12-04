/*
 * Copyright (C) 2010 Jan Pokorsky
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

package cz.incad.kramerius.relation;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.relation.impl.RelationServiceImpl;
import org.easymock.EasyMock;

/**
 *
 * @author Jan Pokorsky
 */
public class RelationGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        FedoraAccess fedoraAccess = EasyMock.createMock(FedoraAccess.class);
        bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).toInstance(fedoraAccess);
        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
    }

}
