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
package cz.incad.kramerius.security.impl.http;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import org.easymock.EasyMock;

import com.google.inject.AbstractModule;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class MockGuiceSecurityHTTPModule extends AbstractModule {

    @Override
    protected void configure() {
        IsActionAllowed isAllowed = EasyMock.createMock(IsActionAllowed.class);
        EasyMock.expect(isAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), FedoraUtils.IMG_FULL_STREAM, SpecialObjects.REPOSITORY.getPid(), new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid()))).andReturn(true);
        replay(isAllowed);

        bind(IsActionAllowed.class).toInstance(isAllowed);
        bind(User.class).toProvider(MockUserProvider.class);
    }
}
