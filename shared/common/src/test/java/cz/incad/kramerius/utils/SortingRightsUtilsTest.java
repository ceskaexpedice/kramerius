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
package cz.incad.kramerius.utils;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.impl.RightImpl;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.utils.SortingRightsUtils;

public class SortingRightsUtilsTest {

    @Test
    public void testSortByPid() {

        List<Right> rights = new ArrayList<Right>(); {
            rights.add(new RightImpl(1, null,"uuid:repository","read",new RoleImpl(-1, "su", -1)));
            rights.add(new RightImpl(2, null,"uuid:monograph","read",new RoleImpl(-1, "su", -1)));
            rights.add(new RightImpl(3, null,"uuid:internalpart","read",new RoleImpl(-1, "su", -1)));
            rights.add(new RightImpl(4, null,"uuid:page","read",new RoleImpl(-1, "su", -1)));
        }
        
        List<String> pids = new ArrayList<String>();{
            pids.add("uuid:page");
            pids.add("uuid:internalpart");
            pids.add("uuid:monograph");
            pids.add("uuid:repository");
        }

        ObjectPidsPath path = new ObjectPidsPath("uuid:repository","uuid:monograph","uuid:internalpart","uuid:page");
        SortingRightsUtils.sortByPID(rights, path);
        
        for (int i = 0; i < rights.size(); i++) {
            Right right = rights.get(i);
            TestCase.assertEquals(right.getPid(),pids.get(i));
        }
        
    }
}
