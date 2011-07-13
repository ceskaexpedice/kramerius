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

import java.sql.SQLSyntaxErrorException;
import java.util.Stack;

import junit.framework.Assert;

import org.antlr.stringtemplate.StringTemplate;
import org.easymock.EasyMock;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.guice.MockGuiceSecurityModule;
import cz.incad.kramerius.security.guice.MockRightCriteriumContextGuiceMudule;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.RightCriteriumParamsImpl;
import cz.incad.kramerius.security.impl.RightImpl;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.security.impl.http.MockGuiceSecurityHTTPModule;

public class UserTemplatesTests extends AbstractGuiceTestCase {


    @Test
    public void insertRole() {
        RoleImpl grpImpl = new RoleImpl(2, "4TEST", -1);
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRole");
        template.setAttribute("role",grpImpl);
        String sql = template.toString().trim();
        String expectedSql = 
            "insert into group_entity(group_id,gname, personal_admin_id)\n"+
            "        values(\n"+
            "            nextval('group_id_sequence'),\n"+
            "            '4TEST',\n"+
            "            NULL\n"+
            "    )";  

        /*
        System.out.println(sql);
        System.out.println(expectedSql);
        Stack<Character> stckSQL = new Stack<Character>();
        for (int i = 0; i < sql.length(); i++) {
            stckSQL.push(sql.charAt(i));
        }
        Stack<Character> expectedSQL = new Stack<Character>();
        for (int i = 0; i < expectedSql.length(); i++) {
            expectedSQL.push(expectedSql.charAt(i));
        }
        while(!stckSQL.isEmpty()) {
            Character chr = stckSQL.pop();
            Character echr = expectedSQL.pop();
            if (chr != echr) {
                System.out.println(expectedSQL.toString());
                System.out.println(stckSQL.toString());
            }
        }*/
        
        
        Assert.assertEquals(expectedSql, sql);
        
        grpImpl = new RoleImpl(2, "4TEST", 3);
        template = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRole");
        template.setAttribute("role",grpImpl);
        sql = template.toString().trim();
        expectedSql = 
            "insert into group_entity(group_id,gname, personal_admin_id)\n"+
            "        values(\n"+
            "            nextval('group_id_sequence'),\n"+
            "            '4TEST',\n"+
            "            3\n"+
            "    )";
        Assert.assertEquals(expectedSql, sql);
        
        
    }

    @Test
    public void updateRole() {
        RoleImpl grpImpl = new RoleImpl(2, "4TEST", -1);
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("updateRole");
        template.setAttribute("role",grpImpl);
        String sql = template.toString().trim();
        String expectedSQL = "update group_entity set \n"+
            "            gname='4TEST', \n"+ 
            "            personal_admin_id=NULL\n"+
            "        where group_id=2";

        Assert.assertEquals(expectedSQL, sql);
        

        grpImpl = new RoleImpl(2, "4TEST", 10);
        template = SecurityDatabaseUtils.stGroup().getInstanceOf("updateRole");
        template.setAttribute("role",grpImpl);
        sql = template.toString().trim();
        expectedSQL = "update group_entity set \n"+
            "            gname='4TEST', \n"+ 
            "            personal_admin_id=10\n"+
            "         where group_id=2";

        
        Assert.assertEquals(expectedSQL, sql);
    }

    
    @Test
    public void deleteRole() {
        RoleImpl grpImpl = new RoleImpl(2, "4TEST", -1);
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("deleteRole");
        template.setAttribute("role",grpImpl);
        String sql = template.toString().trim();
        String expectedSql = "delete from group_entity \n"+
                "   where group_id=2";
        Assert.assertEquals(expectedSql, sql);
        
    }

    
    @Test
    public void testFindGroupsWhichAdministrate(){
        Injector injector = injector();
        
        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("findGroupsWhichAdministrate");
        template.setAttribute("grps", new int[]{1,2,3});
        String sql = template.toString();
        
        String expectedSql="    select * from group_entity \n"+
"    where personal_admin_id in (1,2,3) ";
        
      Assert.assertEquals(expectedSql, sql);

//        
//        String expectedSql =
//            " \n"+
//            "        insert into right_entity(right_id,uuid,action,rights_crit,\"user_id\", fixed_priority) \n"+
//            "        values(\n"+
//            "            nextval('right_id_sequence'),\n"+
//            "            'uuid:0xABC',\n"+
//            "            'read',\n"+
//            "            5,\n"+
//            "            0,\n"+
//            "            NULL\n"+
//            "            )  ";
//        
//        Assert.assertEquals(expectedSql, sql);
    }

    
    @Override
    protected Injector injector() {
        return Guice.createInjector(
                new MockGuiceSecurityModule(), 
                new MockGuiceSecurityHTTPModule(), 
                new MockRightCriteriumContextGuiceMudule());
    }


    private void compareTexts(String sql, String expectedSQL) {
        int min = Math.min(sql.length(), expectedSQL.length());
        for (int i = 0; i < min; i++) {
            if (sql.charAt(i) == expectedSQL.charAt(i)) {
                System.out.println("ok charAt("+i+") = '"+sql.charAt(i)+"'");
            } else {
                System.out.println("bad sql.charAt("+i+") = '"+sql.charAt(i)+"'");
                System.out.println("bad expectedSQL.charAt("+i+") = '"+expectedSQL.charAt(i)+"'");
                
            }
        }
    }

}
