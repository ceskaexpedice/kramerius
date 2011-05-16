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
package cz.incad.kramerius.security.guice;

import static org.easymock.EasyMock.replay;

import org.easymock.EasyMock;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;

public class MockRightCriteriumContextGuiceMudule extends AbstractModule {

    @Override
    protected void configure() {
        FedoraAccess fedoraAccess = EasyMock.createMock(FedoraAccess.class);
        bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).toInstance(fedoraAccess);

        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        bind(SolrAccess.class).toInstance(solrAccess);
        
        
    }
}
