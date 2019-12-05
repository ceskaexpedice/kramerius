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

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.security.impl.http.MockGuiceSecurityHTTPModule;

public class TestGuiceModule extends AbstractGuiceTestCase {

    @Test
    public void testModule() throws RightCriteriumException {
        Injector injector = injector();
        RightCriteriumWrapperFactory wrapperFactory = injector.getInstance(RightCriteriumWrapperFactory.class);
        RightCriteriumWrapper createCriterium = wrapperFactory.createCriteriumWrapper(MovingWall.class.getName());
        org.junit.Assert.assertEquals("cz.incad.kramerius.security.impl.RightCriteriumWrapperImpl", createCriterium.getClass().getName());

        RightCriteriumWrapper existingWrapper = wrapperFactory.loadExistingWrapper(CriteriumType.CLASS, MovingWall.class.getName(), -1, null);
        org.junit.Assert.assertEquals("cz.incad.kramerius.security.impl.RightCriteriumWrapperImpl", existingWrapper.getClass().getName());
    }
    
    @Override
    protected Injector injector() {
        return Guice.createInjector(
                new MockGuiceSecurityModule(), 
                new MockGuiceSecurityHTTPModule(), 
                new MockRightCriteriumContextGuiceMudule());
    }
        
}
