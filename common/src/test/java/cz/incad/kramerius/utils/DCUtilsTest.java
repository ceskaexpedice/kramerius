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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DCUtilsTest {


    @Test
    public void testPeriodicalItem() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = this.getClass().getResourceAsStream("dc.uuid91214030-80bb-11e0-b482-000d606f5dc6.xml");
        
        Document doc = XMLUtils.parseDocument(is,true);
        String title = DCUtils.titleFromDC(doc);
        Assert.assertEquals("3",title);
        
        String[] idents = DCUtils.identifierlsFromDC(doc);
        Assert.assertTrue(idents.length == 2);
        Assert.assertEquals(idents[0], "uuid:91214030-80bb-11e0-b482-000d606f5dc6");
        Assert.assertEquals(idents[1], "contract:00001");
        
        
        String[] creators = DCUtils.creatorsFromDC(doc);
        Assert.assertTrue(creators.length == 0);
        
        String date = DCUtils.dateFromDC(doc);
        Assert.assertEquals(date, "2011");
        
    }
    
    @Test
    public void testPage() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = this.getClass().getResourceAsStream("dc.uuid00dbc770-8138-11e0-b63f-000d606f5dc6.xml");
        Document doc = XMLUtils.parseDocument(is,true);
        String title = DCUtils.titleFromDC(doc);
        Assert.assertEquals("6",title);
        
        String[] idents = DCUtils.identifierlsFromDC(doc);
        Assert.assertTrue(idents.length == 1);
        Assert.assertEquals(idents[0], "uuid:00dbc770-8138-11e0-b63f-000d606f5dc6");
        
        String[] creators = DCUtils.creatorsFromDC(doc);
        Assert.assertTrue(creators.length == 0);
        
        String date = DCUtils.dateFromDC(doc);
        Assert.assertNull(date);
    }    
    
    @Test
    public void testInternalPart() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = this.getClass().getResourceAsStream("dc.uuidab7e5a19-bddb-11e0-bff9-0016e6840575.xml");
        Document doc = XMLUtils.parseDocument(is,true);
        String title = DCUtils.titleFromDC(doc);
        Assert.assertEquals("„Lidé si budou vypravovat o jejich moudrosti\"",title);
        
        String[] idents = DCUtils.identifierlsFromDC(doc);
        Assert.assertTrue(idents.length == 1);
        Assert.assertEquals(idents[0], "uuid:ab7e5a19-bddb-11e0-bff9-0016e6840575");
        
        
        String[] creators = DCUtils.creatorsFromDC(doc);
        Assert.assertTrue(creators.length == 2);
        Assert.assertEquals(creators[0], "ROrr Jan");
        Assert.assertEquals(creators[1], "BIEGEL Richard");
        
        String date = DCUtils.dateFromDC(doc);
        Assert.assertNull(date);

    }
    
    @Test
    public void testPeriodicalVolume() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = this.getClass().getResourceAsStream("dc.uuidf7e50720-80b6-11e0-9ec7-000d606f5dc6.xml");
        Document doc = XMLUtils.parseDocument(is,true);
        String title = DCUtils.titleFromDC(doc);
        Assert.assertEquals("33",title);
        
        String[] idents = DCUtils.identifierlsFromDC(doc);
        Assert.assertTrue(idents.length == 2);
        Assert.assertEquals(idents[0], "uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6");
        Assert.assertEquals(idents[1], "contract:00001");
        
        
        String[] creators = DCUtils.creatorsFromDC(doc);
        Assert.assertTrue(creators.length == 0);

        String date = DCUtils.dateFromDC(doc);
        Assert.assertEquals("2011",date);

    }

    @Test
    public void testPeriodikum() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = this.getClass().getResourceAsStream("dc.uuid045b1250-7e47-11e0-add1-000d606f5dc6.xml");
        Document doc = XMLUtils.parseDocument(is,true);
        String title = DCUtils.titleFromDC(doc);
        Assert.assertEquals("Dějiny a současnost",title);
        
        String[] idents = DCUtils.identifierlsFromDC(doc);
        Assert.assertTrue(idents.length == 2);
        Assert.assertEquals(idents[0], "uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
        Assert.assertEquals(idents[1], "issn:0862-6111");
        
        
        String[] creators = DCUtils.creatorsFromDC(doc);
        Assert.assertTrue(creators.length == 0);
        
        String date = DCUtils.dateFromDC(doc);
        Assert.assertNull(date);
    }
    
}
