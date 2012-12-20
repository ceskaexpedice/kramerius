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
/**
 * 
 */
package cz.incad.kramerius.imaging;

import junit.framework.Assert;

import org.junit.Test;

import cz.incad.Kramerius.imaging.DeepZoomServlet;

/**
 * @author pavels
 *
 */
public class DeepZoomServletTest {

    @Test
    public void testDeepZoom() {
        String disected = DeepZoomServlet.disectZoom("http://krameriusdemo.mzk.cz/search/deepZoom/uuid:4a7ec660-af36-11dd-a782-000d606f5dc6/_files/10/1_0.jpg");
        Assert.assertTrue("uuid:4a7ec660-af36-11dd-a782-000d606f5dc6/_files/10/1_0.jpg".equals(disected));

        disected = DeepZoomServlet.disectZoom("http://krameriusdemo.mzk.cz/search/zoomify/uuid:4a57a2a7-d0e9-11e1-945e-0050569d679d/ImageProperties.xml");
        Assert.assertTrue("uuid:4a57a2a7-d0e9-11e1-945e-0050569d679d/ImageProperties.xml".equals(disected));

        disected = DeepZoomServlet.disectZoom("http://krameriusdemo.mzk.cz/search/zoomify/uuid:4a57a2a7-d0e9-11e1-945e-0050569d679d/TileGroup0/0-0-0.jpg");
        Assert.assertTrue("uuid:4a57a2a7-d0e9-11e1-945e-0050569d679d/TileGroup0/0-0-0.jpg".equals(disected));

    }
}
