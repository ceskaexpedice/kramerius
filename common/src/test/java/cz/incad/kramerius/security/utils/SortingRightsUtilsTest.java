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
package cz.incad.kramerius.security.utils;

import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.impl.ClassRightCriterium;
import cz.incad.kramerius.security.impl.GroupImpl;
import cz.incad.kramerius.security.impl.RightImpl;
import cz.incad.kramerius.security.impl.criteria.Abonents;
import cz.incad.kramerius.security.impl.criteria.DefaultIPAddressFilter;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.security.impl.criteria.StrictIPAddresFilter;

import junit.framework.TestCase;

public class SortingRightsUtilsTest extends TestCase {

//    public static int CRITERIUM_COUTER=1;
//    
//    /**
//     * MovingWall 
//     */
//    public void testMovingWallAndIPFilter() {
//        List<String> uuids = new ArrayList<String>() {{
//            add("uuid:page");
//            add("uuid:periodicalitem");
//            add("uuid:periodicalvolume");
//            add("uuid:peridodical");
//            add("uuid:repository");
//        }};
//        
//        List<Right> rights = new ArrayList<Right>();
//
//        rights.add(createRight("uuid:repository", createCommonUsers(), createDefaultIPFilter("194.*")));
//        rights.add(createRight("uuid:repository", createCommonUsers(), createMowingWall("1956")));
//        
//        
//        rights.add(createRight("uuid:periodicalitem", createCommonUsers(), createMowingWall("1966")));
//
//        Right[] sorted = SortingRightsUtils.sortRights((Right[]) rights.toArray(new Right[rights.size()]), uuids);
//        ClassRightCriterium clzCrit = (ClassRightCriterium) sorted[0].getCriterium();
//        Class<? extends RightCriterium> criteriumClz = clzCrit.getCriteriumClz();
//        assertTrue(criteriumClz.equals(DefaultIPAddressFilter.class));
//
//        clzCrit = (ClassRightCriterium) sorted[1].getCriterium();
//        criteriumClz = clzCrit.getCriteriumClz();
//        assertTrue(criteriumClz.equals(MovingWall.class));
//        assertTrue(sorted[1].getCriterium().getObjects()[0].equals("1966"));
//        
//        clzCrit = (ClassRightCriterium) sorted[2].getCriterium();
//        criteriumClz = clzCrit.getCriteriumClz();
//        assertTrue(criteriumClz.equals(MovingWall.class));
//        assertTrue(sorted[2].getCriterium().getObjects()[0].equals("1956"));
//
//    }
//    
//    
//    public void testMovingWallIPFilterStrictIPFilter() {
//        List<String> uuids = new ArrayList<String>() {{
//            add("uuid:page");
//            add("uuid:periodicalitem");
//            add("uuid:periodicalvolume");
//            add("uuid:peridodical");
//            add("uuid:repository");
//        }};
//        
//        List<Right> rights = new ArrayList<Right>();
//
//        rights.add(createRight("uuid:repository", createCommonUsers(), createDefaultIPFilter("194.*")));
//        rights.add(createRight("uuid:repository", createCommonUsers(), createMowingWall("1956")));
//        
//        rights.add(createRight("uuid:periodicalitem", createCommonUsers(), createStrictIPFilter("192.168.0.1")));
//        
//        Right[] sorted = SortingRightsUtils.sortRights((Right[]) rights.toArray(new Right[rights.size()]), uuids);
//
//        ClassRightCriterium clzCrit = (ClassRightCriterium) sorted[0].getCriterium();
//        Class<? extends RightCriterium> criteriumClz = clzCrit.getCriteriumClz();
//        assertTrue(criteriumClz.equals(StrictIPAddresFilter.class));
//        
//        clzCrit = (ClassRightCriterium) sorted[1].getCriterium();
//        criteriumClz = clzCrit.getCriteriumClz();
//        assertTrue(criteriumClz.equals(DefaultIPAddressFilter.class));
//
//        clzCrit = (ClassRightCriterium) sorted[2].getCriterium();
//        criteriumClz = clzCrit.getCriteriumClz();
//        assertTrue(criteriumClz.equals(MovingWall.class));
//        assertTrue(sorted[2].getCriterium().getObjects()[0].equals("1956"));
//
//    }
//    
//    Group createCommonUsers() {
//        Group grp = new GroupImpl(1,"common_users");
//        return grp;
//    }
//    
//    Group createAbonentRP_2007Group() {
//        Group grp = new GroupImpl(2,"abonents_2007_RP");
//        return grp;
//    }
//    
//    Group createAbonentRP_2008Group() {
//        Group grp = new GroupImpl(2,"abonents_2008_RP");
//        return grp;
//    }
//    
//    
//    Right createRight(String uuid, AbstractUser user, RightCriterium crit) {
//        Right r = new RightImpl(-1, crit, uuid, "readPreview", user);
//        return r;
//    }
//
//    public RightCriterium createMowingWall(String year) {
//        RightCriterium criterium = CriteriumType.CLASS.createCriterium(CRITERIUM_COUTER++, MovingWall.class.getName(), new Object[] {year});
//        return criterium;
//    }
//    
//    public RightCriterium createDefaultIPFilter(String... address) {
//        Object[] objs = new Object[address.length];
//        System.arraycopy(address, 0, objs, 0, objs.length);
//        RightCriterium criterium = CriteriumType.CLASS.createCriterium(CRITERIUM_COUTER++, DefaultIPAddressFilter.class.getName(), objs);
//        return criterium;
//    }
//
//    public RightCriterium createStrictIPFilter(String... address) {
//        Object[] objs = new Object[address.length];
//        System.arraycopy(address, 0, objs, 0, objs.length);
//        RightCriterium criterium = CriteriumType.CLASS.createCriterium(CRITERIUM_COUTER++, StrictIPAddresFilter.class.getName(), objs);
//        return criterium;
//    }
//    
//    public RightCriterium createAbonents(String... groups) {
//        Object[] objs = new Object[groups.length];
//        System.arraycopy(groups, 0, objs, 0, objs.length);
//        RightCriterium criterium = CriteriumType.CLASS.createCriterium(CRITERIUM_COUTER++, Abonents.class.getName(), objs);
//        return criterium;
//    }

}
