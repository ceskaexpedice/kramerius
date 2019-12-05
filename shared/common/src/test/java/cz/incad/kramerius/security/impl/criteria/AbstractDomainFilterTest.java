/*
 * Copyright (C) 2012 Pavel Stastny
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

import java.net.UnknownHostException;

import cz.incad.kramerius.security.utils.RightsDBUtils;
import junit.framework.TestCase;

public class AbstractDomainFilterTest extends TestCase {

    public void testDomains() throws UnknownHostException {
        DefaultDomainFilter defaultDomainFilter = new DefaultDomainFilter();
        
        Object[] objs = RightsDBUtils.valsFromString(".*.lib.cas.cz;.*.nkp.cz;.*.seznam.cz");

        String base = ".seznam.cz";
        
        for (int i = 1; i <= 30; i++) {
            String remoteHost = i+base;
            String remoteAddr = i+".77.74.72";
            assertTrue("adresa "+remoteAddr, defaultDomainFilter.matchDomain(objs, remoteAddr,remoteHost));
        }
    }
}
