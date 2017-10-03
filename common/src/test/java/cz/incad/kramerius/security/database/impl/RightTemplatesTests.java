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

import static cz.incad.kramerius.utils.WhitespaceUtility.replace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.easymock.EasyMock;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.database.SecurityDatabaseUtils;
import cz.incad.kramerius.security.guice.MockGuiceSecurityModule;
import cz.incad.kramerius.security.guice.MockRightCriteriumContextGuiceMudule;
import cz.incad.kramerius.security.impl.RightCriteriumParamsImpl;
import cz.incad.kramerius.security.impl.RightImpl;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.security.impl.http.MockGuiceSecurityHTTPModule;
import junit.framework.Assert;

public class RightTemplatesTests {

    @Test
    public void testFindRights() {
        StringTemplate tmpl = SecurityDatabaseUtils.stGroup().getInstanceOf("findAllRights");

        Map<String, List<String>> m = new HashMap<String, List<String>>();

        m.put("gname", Arrays.asList("k4_admins", "common_users"));
        m.put("action", Arrays.asList("read", "store"));
        m.put("uuid", Arrays.asList("uuid:112233", "uuid:223344"));

        tmpl.setAttribute("params", m);
        String expectedSQL = "select * from right_entity ent\n"
                + "left join rights_criterium_entity crit on (ent.rights_crit=crit.crit_id) left join criterium_param_entity param on (crit.citeriumparam=param.crit_param_id) left join  user_entity users on  (ent.user_id = users.user_id) left join  group_entity groups on  (ent.group_id = groups.group_id)\n"
                + " where  (action in ('read','store'))     and  (gname in ('k4_admins','common_users'))     and  (uuid in ('uuid:112233','uuid:223344'))        ";

        String templateString = tmpl.toString();

        String expectedSQLReplaced = replace(expectedSQL);
        String templateStringReplaced = replace(templateString);
        Assert.assertEquals(expectedSQLReplaced, templateStringReplaced);
    }

    
    @Test
    public void testFindRights_SomeParams() {
        StringTemplate tmpl = SecurityDatabaseUtils.stGroup().getInstanceOf("findAllRights");

        Map<String, List<String>> m = new HashMap<String, List<String>>();

        m.put("gname", Arrays.asList("k4_admins", "common_users"));
        tmpl.setAttribute("params", m);
        String expectedSQL = "select * from right_entity ent\n"
                + "left join rights_criterium_entity crit on (ent.rights_crit=crit.crit_id) left join criterium_param_entity param on (crit.citeriumparam=param.crit_param_id) left join  user_entity users on  (ent.user_id = users.user_id) left join  group_entity groups on  (ent.group_id = groups.group_id)\n"
                + " where  (gname in ('k4_admins','common_users'))        ";

        String templateString = tmpl.toString();
        Assert.assertEquals(replace(expectedSQL), replace(templateString));
    }

    @Test
    public void testFindRights_NoParams() {
        StringTemplate tmpl = SecurityDatabaseUtils.stGroup().getInstanceOf("findAllRights");
        tmpl.setAttribute("params", null);
        String expectedSQL = "select * from right_entity ent\n"
                + "left join rights_criterium_entity crit on (ent.rights_crit=crit.crit_id) left join criterium_param_entity param on (crit.citeriumparam=param.crit_param_id) left join  user_entity users on  (ent.user_id = users.user_id) left join  group_entity groups on  (ent.group_id = groups.group_id)\n"
                + "   ";
        String templateString = tmpl.toString();
        Assert.assertEquals(replace(expectedSQL), replace(templateString));

    }

    @Test
    public void testInsertCriteriumTemplate() {
        Injector injector = injector();
        RightCriteriumWrapperFactory wrapperFactory = injector.getInstance(RightCriteriumWrapperFactory.class);
        RightCriteriumWrapper mw = wrapperFactory.createCriteriumWrapper(MovingWall.class.getName());

        StringTemplate template1 = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRightCriterium");
        template1.setAttribute("criteriumWrapper", mw);
        template1.setAttribute("type", mw.getCriteriumType().getVal());

        String sql1 = template1.toString();
        String expectedSql = "        insert into rights_criterium_entity(crit_id,qname,\"type\")\n"
                + "        values(nextval('crit_id_sequence'),\n"
                + "            'cz.incad.kramerius.security.impl.criteria.MovingWall',\n" + "            1)  ";

        Assert.assertEquals(replace(expectedSql), replace(sql1));

        RightCriteriumParamsImpl paramsImpl = new RightCriteriumParamsImpl(2);
        paramsImpl.setObjects(new String[] { "1", "2", "3" });
        mw.setCriteriumParams(paramsImpl);

        StringTemplate template2 = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRightCriterium");
        template2.setAttribute("criteriumWrapper", mw);
        template2.setAttribute("type", mw.getCriteriumType().getVal());

        String sql2 = template2.toString();
        String expectedSql2 = " \n"
                + "        insert into rights_criterium_entity(crit_id,qname, \"type\",citeriumparam)\n"
                + "        values(nextval('crit_id_sequence'),\n"
                + "            'cz.incad.kramerius.security.impl.criteria.MovingWall',\n" + "            1,\n"
                + "            2 )  ";

        Assert.assertEquals(replace(expectedSql2), replace(sql2));
    }

    @Test
    public void testInsertRightCriteriumParamsTemplate() {
        RightCriteriumParamsImpl paramsImpl = new RightCriteriumParamsImpl(2);
        paramsImpl.setObjects(new String[] { "1", "2", "3" });
        paramsImpl.setShortDescription("short desc");

        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRightCriteriumParams");
        template.setAttribute("params", paramsImpl);
        String sql = template.toString();
        String expectedSql = "    insert into criterium_param_entity(crit_param_id,short_desc,long_desc, vals) \n"
                + "    values(\n" + "        nextval('crit_param_id_sequence'),\n" + "        'short desc',\n"
                + "        '',\n" + "        '1;2;3'\n" + "    )";

        Assert.assertEquals(replace(expectedSql), replace(sql));
    }

    @Test
    public void testInsertRightTemplate() {
        Injector injector = injector();

        RightCriteriumParamsImpl paramsImpl = new RightCriteriumParamsImpl(2);
        paramsImpl.setObjects(new String[] { "1", "2", "3" });
        paramsImpl.setShortDescription("shortDesc");

        RightCriteriumWrapperFactory wrapperFactory = injector.getInstance(RightCriteriumWrapperFactory.class);
        RightCriteriumWrapper mw = wrapperFactory.loadExistingWrapper(CriteriumType.CLASS, MovingWall.class.getName(),
                5, null);
        mw.setCriteriumParams(paramsImpl);

        User mockUser = EasyMock.createMock(User.class);
        EasyMock.expect(mockUser.getId()).andReturn(111);

        RightImpl rightImpl = new RightImpl(1, mw, "0xABC", SecuredActions.READ.getFormalName(), mockUser);
        rightImpl.setCriteriumWrapper(mw);

        StringTemplate template = SecurityDatabaseUtils.stGroup().getInstanceOf("insertRight");
        template.setAttribute("association", rightImpl.getUser() instanceof Role ? "group_id" : "user_id");
        template.setAttribute("right", rightImpl);
        template.setAttribute("priority",
                rightImpl.getFixedPriority() == 0 ? "NULL" : "" + rightImpl.getFixedPriority());
        String sql = template.toString();

        String expectedSql = " \n"
                + "        insert into right_entity(right_id,uuid,action,rights_crit,\"user_id\", fixed_priority) \n"
                + "        values(\n" + "            nextval('right_id_sequence'),\n" + "            'uuid:0xABC',\n"
                + "            'read',\n" + "            5,\n" + "            0,\n" + "            NULL\n"
                + "            )  ";

        Assert.assertEquals(replace(expectedSql), replace(sql));
    }

    protected Injector injector() {
        return Guice.createInjector(new MockGuiceSecurityModule(), new MockGuiceSecurityHTTPModule(),
                new MockRightCriteriumContextGuiceMudule());
    }

}
