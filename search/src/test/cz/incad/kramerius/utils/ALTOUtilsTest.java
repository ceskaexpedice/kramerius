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
package cz.incad.kramerius.utils;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.ALTOUtils.AltoDisected;

/**
 * @author pavels
 *
 */
public class ALTOUtilsTest {

    @Test
    public void testAlto() throws ParserConfigurationException, SAXException, IOException {
        Document parsed = XMLUtils.parseDocument(ALTOUtilsTest.class.getResourceAsStream("res/alto.xml"));
        Assert.assertNotNull(ALTOUtils.disectAlto("cena", parsed));
        Assert.assertNotNull(ALTOUtils.disectAlto("ročník", parsed));
    }

    @Test
    public void testBadAlto() throws ParserConfigurationException, SAXException, IOException {
        Document parsed = XMLUtils.parseDocument(ALTOUtilsTest.class.getResourceAsStream("res/bad_alto.xml"));
        AltoDisected disected = ALTOUtils.disectAlto("cena", parsed);
        Assert.assertNull(disected.getAltoImageDimension());
        Assert.assertTrue(disected.getBoxes().isEmpty());
        // ok -> no exception
        
    }

    @Test
    public void testAlto2() throws ParserConfigurationException, SAXException, IOException {
        Document parsed = XMLUtils.parseDocument(ALTOUtilsTest.class.getResourceAsStream("res/nalto.xml"));
        AltoDisected disected = ALTOUtils.disectAlto("prosa", parsed);
        Assert.assertNotNull(disected.getAltoImageDimension());
        Assert.assertTrue(disected.getBoxes().size() > 0);
        
    }

    @Test
    public void testNoAlto() throws ParserConfigurationException, SAXException, IOException {
        Document parsed = XMLUtils.parseDocument(ALTOUtilsTest.class.getResourceAsStream("res/nalto.xml"));
        AltoDisected disected = ALTOUtils.disectAlto("", parsed);
        Assert.assertNotNull(disected.getAltoImageDimension());
        Assert.assertTrue(disected.getBoxes().size() == 0);
    }

    @Test
    public void testNoAlto2() throws ParserConfigurationException, SAXException, IOException {
        Document parsed = XMLUtils.parseDocument(ALTOUtilsTest.class.getResourceAsStream("res/nalto.xml"));
        AltoDisected disected = ALTOUtils.disectAlto(null, parsed);
        Assert.assertNotNull(disected.getAltoImageDimension());
        Assert.assertTrue(disected.getBoxes().size() == 0);
    }

}
