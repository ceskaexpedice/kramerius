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
package cz.incad.kramerius.security.impl.criteria;

import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.security.utils.RightsDBUtils;

import junit.framework.TestCase;

public class AbstractIPAddressFilterTest extends TestCase {

    public void testAddress() {
        DefaultIPAddressFilter defIPAddr = new DefaultIPAddressFilter();
        
        Object[] objs = RightsDBUtils.valsFromString("147.231.62.[1-9];147.231.62.1[0-9];147.231.62.2[0-9];147.231.62.30");

        String base = "147.231.62.";
        for (int i = 1; i <= 30; i++) {
            String remoteAddr = base+i;
            assertTrue("adresa "+remoteAddr, defIPAddr.matchIPAddresses(objs, remoteAddr));
        }
    }
    
}
