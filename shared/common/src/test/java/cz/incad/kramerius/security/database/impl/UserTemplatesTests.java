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

import org.junit.Ignore;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.security.guice.MockGuiceSecurityModule;
import cz.incad.kramerius.security.guice.MockRightCriteriumContextGuiceMudule;
import cz.incad.kramerius.security.impl.http.MockGuiceSecurityHTTPModule;

@Ignore
public class UserTemplatesTests extends AbstractGuiceTestCase {
    
//    @Test
//    public void testFindGroupsWhichAdministrate(){
//        Injector injector = injector();
//        
//        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findGroupsWhichAdministrate");
//        template.setAttribute("grps", new int[]{1,2,3});
//        String sql = template.toString();
//        
//        String expectedSql="    select * from group_entity \n"+
//"    where personal_admin_id in (1,2,3) ";
//        
//      Assert.assertEquals(expectedSql, sql);
//
////        
////        String expectedSql =
////            " \n"+
////            "        insert into right_entity(right_id,uuid,action,rights_crit,\"user_id\", fixed_priority) \n"+
////            "        values(\n"+
////            "            nextval('right_id_sequence'),\n"+
////            "            'uuid:0xABC',\n"+
////            "            'read',\n"+
////            "            5,\n"+
////            "            0,\n"+
////            "            NULL\n"+
////            "            )  ";
////        
////        Assert.assertEquals(expectedSql, sql);
//    }
//
//    
    @Override
    protected Injector injector() {
        return Guice.createInjector(
                new MockGuiceSecurityModule(), 
                new MockGuiceSecurityHTTPModule(), 
                new MockRightCriteriumContextGuiceMudule());
    }



}
