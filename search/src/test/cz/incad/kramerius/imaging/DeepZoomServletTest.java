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

import org.antlr.stringtemplate.StringTemplate;
import org.junit.Test;

import cz.incad.Kramerius.imaging.DeepZoomServlet;

/**
 * @author pavels
 *
 */
public class DeepZoomServletTest {

    //@Test
    public void testDeepZoom() {
        String disected = DeepZoomServlet.disectZoom("http://krameriusdemo.mzk.cz/search/deepZoom/uuid:4a7ec660-af36-11dd-a782-000d606f5dc6/_files/10/1_0.jpg");
        Assert.assertTrue("uuid:4a7ec660-af36-11dd-a782-000d606f5dc6/_files/10/1_0.jpg".equals(disected));

        disected = DeepZoomServlet.disectZoom("http://krameriusdemo.mzk.cz/search/zoomify/uuid:4a57a2a7-d0e9-11e1-945e-0050569d679d/ImageProperties.xml");
        Assert.assertTrue("uuid:4a57a2a7-d0e9-11e1-945e-0050569d679d/ImageProperties.xml".equals(disected));

        disected = DeepZoomServlet.disectZoom("http://krameriusdemo.mzk.cz/search/zoomify/uuid:4a57a2a7-d0e9-11e1-945e-0050569d679d/TileGroup0/0-0-0.jpg");
        Assert.assertTrue("uuid:4a57a2a7-d0e9-11e1-945e-0050569d679d/TileGroup0/0-0-0.jpg".equals(disected));

    }

    @Test
    public void testAB() {
    	String dataStreamUrl = "http://localhost/fcgi-bin/iipsrv.fcgi?DeepZoom=/home/k4/iip-data/aba009-000029/UC_aba009-000029_0001.jp2";
    	StringTemplate tileUrl = DeepZoomServlet.stGroup().getInstanceOf("ntile");
        if (dataStreamUrl.endsWith("/")) dataStreamUrl = dataStreamUrl.substring(0, dataStreamUrl.length()-1);
        tileUrl.setAttribute("url", dataStreamUrl);
        tileUrl.setAttribute("level", "10");
        tileUrl.setAttribute("tile", "10");
        //System.out.println(tileUrl);
    }

    @Test
    public void testBC() {
    	String dataStreamUrl = "http://localhost/fcgi-bin/iipsrv.fcgi?DeepZoom=/home/k4/iip-data/aba009-000029/UC_aba009-000029_0001.jp2";
    
        StringTemplate dziUrl = DeepZoomServlet.stGroup().getInstanceOf("ndzi");
        if (dataStreamUrl.endsWith("/")) dataStreamUrl = dataStreamUrl.substring(0, dataStreamUrl.length()-1);
        dziUrl.setAttribute("url", dataStreamUrl);

        //System.out.println(dziUrl);
    }

}
