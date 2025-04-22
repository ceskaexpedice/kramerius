/*
 * Copyright (C) Apr 2, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.client.v70;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import static cz.incad.kramerius.rest.apiNew.client.v70.ItemsResource.*;


public class ItemsResourceTest {

    /**
     * Support all options for size 
     * from IIIF Image protocol
     * <ul>
     *  <li><code>w,</code> 
     * <li><code>^w,</code>
     * <li><code>,h<code>
     * <li><code>^,h</code>
     * <li><code>^w,h</code>
     * <li><code>!w,h</code>
     * <li><code>^!w,h</code>
     * </code>
     */
    @Test
    public void testIIIFProtocol() {
        
        Pair<String, String> val1 = iiifValues("123,");

        Assert.assertNotNull(val1);
        Assert.assertNotNull(val1.getLeft());
        Assert.assertTrue(val1.getLeft().equals("123"));
        Assert.assertNull(val1.getRight());
        
        
        Pair<String, String> val2 = iiifValues("^124,");

        Assert.assertNotNull(val2);
        Assert.assertNotNull(val2.getLeft());
        Assert.assertTrue(val2.getLeft().equals("124"));
        Assert.assertNull(val2.getRight());

        
        Pair<String, String> val3 = iiifValues(",125");
        Assert.assertNotNull(val3);
        Assert.assertNotNull(val3.getLeft());
        Assert.assertTrue(val3.getLeft().equals(""));
        Assert.assertNotNull(val3.getRight());
        Assert.assertTrue(val3.getRight().equals("125"));

        
        Pair<String, String> val4 = iiifValues("^,126");
        Assert.assertNotNull(val4);
        Assert.assertNotNull(val4.getLeft());
        Assert.assertTrue(val4.getLeft().equals(""));
        Assert.assertNotNull(val4.getRight());
        Assert.assertTrue(val4.getRight().equals("126"));

        Pair<String, String> val5 = iiifValues("^132,124");
        Assert.assertNotNull(val5);
        Assert.assertNotNull(val5.getLeft());
        Assert.assertTrue(val5.getLeft().equals("132"));
        Assert.assertNotNull(val5.getRight());
        Assert.assertTrue(val5.getRight().equals("124"));

        
        Pair<String, String> val6 = iiifValues("!132,124");
        Assert.assertNotNull(val6);
        Assert.assertNotNull(val6.getLeft());
        Assert.assertTrue(val6.getLeft().equals("132"));
        Assert.assertNotNull(val6.getRight());
        Assert.assertTrue(val6.getRight().equals("124"));

        Pair<String, String> val7 = iiifValues("^!132,124");
        Assert.assertNotNull(val7);
        Assert.assertNotNull(val7.getLeft());
        Assert.assertTrue(val7.getLeft().equals("132"));
        Assert.assertNotNull(val7.getRight());
        Assert.assertTrue(val7.getRight().equals("124"));

        Pair<String, String> val8 = iiifValues("!^132,124");
        Assert.assertNotNull(val8);
        Assert.assertNotNull(val8.getLeft());
        Assert.assertTrue(val8.getLeft().equals("132"));
        Assert.assertNotNull(val8.getRight());
        Assert.assertTrue(val8.getRight().equals("124"));
        
    }
    
    @Test
    public void testCacheDir() {
        boolean bool = ItemsResource.isChacheDirDisabledAndFromCache(false, "kramerius4://deepZoomCache");
        Assert.assertFalse(bool);

        bool = ItemsResource.isChacheDirDisabledAndFromCache(true, "kramerius4://deepZoomCache");
        Assert.assertTrue(bool);

        bool = ItemsResource.isChacheDirDisabledAndFromCache(false, "https://iip.server");
        Assert.assertFalse(bool);

        bool = ItemsResource.isChacheDirDisabledAndFromCache(true, "https://iip.server");
        Assert.assertFalse(bool);

    }
    
}
