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
package cz.incad.kramerius.security.database.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.TestDBConnectionModule;
import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.guice.GuiceSecurityModule;
import cz.incad.kramerius.security.guice.MockRightCriteriumContextGuiceMudule;
import cz.incad.kramerius.security.impl.RightCriteriumParamsImpl;
import cz.incad.kramerius.security.impl.RightImpl;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.security.impl.http.MockGuiceSecurityHTTPModule;

public class DatabaseRightManagerTests extends AbstractGuiceTestCase{


    /*
    public DatabaseRightManagerTests() {
        super();
    }

    @Before
    public void cleanup() throws IOException, SQLException {
        dropTables();
    }
    
    @Test
    public void testInsertRight() throws SQLException {
        Injector injector = injector();
        RightsManager manager = injector.getInstance(RightsManager.class);
        
        int rid = insertRightSupport(injector, manager);

        Right rightById = manager.findRightById(rid);

        Assert.assertNotNull(rightById.getCriteriumWrapper());
        Assert.assertTrue(rightById.getCriteriumWrapper().getId() > 0);
        Assert.assertFalse(rightById.getCriteriumWrapper().isJustCreated());
        Assert.assertNotNull(rightById.getCriteriumWrapper().getCriteriumParams());
        Assert.assertTrue(rightById.getCriteriumWrapper().getCriteriumType()==CriteriumType.CLASS);
        Assert.assertTrue(rightById.getCriteriumWrapper().getRightCriterium().getQName().equals(MovingWall.class.getName()));
        
        
        Right[] findAllRights = manager.findAllRights(new String[] {"uuid:0xABC"}, SecuredActions.READ.getFormalName());
        Assert.assertTrue(findAllRights.length == 1);
        Assert.assertNotNull(findAllRights[0].getCriteriumWrapper());
        Assert.assertTrue(findAllRights[0].getCriteriumWrapper().getId() > 0);
        Assert.assertFalse(findAllRights[0].getCriteriumWrapper().isJustCreated());
        Assert.assertNotNull(findAllRights[0].getCriteriumWrapper().getCriteriumParams());
        Assert.assertTrue(findAllRights[0].getCriteriumWrapper().getCriteriumType()==CriteriumType.CLASS);
        Assert.assertTrue(findAllRights[0].getCriteriumWrapper().getRightCriterium().getQName().equals(MovingWall.class.getName()));
    }

    

    @Test
    public void testUpdateRight() throws SQLException {
        Injector injector = injector();
        RightsManager manager = injector.getInstance(RightsManager.class);
        int rid = insertRightSupport(injector, manager);
        
        Right rightById = manager.findRightById(rid);
        RightCriteriumParamsImpl params = new RightCriteriumParamsImpl(-1);
        params.setLongDescription("longdesc");
        params.setShortDescription("shortdesc");
        params.setObjects(new Object[] {"1","a","2"});
        rightById.getCriteriumWrapper().setCriteriumParams(params);
        
        manager.updateRight(rightById);
        
        Right rightById2 = manager.findRightById(rid);
        Assert.assertFalse(rightById2.getCriteriumWrapper().isJustCreated());
        Assert.assertTrue(rightById2.getCriteriumWrapper().getCriteriumParams().getLongDescription().equals("longdesc"));
        Assert.assertTrue(rightById2.getCriteriumWrapper().getCriteriumParams().getShortDescription().equals("shortdesc"));
        
    }

    @Test
    public void testSelectsRight() throws SQLException {
        Injector injector = injector();
        RightsManager manager = injector.getInstance(RightsManager.class);
        int rid = insertRightSupport(injector, manager);

        Right rightById = manager.findRightById(rid);
        RightCriteriumWrapper critWrap = rightById.getCriteriumWrapper();
        RightCriteriumWrapper copyOfCritWrap = manager.findRightCriteriumById(critWrap.getId());
        
        Assert.assertEquals(critWrap.getId(), copyOfCritWrap.getId());
        Assert.assertEquals(critWrap.getCriteriumType(), copyOfCritWrap.getCriteriumType());
        Assert.assertEquals(critWrap.isJustCreated(), copyOfCritWrap.isJustCreated());
        Assert.assertEquals(critWrap.getRightCriterium().getQName(), copyOfCritWrap.getRightCriterium().getQName());

        RightCriteriumParams critParams = critWrap.getCriteriumParams();
        RightCriteriumParams copyOfParams = manager.findParamById(critWrap.getCriteriumParams().getId());
        Assert.assertEquals(critParams.getId(), copyOfParams.getId());
        Assert.assertEquals(critParams.getLongDescription(), copyOfParams.getLongDescription());
        Assert.assertEquals(critParams.getShortDescription(), copyOfParams.getShortDescription());
        Assert.assertEquals(Arrays.asList(critParams.getObjects()), Arrays.asList(copyOfParams.getObjects()));
    }

    
    public int insertRightSupport(Injector injector, RightsManager manager) throws SQLException {
        RightCriteriumParamsImpl paramsImpl = new RightCriteriumParamsImpl(-1);
        paramsImpl.setObjects(new String[] {"1","2","3"});
        paramsImpl.setShortDescription("shortDesc");
        
        RightCriteriumWrapperFactory wrapperFactory = injector.getInstance(RightCriteriumWrapperFactory.class);
        RightCriteriumWrapper mw = wrapperFactory.createCriteriumWrapper( MovingWall.class.getName());
        mw.setCriteriumParams(paramsImpl);
        
        User mockUser = EasyMock.createMock(User.class);
        EasyMock.expect(mockUser.getId()).andReturn(1);
        EasyMock.replay(mockUser);

        
        RightImpl rightImpl = new RightImpl(1, mw, "0xABC", SecuredActions.READ.getFormalName(),mockUser);
        rightImpl.setCriteriumWrapper(mw);

        int rid = manager.insertRight(rightImpl);
        return rid;
    }
    */
    
    @Override
    protected Injector injector() {
        return Guice.createInjector(
                new GuiceSecurityModule(),
                new MockGuiceSecurityHTTPModule(), 
                new MockRightCriteriumContextGuiceMudule(), 
                new TestDBConnectionModule());
    }


}
