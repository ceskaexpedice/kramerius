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
package org.kramerius.replications;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author pavels
 *
 */
public class K4ReplicationProcessTest {

    @Test
    public void prepareURL() {
        String goodURL = "http://vmkramerius/handle/uuid:XXXXX";
        String good = K4ReplicationProcess.prepareURL(goodURL,"false");
        Assert.assertTrue("http://vmkramerius/api/v4.6/replication/uuid:XXXXX/tree".equals(good));
    }

    @Test
    public void prepareURLWithCollection() {
        String goodURL = "http://vmkramerius/handle/uuid:XXXXX";
        String good = K4ReplicationProcess.prepareURL(goodURL,"true");
        Assert.assertTrue("http://vmkramerius/api/v4.6/replication/uuid:XXXXX/tree?replicateCollections=true".equals(good));
    }
    
    
    @Test
    public void pidFrom() {
        String goodURL = "http://vmkramerius/handle/uuid:XXXXX";
        String goodPid = K4ReplicationProcess.pidFrom(goodURL);
        Assert.assertTrue("uuid:XXXXX".equals(goodPid));

        String badUrl = "darmomluvit";
        String badPid = K4ReplicationProcess.pidFrom(badUrl);
        Assert.assertTrue("".equals(badPid));
    }
    
    @Test
    public void testDescription() {
        String goodURL = "http://vmkramerius/handle/uuid:XXXXX";
        String good = K4ReplicationProcess.descriptionURL(goodURL);
        Assert.assertTrue("http://vmkramerius/api/v4.6/replication/uuid:XXXXX".equals(good));
        
        String badURL = "http://vmkramerius/trouble/double";
        String bad = K4ReplicationProcess.descriptionURL(badURL);
        // withnout pid -> cannot find it
        Assert.assertTrue("http://vmkramerius/trouble/doubleapi/v4.6/replication/".equals(bad));

    }

}
