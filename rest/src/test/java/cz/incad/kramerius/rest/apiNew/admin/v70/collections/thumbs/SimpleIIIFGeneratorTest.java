/*
 * Copyright (C) Nov 27, 2023 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.admin.v70.collections.thumbs;

import java.net.MalformedURLException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;


public class SimpleIIIFGeneratorTest {
    
    @Test
    public void testAccept() {
        // new API
        ThumbsGenerator simpleIIIFGenerator = new SimpleIIIFGenerator();
        boolean acceptNew = simpleIIIFGenerator.acceptUrl("https://k7.inovatika.dev/search/api/client/v7.0/items/uuid:62134c50-3fb6-4c11-9ec2-1608d3b608f4/image/iiif/368,648,1150,801/max/0/default.jpg");
        Assert.assertTrue(acceptNew);

        boolean acceptOld = simpleIIIFGenerator.acceptUrl("https://api.kramerius.mzk.cz/search/iiif/uuid:c0c3ab27-78c8-48f6-82a8-537728104b65/283,161,2731,1073/max/0/default.jpg");
        Assert.assertTrue(acceptOld);
    }

    @Test
    public void testBaseUrl() throws MalformedURLException {
        // new API
        Pair<String,Boolean> extractBaseUrl = SimpleIIIFGenerator.extractBaseUrl("https://k7.inovatika.dev/search/api/client/v7.0/items/uuid:62134c50-3fb6-4c11-9ec2-1608d3b608f4/image/iiif/368,648,1150,801/max/0/default.jpg");
        Assert.assertEquals(extractBaseUrl,Pair.of("https://k7.inovatika.dev/search/api/client/v7.0/items/uuid:62134c50-3fb6-4c11-9ec2-1608d3b608f4",true));

        Pair<String,Boolean> extractBaseOldUrl = SimpleIIIFGenerator.extractBaseUrl("https://api.kramerius.mzk.cz/search/iiif/uuid:c0c3ab27-78c8-48f6-82a8-537728104b65/283,161,2731,1073/max/0/default.jpg");
        Assert.assertEquals(extractBaseOldUrl,Pair.of("https://api.kramerius.mzk.cz/search/iiif/uuid:c0c3ab27-78c8-48f6-82a8-537728104b65",false));
    }

    @Test
    public void testInfoUrl() throws MalformedURLException {
        // new API
        Pair<String,Boolean> extractBaseUrl = SimpleIIIFGenerator.extractBaseUrl("https://k7.inovatika.dev/search/api/client/v7.0/items/uuid:62134c50-3fb6-4c11-9ec2-1608d3b608f4/image/iiif/368,648,1150,801/max/0/default.jpg");
        String infoUrl = SimpleIIIFGenerator.buildInfoJsonUrl(extractBaseUrl);
        Assert.assertEquals("https://k7.inovatika.dev/search/api/client/v7.0/items/uuid:62134c50-3fb6-4c11-9ec2-1608d3b608f4/image/iiif/info.json", infoUrl);
        
        Pair<String,Boolean> extractBaseOldUrl = SimpleIIIFGenerator.extractBaseUrl("https://api.kramerius.mzk.cz/search/iiif/uuid:c0c3ab27-78c8-48f6-82a8-537728104b65/283,161,2731,1073/max/0/default.jpg");
        String oldInfoUrl = SimpleIIIFGenerator.buildInfoJsonUrl(extractBaseOldUrl);
        Assert.assertEquals("https://api.kramerius.mzk.cz/search/iiif/uuid:c0c3ab27-78c8-48f6-82a8-537728104b65/info.json", oldInfoUrl);
    }

}
