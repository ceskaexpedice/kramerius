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

import static org.easymock.EasyMock.replay;

import cz.incad.kramerius.security.*;
import org.easymock.EasyMock;

import com.google.inject.AbstractModule;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.utils.FedoraUtils;

public class MockGuiceSecurityHTTPModule extends AbstractModule {

    @Override
    protected void configure() {
        RightsReturnObject rightsReturnObject = new RightsReturnObject(null, EvaluatingResultState.FALSE);
        RightsResolver isAllowed = EasyMock.createMock(RightsResolver.class);
        EasyMock.expect(isAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), FedoraUtils.IMG_FULL_STREAM, SpecialObjects.REPOSITORY.getPid(), new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid()))).andReturn(rightsReturnObject);
        replay(isAllowed);

        bind(RightsResolver.class).toInstance(isAllowed);
        bind(User.class).toProvider(MockUserProvider.class);
    }
}
